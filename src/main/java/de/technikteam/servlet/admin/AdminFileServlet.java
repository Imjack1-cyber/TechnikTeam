package de.technikteam.servlet.admin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.File; // Our own model: de.technikteam.model.File
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * Mapped to `/admin/files`, this servlet manages file uploads and deletions for
 * administrators. A GET request displays the management page
 * (`admin_files.jsp`) with a list of all files grouped by category. A POST
 * request handles either uploading a new file or deleting an existing one. It
 * correctly handles `multipart/form-data` to read form fields and the uploaded
 * file.
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
@WebServlet("/admin/files")
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
		request.getRequestDispatcher("/admin/admin_files.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String contentType = request.getContentType();

		// Differentiate between multipart (upload) and standard (delete) forms
		if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
			handleUpload(request, response);
		} else {
			String action = request.getParameter("action");
			if ("delete".equals(action)) {
				handleDelete(request, response);
			} else {
				logger.warn("Received non-multipart POST with unknown or missing action: '{}'", action);
				request.getSession().setAttribute("errorMessage", "Unbekannte Aktion empfangen.");
				response.sendRedirect(request.getContextPath() + "/admin/files");
			}
		}
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			String uploadFilePath = AppConfig.UPLOAD_DIRECTORY;
			java.io.File uploadDir = new java.io.File(uploadFilePath);
			if (!uploadDir.exists())
				uploadDir.mkdirs();

			Part filePart = request.getPart("file");
			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

			String requiredRole = getPartValue(request.getPart("requiredRole"));
			String categoryIdStr = getPartValue(request.getPart("categoryId"));

			int categoryId = 0;
			try {
				if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
					categoryId = Integer.parseInt(categoryIdStr);
				}
			} catch (NumberFormatException e) {
				logger.warn("No valid category ID provided during upload.");
			}

			if (fileName == null || fileName.isEmpty()) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
			} else if (categoryId == 0) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Kategorie aus.");
			} else {
				java.io.File targetFile = new java.io.File(uploadDir, fileName);
				filePart.write(targetFile.getAbsolutePath());
				logger.info("File '{}' uploaded by '{}' to: {}", fileName, adminUser.getUsername(),
						targetFile.getAbsolutePath());

				File newDbFile = new File();
				newDbFile.setFilename(fileName);
				newDbFile.setFilepath(fileName); // Filepath is just the filename for top-level uploads
				newDbFile.setCategoryId(categoryId);
				newDbFile.setRequiredRole(requiredRole);

				if (fileDAO.createFile(newDbFile)) {
					String categoryName = fileDAO.getCategoryNameById(categoryId);
					String logDetails = String.format("Datei '%s' in Kategorie '%s' hochgeladen. Sichtbar für: %s.",
							fileName, categoryName, requiredRole);
					AdminLogService.log(adminUser.getUsername(), "FILE_UPLOAD", logDetails);
					request.getSession().setAttribute("successMessage",
							"Datei '" + fileName + "' erfolgreich hochgeladen.");
				} else {
					request.getSession().setAttribute("errorMessage",
							"DB-Fehler: Datei konnte nicht gespeichert werden (ggf. existiert der Name bereits).");
					targetFile.delete();
				}
			}
		} catch (Exception e) {
			logger.error("File upload failed.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/files");
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
		response.sendRedirect(request.getContextPath() + "/admin/files");
	}

	private String getPartValue(Part part) throws IOException {
		if (part == null) {
			return null;
		}
		try (InputStream inputStream = part.getInputStream();
				Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
		}
	}
}