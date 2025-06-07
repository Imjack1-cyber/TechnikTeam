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

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File; // Unser eigenes Model: de.technikteam.model.File

/**
 * Servlet for handling file management by administrators. Supports displaying,
 * uploading, and deleting files. The @MultipartConfig annotation is crucial for
 * handling file uploads.
 */
@WebServlet("/admin/files")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1 MB: files smaller than this are stored in memory
		maxFileSize = 1024 * 1024 * 20, // 20 MB: maximum size of a single file
		maxRequestSize = 1024 * 1024 * 50 // 50 MB: maximum size of the total request
)
public class AdminFileServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(AdminFileServlet.class);
	private static final String UPLOAD_DIR = "uploads"; // Directory within the webapp to store files
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	/**
	 * Handles GET requests. Displays the file management page with a list of
	 * existing files and the upload form.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.debug("GET request for AdminFileServlet received. Forwarding to file management page.");

		// Fetch all files grouped by category to display them
		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory();

		request.setAttribute("groupedFiles", groupedFiles);
		request.getRequestDispatcher("/admin/admin_files.jsp").forward(request, response);
	}

	/**
	 * Handles POST requests. Differentiates between 'upload' and 'delete' actions.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");

		// The default action if none is specified is "upload"
		if ("delete".equals(action)) {
			handleDelete(request, response);
		} else {
			handleUpload(request, response);
		}
	}

	/**
	 * Handles the file upload logic.
	 */
	private void handleUpload(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			// Get the absolute path of the web application
			String applicationPath = request.getServletContext().getRealPath("");
			String uploadFilePath = applicationPath + java.io.File.separator + UPLOAD_DIR;

			// Create the upload directory if it does not exist
			java.io.File uploadDir = new java.io.File(uploadFilePath);
			if (!uploadDir.exists()) {
				uploadDir.mkdirs();
			}

			Part filePart = request.getPart("file");
			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // Sanitize filename
			String category = request.getParameter("category");

			if (fileName == null || fileName.isEmpty()) {
				request.getSession().setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
				response.sendRedirect(request.getContextPath() + "/admin/files");
				return;
			}

			// Write the file to the server's filesystem
			filePart.write(uploadFilePath + java.io.File.separator + fileName);
			logger.info("Successfully uploaded file: {}", fileName);

			// Create a new File model object to save metadata to the database
			File newDbFile = new File();
			newDbFile.setFilename(fileName);
			newDbFile.setFilepath(UPLOAD_DIR + "/" + fileName); // Relative path for web access
			newDbFile.setCategory(category);

			if (fileDAO.createFile(newDbFile)) {
				request.getSession().setAttribute("successMessage",
						"Datei '" + fileName + "' erfolgreich hochgeladen und in der Datenbank gespeichert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Datei hochgeladen, aber der Datenbankeintrag ist fehlgeschlagen. Möglicherweise existiert der Dateipfad bereits.");
			}

		} catch (Exception e) {
			logger.error("File upload failed.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Datei-Upload: " + e.getMessage());
		}

		response.sendRedirect(request.getContextPath() + "/admin/files");
	}

	/**
	 * Handles the file deletion logic.
	 */
	private void handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));

			// First, get the file metadata from the database to know its path
			File dbFile = fileDAO.getFileById(fileId);

			if (dbFile != null) {
				// Construct the absolute path to the physical file
				String applicationPath = request.getServletContext().getRealPath("");
				java.io.File physicalFile = new java.io.File(
						applicationPath + java.io.File.separator + dbFile.getFilepath());

				// Attempt to delete the physical file
				if (physicalFile.delete()) {
					logger.warn("Successfully deleted physical file: {}", physicalFile.getAbsolutePath());
					// If physical deletion is successful, delete the database record
					fileDAO.deleteFile(fileId);
					request.getSession().setAttribute("successMessage", "Datei '" + dbFile.getFilename()
							+ "' wurde erfolgreich vom Server und aus der Datenbank gelöscht.");
				} else {
					// Handle cases where the physical file does not exist but the DB record does
					if (!physicalFile.exists()) {
						logger.warn("Physical file not found, but deleting database record anyway for file: {}",
								dbFile.getFilename());
						fileDAO.deleteFile(fileId);
						request.getSession().setAttribute("successMessage",
								"Datei-Eintrag aus der Datenbank entfernt (physische Datei wurde auf dem Server nicht gefunden).");
					} else {
						request.getSession().setAttribute("errorMessage",
								"Die physische Datei konnte nicht gelöscht werden. Überprüfen Sie die Dateiberechtigungen auf dem Server.");
					}
				}
			} else {
				request.getSession().setAttribute("errorMessage", "Datei-Eintrag in der Datenbank nicht gefunden.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid file ID for deletion.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Datei-ID.");
		}

		response.sendRedirect(request.getContextPath() + "/admin/files");
	}
}