package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.ConfigurationService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A comprehensive servlet for managing all administrative aspects of files and
 * categories. This single servlet acts as the controller for the
 * `/admin/dateien` endpoint.
 * <p>
 * - The {@code doGet} method handles the display of the file management page. -
 * The {@code doPost} method is a router that handles various actions,
 * including: - Multipart file uploads (create, update). - Standard form
 * submissions for managing categories and file metadata (delete, reassign).
 * <p>
 * This servlet's multipart configuration is defined in `web.xml` to ensure
 * compatibility with the Guice-managed servlet lifecycle, which is the key to
 * solving previous issues.
 */
@Singleton
@MultipartConfig
public class AdminFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileServlet.class);
	private final FileDAO fileDAO;
	private final ConfigurationService configService;
	private final AdminLogService adminLogService;

	private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif",
			"application/pdf", "text/markdown", "text/plain", "application/vnd.oasis.opendocument.text");

	@Inject
	public AdminFileServlet(FileDAO fileDAO, ConfigurationService configService, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.configService = configService;
		this.adminLogService = adminLogService;
	}

	/**
	 * Handles GET requests to display the main file management page. It fetches all
	 * files and categories for an administrative view.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		logger.info("Admin file management page requested by user '{}' (Role: {})", user.getUsername(),
				user.getRoleName());

		User adminProxy = new User();
		adminProxy.setPermissions(new HashSet<>());
		adminProxy.getPermissions().add("ACCESS_ADMIN_PANEL");

		Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(adminProxy);
		List<de.technikteam.model.FileCategory> allCategories = fileDAO.getAllCategories();

		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("allCategories", allCategories);

		logger.debug("Forwarding file and category data to admin_files.jsp.");
		request.getRequestDispatcher("/views/admin/admin_files.jsp").forward(request, response);
	}

	/**
	 * Handles POST requests for all file and category management actions. With the
	 * correct web.xml configuration, request.getParameter() now works reliably for
	 * all form fields, regardless of the request's content type, making the CSRF
	 * check simple and robust.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");
		logger.debug("AdminFileServlet processing POST for action: {}", action);

		switch (action) {
		case "create":
			handleCreateUpload(request, response, adminUser);
			break;
		case "update":
			handleUpdateUpload(request, response, adminUser);
			break;
		case "delete":
			handleDelete(request, response, adminUser);
			break;
		case "reassign":
			handleReassign(request, response, adminUser);
			break;
		case "createCategory":
			handleCreateCategory(request, response, adminUser);
			break;
		case "deleteCategory":
			handleDeleteCategory(request, response, adminUser);
			break;
		default:
			session.setAttribute("errorMessage", "Unbekannte Aktion: " + action);
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			break;
		}
	}

	private void handleCreateUpload(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException, ServletException {
		HttpSession session = request.getSession();
		Part filePart = request.getPart("file");

		if (filePart == null || filePart.getSize() == 0) {
			session.setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		String contentType = filePart.getContentType();
		if (!ALLOWED_MIME_TYPES.contains(contentType)) {
			session.setAttribute("errorMessage", "Dateityp '" + contentType + "' ist nicht erlaubt.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		try {
			int categoryId = Integer.parseInt(request.getParameter("categoryId"));
			String requiredRole = request.getParameter("requiredRole");
			String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String sanitizedOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
			String uniqueFileName = UUID.randomUUID().toString() + "-" + sanitizedOriginalFileName;
			File targetFile = new File(configService.getProperty("upload.directory"), uniqueFileName);

			filePart.write(targetFile.getAbsolutePath());

			de.technikteam.model.File newDbFile = new de.technikteam.model.File();
			newDbFile.setFilename(sanitizedOriginalFileName);
			newDbFile.setFilepath(uniqueFileName);
			newDbFile.setCategoryId(categoryId);
			newDbFile.setRequiredRole(requiredRole);

			if (fileDAO.createFile(newDbFile)) {
				String categoryName = fileDAO.getCategoryNameById(categoryId);
				adminLogService.log(adminUser.getUsername(), "FILE_UPLOAD", "Datei '" + sanitizedOriginalFileName
						+ "' in Kategorie '" + categoryName + "' hochgeladen. Sichtbar für: " + requiredRole + ".");
				session.setAttribute("successMessage", "Datei erfolgreich hochgeladen.");
			} else {
				targetFile.delete();
				session.setAttribute("errorMessage", "DB-Fehler: Datei konnte nicht gespeichert werden.");
			}
		} catch (Exception e) {
			logger.error("Error during file creation upload.", e);
			session.setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleUpdateUpload(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException, ServletException {
		HttpSession session = request.getSession();
		Part filePart = request.getPart("file");

		if (filePart == null || filePart.getSize() == 0) {
			session.setAttribute("errorMessage", "Bitte wählen Sie eine Datei zum Hochladen aus.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
			return;
		}

		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);
			if (dbFile == null) {
				session.setAttribute("errorMessage", "Datei zum Aktualisieren nicht gefunden.");
				response.sendRedirect(request.getContextPath() + "/admin/dateien");
				return;
			}

			File targetFile = new File(configService.getProperty("upload.directory"), dbFile.getFilepath());
			filePart.write(targetFile.getAbsolutePath());

			if (fileDAO.touchFileRecord(dbFile.getId())) {
				adminLogService.log(adminUser.getUsername(), "FILE_UPDATE",
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

	private void handleDelete(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		HttpSession session = request.getSession();
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			de.technikteam.model.File fileToDelete = fileDAO.getFileById(fileId);
			if (fileToDelete == null) {
				session.setAttribute("errorMessage", "Datei zum Löschen nicht gefunden.");
			} else if (fileDAO.deleteFile(fileId)) {
				adminLogService.log(adminUser.getUsername(), "FILE_DELETE", "Datei '" + fileToDelete.getFilename()
						+ "' (ID: " + fileId + ") aus Kategorie '" + fileToDelete.getCategoryName() + "' gelöscht.");
				session.setAttribute("successMessage", "Datei '" + fileToDelete.getFilename() + "' wurde gelöscht.");
			} else {
				session.setAttribute("errorMessage", "Datei konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMessage", "Ungültige Datei-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleReassign(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		HttpSession session = request.getSession();
		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			int newCategoryId = Integer.parseInt(request.getParameter("newCategoryId"));

			de.technikteam.model.File file = fileDAO.getFileById(fileId);
			if (file == null) {
				session.setAttribute("errorMessage", "Datei nicht gefunden.");
			} else if (fileDAO.reassignFileToCategory(fileId, newCategoryId)) {
				adminLogService.log(adminUser.getUsername(), "FILE_REASSIGN", "Datei '" + file.getFilename() + "' (ID: "
						+ fileId + ") wurde in Kategorie ID " + newCategoryId + " verschoben.");
				session.setAttribute("successMessage", "Datei wurde erfolgreich neu zugeordnet.");
			} else {
				session.setAttribute("errorMessage", "Datei konnte nicht neu zugeordnet werden.");
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMessage", "Ungültige ID-Angabe.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleCreateCategory(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		String categoryName = request.getParameter("categoryName");
		HttpSession session = request.getSession();
		if (categoryName == null || categoryName.trim().isEmpty()) {
			session.setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
		} else if (fileDAO.createCategory(categoryName)) {
			adminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY",
					"Dateikategorie '" + categoryName + "' erstellt.");
			session.setAttribute("successMessage", "Kategorie '" + categoryName + "' erfolgreich erstellt.");
		} else {
			session.setAttribute("errorMessage",
					"Kategorie konnte nicht erstellt werden. Möglicherweise existiert der Name bereits.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleDeleteCategory(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		HttpSession session = request.getSession();
		try {
			int categoryId = Integer.parseInt(request.getParameter("categoryId"));
			String categoryName = fileDAO.getCategoryNameById(categoryId);
			if (fileDAO.deleteCategory(categoryId)) {
				adminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY", "Dateikategorie '"
						+ (categoryName != null ? categoryName : "ID: " + categoryId) + "' gelöscht.");
				session.setAttribute("successMessage", "Kategorie erfolgreich gelöscht.");
			} else {
				session.setAttribute("errorMessage", "Kategorie konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			session.setAttribute("errorMessage", "Ungültige Kategorie-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}
}