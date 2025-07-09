package de.technikteam.servlet.admin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
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

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
@WebServlet("/admin/dateien")
public class AdminFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileServlet.class);
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		logger.info("Admin file page requested by user '{}'.", user.getUsername());

		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(user);
		List<FileCategory> allCategories = fileDAO.getAllCategories();

		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("allCategories", allCategories);

		logger.debug("Forwarding to admin_files.jsp with {} file groups and {} categories.", groupedFiles.size(),
				allCategories.size());
		request.getRequestDispatcher("/views/admin/admin_files.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String contentType = request.getContentType();

		if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
			handleUpload(request, response);
		} else {
			if (!CSRFUtil.isTokenValid(request)) {
				logger.warn("CSRF token validation failed for file management action.");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
				return;
			}
			String action = request.getParameter("action");
			if ("delete".equals(action)) {
				handleDelete(request, response);
			} else {
				logger.warn("Received non-multipart POST with unknown or missing action: '{}'", action);
				request.getSession().setAttribute("errorMessage", "Unbekannte Aktion empfangen.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
			}
		}
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");

		String categoryIdStr = null;
		String requiredRole = null;
		String csrfToken = null;
		Part filePart = null;

		try {
			for (Part part : request.getParts()) {
				if (part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
					// This is a form field
					String fieldName = part.getName();
					String fieldValue = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
					switch (fieldName) {
					case "csrfToken":
						csrfToken = fieldValue;
						break;
					case "categoryId":
						categoryIdStr = fieldValue;
						break;
					case "requiredRole":
						requiredRole = fieldValue;
						break;
					}
				} else {
					// This is the file part
					if (filePart == null) {
						filePart = part;
					}
				}
			}

			logger.debug(
					"Parsed from multipart form: categoryId='{}', requiredRole='{}', csrfToken (present)='{}', filePart (present)='{}'",
					categoryIdStr, requiredRole, csrfToken != null, filePart != null);

			if (!CSRFUtil.isTokenValid(session, csrfToken)) {
				logger.warn("CSRF token validation failed for file upload action.");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
				return;
			}

			if (categoryIdStr == null || categoryIdStr.trim().isEmpty() || "0".equals(categoryIdStr)) {
				logger.warn("File upload failed: No category was selected. categoryIdStr was '{}'.", categoryIdStr);
				session.setAttribute("errorMessage", "Es muss eine Kategorie ausgewählt werden.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
				return;
			}
			int categoryId = Integer.parseInt(categoryIdStr);

			if (filePart == null || filePart.getSize() == 0) {
				session.setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
				return;
			}

			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

			String uploadFilePath = AppConfig.UPLOAD_DIRECTORY;
			java.io.File uploadDir = new java.io.File(uploadFilePath);
			if (!uploadDir.exists())
				uploadDir.mkdirs();

			java.io.File targetFile = new java.io.File(uploadDir, fileName);
			filePart.write(targetFile.getAbsolutePath());
			logger.info("File '{}' uploaded by '{}' to: {}", fileName, adminUser.getUsername(),
					targetFile.getAbsolutePath());

			File newDbFile = new File();
			newDbFile.setFilename(fileName);
			newDbFile.setFilepath(fileName);
			newDbFile.setCategoryId(categoryId);
			newDbFile.setRequiredRole(requiredRole);

			if (fileDAO.createFile(newDbFile)) {
				String categoryName = fileDAO.getCategoryNameById(categoryId);
				String logDetails = String.format("Datei '%s' in Kategorie '%s' hochgeladen. Sichtbar für: %s.",
						fileName, categoryName, requiredRole);
				AdminLogService.log(adminUser.getUsername(), "FILE_UPLOAD", logDetails);
				session.setAttribute("successMessage", "Datei '" + fileName + "' erfolgreich hochgeladen.");
			} else {
				session.setAttribute("errorMessage",
						"DB-Fehler: Datei konnte nicht gespeichert werden (ggf. existiert der Name bereits).");
				targetFile.delete();
			}
		} catch (Exception e) {
			logger.error("File upload failed.", e);
			session.setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			logger.warn("Attempting to delete file with ID: {} by user '{}'", fileId, adminUser.getUsername());
			File dbFile = fileDAO.getFileById(fileId);

			if (dbFile != null) {
				java.io.File physicalFile = new java.io.File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());
				boolean physicalDeleted = true;

				if (physicalFile.exists()) {
					physicalDeleted = physicalFile.delete();
				} else {
					logger.warn("Physical file not found at [{}], but proceeding with DB record deletion.",
							physicalFile.getAbsolutePath());
				}

				if (physicalDeleted) {
					if (fileDAO.deleteFile(fileId)) {
						String categoryName = fileDAO.getCategoryNameById(dbFile.getCategoryId());
						String logDetails = String.format("Datei '%s' (ID: %d) aus Kategorie '%s' gelöscht.",
								dbFile.getFilename(), fileId, categoryName != null ? categoryName : "N/A");
						AdminLogService.log(adminUser.getUsername(), "FILE_DELETE", logDetails);
						request.getSession().setAttribute("successMessage",
								"Datei '" + dbFile.getFilename() + "' wurde erfolgreich gelöscht.");
					} else {
						request.getSession().setAttribute("errorMessage",
								"FEHLER: Die Datei konnte aus der Datenbank nicht gelöscht werden.");
					}
				} else {
					request.getSession().setAttribute("errorMessage",
							"FEHLER: Die physische Datei konnte nicht gelöscht werden. Bitte Berechtigungen prüfen.");
				}
			} else {
				request.getSession().setAttribute("errorMessage", "Datei in der Datenbank nicht gefunden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Datei-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}
}