package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.ConfigurationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

@Singleton
public class MarkdownEditorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MarkdownEditorServlet.class);
	private final FileDAO fileDAO;
	private final ConfigurationService configService;

	@Inject
	public MarkdownEditorServlet(FileDAO fileDAO, ConfigurationService configService) {
		this.fileDAO = fileDAO;
		this.configService = configService;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		String fileIdParam = request.getParameter("fileId");
		if (fileIdParam == null || fileIdParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fileId parameter.");
			return;
		}

		int fileId;
		try {
			fileId = Integer.parseInt(fileIdParam);
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fileId format.");
			return;
		}

		de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);
		if (dbFile == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
			return;
		}

		boolean canUpdate = user.getPermissions().contains("FILE_UPDATE")
				|| user.getPermissions().contains("ACCESS_ADMIN_PANEL");
		boolean canRead = user.getPermissions().contains("FILE_READ");

		if (canUpdate) {
			request.setAttribute("editorMode", "edit");
		} else if (canRead) {
			request.setAttribute("editorMode", "view");
		} else {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
			return;
		}

		String fileContent;
		try {
			java.io.File physicalFile = new java.io.File(configService.getProperty("upload.directory"),
					dbFile.getFilepath());
			fileContent = new String(Files.readAllBytes(physicalFile.toPath()), StandardCharsets.UTF_8);
		} catch (NoSuchFileException e) {
			logger.error("File record found in DB for ID {} but physical file is missing at path: {}", fileId,
					dbFile.getFilepath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Physical file not found on server.");
			return;
		} catch (IOException e) {
			logger.error("Could not read file content for fileId {}", fileId, e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not read file content.");
			return;
		}

		request.setAttribute("file", dbFile);
		request.setAttribute("fileContent", fileContent);
		request.getRequestDispatcher("/views/admin/admin_editor.jsp").forward(request, response);
	}
}