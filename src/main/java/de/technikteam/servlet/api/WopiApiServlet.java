package de.technikteam.servlet.api;

import com.google.gson.Gson;
import de.technikteam.config.AppConfig;
import de.technikteam.dao.FileDAO;
import de.technikteam.util.WopiTokenManager;
import de.technikteam.util.WopiTokenManager.TokenData;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Implements the WOPI Host protocol to integrate with Collabora Online. This
 * servlet handles requests from the Collabora server to get file metadata,
 * download file contents, and upload updated file contents.
 */
@WebServlet("/wopi/files/*")
public class WopiApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(WopiApiServlet.class);
	private FileDAO fileDAO;
	private Gson gson;

	@Override
	public void init() {
		fileDAO = new FileDAO();
		gson = new Gson();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing file ID.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		String fileId = pathParts[0];

		TokenData tokenData = WopiTokenManager.getInstance().validateAndGetData(request.getParameter("access_token"),
				fileId);
		if (tokenData == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired access token.");
			return;
		}

		de.technikteam.model.File dbFile;
		try {
			dbFile = fileDAO.getFileById(Integer.parseInt(fileId));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID format.");
			return;
		}

		if (dbFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
			return;
		}

		if (pathParts.length > 1 && "contents".equals(pathParts[1])) {
			handleGetFile(response, dbFile); // GetFile request
		} else {
			handleCheckFileInfo(request, response, dbFile, tokenData); // CheckFileInfo request
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || !pathInfo.contains("/contents")) {
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "POST is only supported for /contents.");
			return;
		}

		String fileId = pathInfo.substring(1).split("/")[0];

		TokenData tokenData = WopiTokenManager.getInstance().validateAndGetData(request.getParameter("access_token"),
				fileId);
		if (tokenData == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired access token.");
			return;
		}

		handlePutFile(request, response, fileId);
	}

	private void handleCheckFileInfo(HttpServletRequest request, HttpServletResponse response,
			de.technikteam.model.File dbFile, TokenData tokenData) throws IOException {
		logger.debug("WOPI: CheckFileInfo request for file ID {}.", dbFile.getId());

		java.io.File physicalFile = new java.io.File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());

		if (!physicalFile.exists()) {
			logger.error("Physical file for CheckFileInfo not found at path: {}", physicalFile.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical file not found on disk.");
			return;
		}

		Map<String, Object> fileInfo = new HashMap<>();
		fileInfo.put("BaseFileName", dbFile.getFilename());
		fileInfo.put("OwnerId", "admin");
		fileInfo.put("Size", physicalFile.length());
		fileInfo.put("UserId", tokenData.getUsername());
		fileInfo.put("UserFriendlyName", tokenData.getUsername());
		fileInfo.put("UserCanWrite", true);
		fileInfo.put("UserCanNotWriteRelative", true);
		fileInfo.put("SupportsUpdate", true);
		fileInfo.put("LastModifiedTime", Instant.ofEpochMilli(physicalFile.lastModified()).toString());

		// ** BUG FIX: Use the origin passed from the EditorServlet **
		// This must match the origin of the page hosting the iframe (e.g.,
		// http://localhost:8080)
		String postMessageOrigin = request.getParameter("origin");
		if (postMessageOrigin == null || postMessageOrigin.isEmpty()) {
			logger.error("WOPI: CheckFileInfo request is missing the 'origin' parameter. This is required.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required origin parameter in WOPISrc URL.");
			return;
		}
		fileInfo.put("PostMessageOrigin", postMessageOrigin);
		logger.debug("Setting PostMessageOrigin for WOPI to: {}", postMessageOrigin);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(gson.toJson(fileInfo));
	}

	private void handleGetFile(HttpServletResponse response, de.technikteam.model.File dbFile) throws IOException {
		logger.debug("WOPI: GetFile request for file ID {}.", dbFile.getId());
		java.io.File physicalFile = new java.io.File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());

		if (!physicalFile.exists()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical file not found.");
			return;
		}

		response.setContentType("application/octet-stream");
		response.setContentLengthLong(physicalFile.length());

		try (InputStream in = new FileInputStream(physicalFile); OutputStream out = response.getOutputStream()) {
			in.transferTo(out);
		}
	}

	private void handlePutFile(HttpServletRequest request, HttpServletResponse response, String fileId)
			throws IOException {
		logger.info("WOPI: PutFile request for file ID {}.", fileId);
		de.technikteam.model.File dbFile = fileDAO.getFileById(Integer.parseInt(fileId));
		if (dbFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
			return;
		}

		java.io.File physicalFile = new java.io.File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());

		try (InputStream in = request.getInputStream()) {
			Files.copy(in, physicalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			logger.info("Successfully saved updated file content for file ID {}.", fileId);
		} catch (Exception e) {
			logger.error("Error saving updated file content for file ID {}", fileId, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save file.");
			return;
		}

		if (fileDAO.touchFileRecord(dbFile.getId())) {
			logger.info("Successfully updated file timestamp in database for file ID {}.", fileId);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			logger.error("Failed to update file timestamp in database for file ID {}.", fileId);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update file metadata.");
		}
	}
}