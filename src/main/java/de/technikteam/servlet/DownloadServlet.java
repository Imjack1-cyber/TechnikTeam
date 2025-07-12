package de.technikteam.servlet;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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

/**
 * Handles secure file downloads for both general files and specific entity
 * attachments. It ensures that only authorized users can download files and
 * protects against path traversal attacks.
 */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);

	private de.technikteam.dao.FileDAO fileDAO;
	private EventDAO eventDAO;
	private MeetingDAO meetingDAO;
	private AttachmentDAO attachmentDAO;

	@Override
	public void init() {
		fileDAO = new de.technikteam.dao.FileDAO();
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO();
		attachmentDAO = new AttachmentDAO();
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

			// Unified attachments are the primary type of file to check for.
			Attachment attachment = attachmentDAO.getAttachmentById(id);
			if (attachment != null) {
				filePathFromDb = attachment.getFilepath();
				filenameForDownload = attachment.getFilename();
				isAuthorized = isUserAuthorizedForAttachment(user, attachment);
			} else {
				// Fallback to checking the general 'files' table for legacy links or general
				// documents.
				de.technikteam.model.File dbFile = fileDAO.getFileById(id);
				if (dbFile != null) {
					filePathFromDb = dbFile.getFilepath();
					filenameForDownload = dbFile.getFilename();
					// General files are accessible if the user has admin rights or the file is
					// marked for 'NUTZER'
					isAuthorized = user.hasAdminAccess() || "NUTZER".equalsIgnoreCase(dbFile.getRequiredRole());
				}
			}

			if (filePathFromDb == null) {
				logger.error("Download failed for user '{}': No file or attachment record found for ID {}",
						user.getUsername(), id);
				response.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Die angeforderte Datei wurde in der Datenbank nicht gefunden.");
				return;
			}

			if (!isAuthorized) {
				logger.warn("Authorization DENIED for user '{}' trying to download content with ID {}",
						user.getUsername(), id);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Sie haben keine Berechtigung, diese Datei herunterzuladen.");
				return;
			}

			serveFile(filePathFromDb, filenameForDownload, user, response);

		} catch (NumberFormatException e) {
			logger.warn("Invalid ID format for download: {}", idParam);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Die angegebene ID ist ungültig.");
		}
	}

	/**
	 * Determines if a user is authorized to download a specific attachment.
	 *
	 * @param user       The user requesting the download.
	 * @param attachment The attachment metadata from the database.
	 * @return true if the user is authorized, false otherwise.
	 */
	private boolean isUserAuthorizedForAttachment(User user, Attachment attachment) {
		// Admins can download any attachment.
		if (user.hasAdminAccess()) {
			return true;
		}

		// If the attachment is public to all users, we check their association with the
		// parent entity.
		if ("NUTZER".equalsIgnoreCase(attachment.getRequiredRole())) {
			if ("EVENT".equals(attachment.getParentType())) {
				return eventDAO.isUserAssociatedWithEvent(attachment.getParentId(), user.getId());
			} else if ("MEETING".equals(attachment.getParentType())) {
				return meetingDAO.isUserAssociatedWithMeeting(attachment.getParentId(), user.getId());
			}
		}

		// By default, access is denied.
		return false;
	}

	/**
	 * Securely serves a file from the disk to the client.
	 *
	 * @param relativePathFromDb The relative path of the file as stored in the
	 *                           database.
	 * @param originalFilename   The original filename to be sent to the client.
	 * @param user               The user requesting the file (for logging).
	 * @param response           The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void serveFile(String relativePathFromDb, String originalFilename, User user, HttpServletResponse response)
			throws IOException {
		if (relativePathFromDb == null || relativePathFromDb.trim().isEmpty()) {
			logger.error("File path from DB is null or empty for user '{}'.", user.getUsername());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Der Dateipfad in der Datenbank ist ungültig.");
			return;
		}

		// Security: Prevent path traversal attacks by ensuring the resolved file path
		// is within the allowed directory.
		File baseDir = new File(AppConfig.UPLOAD_DIRECTORY);
		File requestedFile = new File(baseDir, relativePathFromDb);

		// Canonicalize paths to resolve any ".." or "." in the path string and get the
		// absolute path.
		String baseDirCanonicalPath = baseDir.getCanonicalPath();
		String requestedFileCanonicalPath = requestedFile.getCanonicalPath();

		if (!requestedFileCanonicalPath.startsWith(baseDirCanonicalPath)) {
			logger.fatal(
					"CRITICAL: Path Traversal Attack Detected! User: '{}' attempted to access '{}' via db path '{}'",
					user.getUsername(), requestedFileCanonicalPath, relativePathFromDb);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Zugriff verweigert.");
			return;
		}

		if (!requestedFile.exists() || !requestedFile.isFile()) {
			logger.error("Download failed for user '{}': File not found at resolved path {}", user.getUsername(),
					requestedFile.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Die Datei wurde auf dem Server nicht gefunden.");
			return;
		}

		response.setContentType("application/octet-stream");
		response.setContentLengthLong(requestedFile.length());

		// URL-encode the filename to handle special characters, spaces, etc., according
		// to RFC 5987.
		String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
		response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);

		logger.info("User '{}' is downloading file: {}. Size: {} bytes.", user.getUsername(),
				requestedFile.getAbsolutePath(), requestedFile.length());

		try (FileInputStream inStream = new FileInputStream(requestedFile);
				OutputStream outStream = response.getOutputStream()) {
			inStream.transferTo(outStream);
		}
	}
}