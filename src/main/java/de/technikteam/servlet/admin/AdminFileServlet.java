package de.technikteam.servlet.admin;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.FileCategoryDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.File; // Our own model: de.technikteam.model.File
import de.technikteam.model.FileCategory;

@WebServlet("/admin/files")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
public class AdminFileServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileServlet.class);
	private FileDAO fileDAO;
	private FileCategoryDAO fileCategoryDAO; // DAO für Kategorien hinzufügen

	@Override
	public void init() {
		fileDAO = new FileDAO();
		fileCategoryDAO = new FileCategoryDAO(); // Initialisieren
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.debug("GET request for AdminFileServlet received. Forwarding to file management page.");

		// 1. Alle Dateien für die Liste holen
		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory();

		// 2. Alle Kategorien für das Dropdown-Menü holen
		List<FileCategory> categories = fileCategoryDAO.getAll();

		// 3. Beide Daten an die JSP übergeben
		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("categories", categories);

		request.getRequestDispatcher("/admin/admin_files.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if ("delete".equals(action)) {
			handleDelete(request, response);
		} else {
			handleUpload(request, response);
		}
	}

	private void handleUpload(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			String uploadFilePath = AppConfig.UPLOAD_DIRECTORY;
			java.io.File uploadDir = new java.io.File(uploadFilePath);
			if (!uploadDir.exists())
				uploadDir.mkdirs();

			Part filePart = request.getPart("file");
			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String category = request.getParameter("category");

			if (fileName.isEmpty()) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Datei aus.");
			} else if (category == null || category.isEmpty()) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Kategorie aus.");
			} else {
				java.io.File targetFile = new java.io.File(uploadDir, fileName);
				filePart.write(targetFile.getAbsolutePath());
				logger.info("File uploaded to: {}", targetFile.getAbsolutePath());

				File newDbFile = new File();
				newDbFile.setFilename(fileName);
				newDbFile.setFilepath(fileName); // Store only the filename in DB
				newDbFile.setCategory(category);

				if (!fileDAO.createFile(newDbFile)) {
					request.getSession().setAttribute("errorMessage", "DB-Fehler: Datei existiert bereits.");
					targetFile.delete(); // Delete the uploaded file if DB insert fails
				} else {
					request.getSession().setAttribute("successMessage",
							"Datei '" + fileName + "' erfolgreich hochgeladen.");
				}
			}
		} catch (Exception e) {
			logger.error("File upload failed.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/files");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			File dbFile = fileDAO.getFileById(fileId);

			if (dbFile != null) {
				java.io.File physicalFile = new java.io.File(AppConfig.UPLOAD_DIRECTORY, dbFile.getFilepath());

				if (physicalFile.exists()) {
					if (physicalFile.delete()) {
						fileDAO.deleteFile(fileId);
						request.getSession().setAttribute("successMessage",
								"Datei '" + dbFile.getFilename() + "' wurde erfolgreich gelöscht.");
					} else {
						request.getSession().setAttribute("errorMessage",
								"FEHLER: Die physische Datei konnte nicht gelöscht werden.");
					}
				} else {
					logger.warn("Physical file not found at [{}], deleting orphan DB record.",
							physicalFile.getAbsolutePath());
					fileDAO.deleteFile(fileId);
					request.getSession().setAttribute("successMessage",
							"Datenbankeintrag wurde entfernt (physische Datei nicht gefunden).");
				}
			} else {
				request.getSession().setAttribute("errorMessage", "Datei in der Datenbank nicht gefunden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Ungültige Datei-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/files");
	}
}