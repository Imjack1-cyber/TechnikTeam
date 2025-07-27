// src/main/java/de/technikteam/api/v1/public_api/PublicFileStreamResource.java
package de.technikteam.api.v1.public_api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.User;
import de.technikteam.service.ConfigurationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Singleton
public class PublicFileStreamResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PublicFileStreamResource.class);

	private final FileDAO fileDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final ConfigurationService configService;

	@Inject
	public PublicFileStreamResource(FileDAO fileDAO, AttachmentDAO attachmentDAO, EventDAO eventDAO,
			MeetingDAO meetingDAO, ConfigurationService configService) {
		this.fileDAO = fileDAO;
		this.attachmentDAO = attachmentDAO;
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.configService = configService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}

		String pathInfo = request.getPathInfo();
		if (pathInfo == null || pathInfo.length() <= 1) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file request.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		String streamType = pathParts[0];

		if ("download".equals(streamType) && pathParts.length == 2) {
			handleDownload(response, user, pathParts[1]);
		} else if ("images".equals(streamType) && pathParts.length == 2) {
			handleImage(response, user, pathParts[1]);
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	private void handleDownload(HttpServletResponse response, User user, String idParam) throws IOException {
		try {
			int id = Integer.parseInt(idParam);
			String filePathFromDb = null;
			String filenameForDownload = null;
			boolean isAuthorized = false;

			Attachment attachment = attachmentDAO.getAttachmentById(id);
			if (attachment != null) {
				filePathFromDb = attachment.getFilepath();
				filenameForDownload = attachment.getFilename();
				isAuthorized = isUserAuthorizedForAttachment(user, attachment);
			} else {
				de.technikteam.model.File dbFile = fileDAO.getFileById(id);
				if (dbFile != null) {
					filePathFromDb = dbFile.getFilepath();
					filenameForDownload = dbFile.getFilename();
					isAuthorized = user.hasAdminAccess() || "NUTZER".equalsIgnoreCase(dbFile.getRequiredRole());
				}
			}

			if (filePathFromDb == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found in database.");
				return;
			}
			if (!isAuthorized) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to download this file.");
				return;
			}
			serveFile(response, user, filePathFromDb, filenameForDownload, false);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID.");
		}
	}

	private void handleImage(HttpServletResponse response, User user, String filename) throws IOException {
		String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
		String relativePath = "images" + File.separator + decodedFilename;
		serveFile(response, user, relativePath, decodedFilename, true);
	}

	private boolean isUserAuthorizedForAttachment(User user, Attachment attachment) {
		if (user.hasAdminAccess())
			return true;
		if ("NUTZER".equalsIgnoreCase(attachment.getRequiredRole())) {
			if ("EVENT".equals(attachment.getParentType())) {
				return eventDAO.isUserAssociatedWithEvent(attachment.getParentId(), user.getId());
			} else if ("MEETING".equals(attachment.getParentType())) {
				return meetingDAO.isUserAssociatedWithMeeting(attachment.getParentId(), user.getId());
			}
		}
		return false;
	}

	private void serveFile(HttpServletResponse response, User user, String relativePath, String originalFilename,
			boolean inline) throws IOException {
		File baseDir = new File(configService.getProperty("upload.directory"));
		File requestedFile = new File(baseDir, relativePath);

		String baseDirCanonicalPath = baseDir.getCanonicalPath();
		String requestedFileCanonicalPath = requestedFile.getCanonicalPath();

		if (!requestedFileCanonicalPath.startsWith(baseDirCanonicalPath)) {
			logger.fatal("CRITICAL: Path Traversal Attack Detected! User: '{}' attempted to access '{}'",
					user.getUsername(), requestedFileCanonicalPath);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (!requestedFile.exists() || !requestedFile.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found on server.");
			return;
		}

		String contentType = getServletContext().getMimeType(originalFilename);
		if (contentType == null)
			contentType = "application/octet-stream";

		response.setContentType(contentType);
		response.setContentLengthLong(requestedFile.length());
		String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
		String disposition = inline ? "inline" : "attachment";
		response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''" + encodedFilename);

		try (FileInputStream inStream = new FileInputStream(requestedFile);
				OutputStream outStream = response.getOutputStream()) {
			inStream.transferTo(outStream);
		}
	}
}