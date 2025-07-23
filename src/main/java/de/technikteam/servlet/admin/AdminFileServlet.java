package de.technikteam.servlet.admin;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all administrative file upload actions, including creating new files
 * and uploading new versions of existing files. Note: A similarly named
 * `UploadFileServlet` using Apache Commons exists but is obsolete. This version
 * uses the standard `@MultipartConfig`.
 */
@WebServlet("/admin/uploadFile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1 MB
		maxFileSize = 1024 * 1024 * 20, // 20 MB
		maxRequestSize = 1024 * 1024 * 50) // 50 MB
public class AdminFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileServlet.class);
	private FileDAO fileDAO;

	// Whitelist of allowed MIME types for security
	private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif",
			"application/pdf", "text/markdown", "text/plain");

	private static final Map<String, String> MIME_TYPE_FRIENDLY_NAMES = Map.of("image/jpeg", "JPEG Bild", "image/png",
			"PNG Bild", "image/gif", "GIF Bild", "application/pdf", "PDF Dokument", "text/markdown", "Markdown Text",
			"text/plain", "Textdatei");

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for file upload action by user '{}'.", adminUser.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");

		if ("delete".equals(action)) {
			handleDeleteUpload(request, response, adminUser);
			return;
		}

		Part filePart;
		try {
			filePart = request.getPart("file");
		} catch (IOException | ServletException e) {
			logger.error("Error getting file part from multipart request.", e);
			session.setAttribute("errorMessage", "Fehler beim Verarbeiten der Anfrage: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		if (filePart == null || filePart.getSize() == 0) {
			logger.warn("Upload failed: File part is missing or empty.");
			session.setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		// Security: Validate MIME type against whitelist
		String contentType = filePart.getContentType();
		if (!ALLOWED_MIME_TYPES.contains(contentType)) {
			String friendlyName = MIME_TYPE_FRIENDLY_NAMES.getOrDefault(contentType, contentType);
			logger.warn("Upload rejected for user '{}': Disallowed MIME type '{}'.", adminUser.getUsername(),
					contentType);
			session.setAttribute("errorMessage", "Dateityp '" + friendlyName
					+ "' ist nicht erlaubt. Erlaubte Typen sind: Bilder, PDF, Text/Markdown.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		if ("create".equals(action)) {
			handleCreateUpload(request, response, adminUser, filePart);
		} else if ("update".equals(action)) {
			handleUpdateUpload(request, response, adminUser, filePart);
		} else {
			logger.warn("Received upload POST with unknown action: '{}'", action);
			session.setAttribute("errorMessage", "Unbekannte Upload-Aktion.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
		}
	}

	private void handleCreateUpload(HttpServletRequest request, HttpServletResponse response, User adminUser,
			Part filePart) throws IOException {
		HttpSession session = request.getSession();
		try {
			String categoryIdStr = request.getParameter("categoryId");
			String requiredRole = request.getParameter("requiredRole");
			if (categoryIdStr == null || requiredRole == null) {
				throw new IllegalArgumentException("Missing required form fields for file creation.");
			}

			int categoryId = Integer.parseInt(categoryIdStr);
			String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

			// Security: Sanitize original filename for display
			String sanitizedOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");

			// Security: Generate a unique filename for storage to prevent collisions and
			// path traversal
			String uniqueFileName = UUID.randomUUID().toString() + "-" + sanitizedOriginalFileName;

			File targetFile = new File(AppConfig.UPLOAD_DIRECTORY, uniqueFileName);

			filePart.write(targetFile.getAbsolutePath());
			logger.info("CREATE: File '{}' successfully written to disk as '{}' for user '{}'.",
					sanitizedOriginalFileName, uniqueFileName, adminUser.getUsername());

			de.technikteam.model.File newDbFile = new de.technikteam.model.File();
			newDbFile.setFilename(sanitizedOriginalFileName); // Store the original, user-friendly name
			newDbFile.setFilepath(uniqueFileName); // Store the unique, secure name for disk access
			newDbFile.setCategoryId(categoryId);
			newDbFile.setRequiredRole(requiredRole);

			if (fileDAO.createFile(newDbFile)) {
				AdminLogService.log(adminUser.getUsername(), "FILE_UPLOAD",
						"Datei '" + sanitizedOriginalFileName + "' hochgeladen.");
				session.setAttribute("successMessage", "Datei erfolgreich hochgeladen.");
			} else {
				if (!targetFile.delete()) {
					logger.warn("Failed to clean up file '{}' after DB insert failure.", targetFile.getAbsolutePath());
				}
				session.setAttribute("errorMessage", "DB-Fehler: Datei konnte nicht gespeichert werden.");
			}
		} catch (Exception e) {
			logger.error("Error during file creation upload.", e);
			session.setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleUpdateUpload(HttpServletRequest request, HttpServletResponse response, User adminUser,
			Part filePart) throws IOException {
		HttpSession session = request.getSession();
		try {
			String fileIdStr = request.getParameter("fileId");
			if (fileIdStr == null) {
				throw new IllegalArgumentException("Missing fileId for update operation.");
			}

			int fileId = Integer.parseInt(fileIdStr);
			de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);
			if (dbFile == null) {
				session.setAttribute("errorMessage", "Datei zum Aktualisieren nicht gefunden.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
				return;
			}

			// Security: Overwrite the existing file using its unique path from the
			// database.
			File targetFile = new File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());
			filePart.write(targetFile.getAbsolutePath());
			logger.info("UPDATE: File '{}' (ID: {}) successfully overwritten on disk by user '{}'.",
					dbFile.getFilename(), fileId, adminUser.getUsername());

			// Update the timestamp in the database to reflect the new version.
			if (fileDAO.touchFileRecord(dbFile.getId())) {
				AdminLogService.log(adminUser.getUsername(), "FILE_UPDATE",
						"Neue Version für Datei '" + dbFile.getFilename() + "' hochgeladen.");
				session.setAttribute("successMessage", "Neue Version erfolgreich hochgeladen.");
			} else {
				session.setAttribute("errorMessage", "DB-Fehler: Datei-Metadaten konnten nicht aktualisiert werden.");
			}
		} catch (Exception e) {
			logger.error("Error during file update upload.", e);
			session.setAttribute("errorMessage", "Fehler beim Aktualisieren: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleDeleteUpload(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		HttpSession session = request.getSession();
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			de.technikteam.model.File fileToDelete = fileDAO.getFileById(fileId);

			if (fileToDelete == null) {
				session.setAttribute("errorMessage", "Datei zum Löschen nicht gefunden.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
				return;
			}

			if (fileDAO.deleteFile(fileId)) {
				AdminLogService.log(adminUser.getUsername(), "FILE_DELETE", "Datei '" + fileToDelete.getFilename()
						+ "' (ID: " + fileId + ") aus Kategorie '" + fileToDelete.getCategoryName() + "' gelöscht.");
				session.setAttribute("successMessage", "Datei '" + fileToDelete.getFilename() + "' wurde gelöscht.");
			} else {
				session.setAttribute("errorMessage", "Datei konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid file ID for deletion", e);
			session.setAttribute("errorMessage", "Ungültige Datei-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}
}