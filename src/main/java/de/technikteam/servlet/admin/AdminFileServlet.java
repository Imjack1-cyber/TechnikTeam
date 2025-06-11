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

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
@WebServlet("/admin/files")
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

		// Daten für die beiden Haupt-Blöcke der Seite laden
		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory();
		List<FileCategory> allCategories = fileDAO.getAllCategories(); // Liste für das Dropdown

		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("allCategories", allCategories); // An JSP übergeben

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

			// Die categoryId als Zahl aus dem Formular lesen
			int categoryId = 0;
			try {
				categoryId = Integer.parseInt(request.getParameter("categoryId"));
			} catch (NumberFormatException e) {
				logger.warn("No valid category ID provided.");
			}

			if (fileName == null || fileName.isEmpty()) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
			} else if (categoryId == 0) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Kategorie aus.");
			} else {
				java.io.File targetFile = new java.io.File(uploadDir, fileName);
				filePart.write(targetFile.getAbsolutePath());
				logger.info("File uploaded to: {}", targetFile.getAbsolutePath());

				// Das File-Objekt mit den korrekten Daten erstellen
				File newDbFile = new File();
				newDbFile.setFilename(fileName);
				newDbFile.setFilepath(fileName); // Nur der Dateiname für den Download-Servlet
				newDbFile.setCategoryId(categoryId); // Die ID setzen, nicht den String

				if (fileDAO.createFile(newDbFile)) {
					request.getSession().setAttribute("successMessage",
							"Datei '" + fileName + "' erfolgreich hochgeladen.");
				} else {
					request.getSession().setAttribute("errorMessage",
							"DB-Fehler: Eine Datei mit diesem Namen existiert bereits.");
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