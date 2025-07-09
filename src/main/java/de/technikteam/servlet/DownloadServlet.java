package de.technikteam.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import de.technikteam.dao.EventAttachmentDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.dao.MeetingAttachmentDAO;
import de.technikteam.dao.MeetingDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);
	private FileDAO fileDAO;
	private EventDAO eventDAO;
	private MeetingDAO meetingDAO;
	private EventAttachmentDAO eventAttachmentDAO;
	private MeetingAttachmentDAO meetingAttachmentDAO;

	@Override
	public void init() throws ServletException {
		fileDAO = new FileDAO();
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO();
		eventAttachmentDAO = new EventAttachmentDAO();
		meetingAttachmentDAO = new MeetingAttachmentDAO();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String type = request.getParameter("type");
		String idParam = request.getParameter("id");

		if (type == null || idParam == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
			return;
		}

		try {
			int id = Integer.parseInt(idParam);
			String filePathFromDb = null;
			boolean isAuthorized = false;

			if (user.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
				isAuthorized = true;
			}

			switch (type) {
			case "event":
				de.technikteam.model.EventAttachment eventAtt = eventAttachmentDAO.getAttachmentById(id);
				if (eventAtt == null) {
					logger.error("Download failed: No event attachment record found for ID {}", id);
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Anhang in Datenbank nicht gefunden.");
					return;
				}
				filePathFromDb = eventAtt.getFilepath();
				if (!isAuthorized && "NUTZER".equalsIgnoreCase(eventAtt.getRequiredRole())) {
					isAuthorized = eventDAO.isUserAssociatedWithEvent(eventAtt.getEventId(), user.getId());
				}
				break;
			case "meeting":
				de.technikteam.model.MeetingAttachment meetingAtt = meetingAttachmentDAO.getAttachmentById(id);
				if (meetingAtt == null) {
					logger.error("Download failed: No meeting attachment record found for ID {}", id);
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Anhang in Datenbank nicht gefunden.");
					return;
				}
				filePathFromDb = meetingAtt.getFilepath();
				if (!isAuthorized && "NUTZER".equalsIgnoreCase(meetingAtt.getRequiredRole())) {
					isAuthorized = meetingDAO.isUserAssociatedWithMeeting(meetingAtt.getMeetingId(), user.getId());
				}
				break;
			case "file":
				de.technikteam.model.File dbFile = fileDAO.getFileById(id);
				if (dbFile == null) {
					logger.error("Download failed: No file record found for ID {}", id);
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datei in Datenbank nicht gefunden.");
					return;
				}
				filePathFromDb = dbFile.getFilepath();
				if (!isAuthorized && "NUTZER".equalsIgnoreCase(dbFile.getRequiredRole())) {
					isAuthorized = true; 
				}
				break;
			default:
				logger.warn("Invalid download type '{}' requested by user '{}'", type, user.getUsername());
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file type specified.");
				return;
			}

			if (!isAuthorized) {
				logger.warn("Authorization DENIED for user '{}' trying to download {} file ID {}", user.getUsername(),
						type, id);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Zugriff verweigert.");
				return;
			}

			if (filePathFromDb == null || filePathFromDb.trim().isEmpty()) {
				logger.error("File path from DB is null or empty for type '{}' and ID {}.", type, id);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Dateipfad ist ungültig.");
				return;
			}

			File baseDir = new File(AppConfig.UPLOAD_DIRECTORY);
			String baseDirCanonicalPath = baseDir.getCanonicalPath();

			File requestedFile = new File(filePathFromDb);
			String sanitizedFilename = requestedFile.getName();
			File finalFile = new File(baseDir, sanitizedFilename);
			String finalFileCanonicalPath = finalFile.getCanonicalPath();

			if (!finalFileCanonicalPath.startsWith(baseDirCanonicalPath + File.separator)) {
				logger.fatal(
						"CRITICAL: Path Traversal Attack Detected! User: '{}' attempted to access '{}' via sanitized path '{}'",
						user.getUsername(), finalFileCanonicalPath, sanitizedFilename);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Zugriff verweigert.");
				return;
			}

			if (!finalFile.exists() || !finalFile.isFile()) {
				logger.error("Download failed: File not found at resolved path {}", finalFile.getAbsolutePath());
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datei nicht gefunden.");
				return;
			}

			response.setContentType("application/octet-stream");
			response.setContentLengthLong(finalFile.length());

			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"",
					URLEncoder.encode(finalFile.getName(), StandardCharsets.UTF_8.toString()));
			response.setHeader(headerKey, headerValue);

			logger.info("User '{}' is downloading file: {}. Size: {} bytes.", user.getUsername(),
					finalFile.getAbsolutePath(), finalFile.length());

			try (FileInputStream inStream = new FileInputStream(finalFile);
					OutputStream outStream = response.getOutputStream()) {

				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
			}

		} catch (NumberFormatException e) {
			logger.warn("Invalid ID format for download: {}", idParam);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige ID.");
		}
	}
}