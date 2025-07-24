package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.EventDAO;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Singleton
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);

	private final de.technikteam.dao.FileDAO fileDAO;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final AttachmentDAO attachmentDAO;
	private final ConfigurationService configService;

	@Inject
	public DownloadServlet(de.technikteam.dao.FileDAO fileDAO, EventDAO eventDAO, MeetingDAO meetingDAO,
			AttachmentDAO attachmentDAO, ConfigurationService configService) {
		this.fileDAO = fileDAO;
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.attachmentDAO = attachmentDAO;
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

		String idParam = request.getParameter("id");
		if (idParam == null || idParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required 'id' parameter.");
			return;
		}

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
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Die angeforderte Datei wurde in der Datenbank nicht gefunden.");
				return;
			}

			if (!isAuthorized) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Sie haben keine Berechtigung, diese Datei herunterzuladen.");
				return;
			}

			serveFile(filePathFromDb, filenameForDownload, user, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Die angegebene ID ist ungültig.");
		}
	}

	private boolean isUserAuthorizedForAttachment(User user, Attachment attachment) {
		if (user.hasAdminAccess()) {
			return true;
		}
		if ("NUTZER".equalsIgnoreCase(attachment.getRequiredRole())) {
			if ("EVENT".equals(attachment.getParentType())) {
				return eventDAO.isUserAssociatedWithEvent(attachment.getParentId(), user.getId());
			} else if ("MEETING".equals(attachment.getParentType())) {
				return meetingDAO.isUserAssociatedWithMeeting(attachment.getParentId(), user.getId());
			}
		}
		return false;
	}

	private void serveFile(String relativePathFromDb, String originalFilename, User user, HttpServletResponse response)
			throws IOException {
		if (relativePathFromDb == null || relativePathFromDb.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Der Dateipfad in der Datenbank ist ungültig.");
			return;
		}

		File baseDir = new File(configService.getProperty("upload.directory"));
		File requestedFile = new File(baseDir, relativePathFromDb);

		String baseDirCanonicalPath = baseDir.getCanonicalPath();
		String requestedFileCanonicalPath = requestedFile.getCanonicalPath();

		if (!requestedFileCanonicalPath.startsWith(baseDirCanonicalPath)) {
			logger.fatal("CRITICAL: Path Traversal Attack Detected! User: '{}' attempted to access '{}'",
					user.getUsername(), requestedFileCanonicalPath);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (!requestedFile.exists() || !requestedFile.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Die Datei wurde auf dem Server nicht gefunden.");
			return;
		}

		response.setContentType("application/octet-stream");
		response.setContentLengthLong(requestedFile.length());
		String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

		try (FileInputStream inStream = new FileInputStream(requestedFile);
				OutputStream outStream = response.getOutputStream()) {
			inStream.transferTo(outStream);
		}
	}
}