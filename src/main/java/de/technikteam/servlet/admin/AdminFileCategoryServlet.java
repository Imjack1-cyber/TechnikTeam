package de.technikteam.servlet.admin;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * This servlet is uniquely mapped to multiple URL patterns to handle specific
 * CRUD actions for file categories. It processes POST requests to create,
 * update, or delete a category based on the servlet path, logs the action, and
 * then redirects back to the main admin file management page.
 */
@WebServlet({ "/admin/dateien/kategorien/erstellen", "/admin/dateien/kategorien/aktualisieren",
		"/admin/dateien/kategorien/loeschen" })
public class AdminFileCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileCategoryServlet.class);
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for file category action.");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String path = request.getServletPath();
		User adminUser = (User) session.getAttribute("user");
		logger.debug("AdminFileCategoryServlet processing POST for action path: {}", path);

		try {
			if (path.endsWith("/erstellen")) {
				handleCreate(request, response, adminUser);
			} else if (path.endsWith("/aktualisieren")) {
				handleUpdate(request, response, adminUser);
			} else if (path.endsWith("/loeschen")) {
				handleDelete(request, response, adminUser);
			} else {
				logger.warn("Unknown path received in AdminFileCategoryServlet: {}", path);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in AdminFileCategoryServlet for action {}", path, e);
			session.setAttribute("errorMessage", "Ungültige ID für Kategorie-Aktion.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
		} catch (Exception e) {
			logger.error("An unexpected error occurred in AdminFileCategoryServlet", e);
			session.setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
		}
	}

	/**
	 * Handles the creation of a new file category.
	 */
	private void handleCreate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		String categoryName = request.getParameter("categoryName");
		HttpSession session = request.getSession();

		if (categoryName == null || categoryName.trim().isEmpty()) {
			session.setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
		} else if (fileDAO.createCategory(categoryName)) {
			AdminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY",
					"Dateikategorie '" + categoryName + "' erstellt.");
			session.setAttribute("successMessage", "Kategorie '" + categoryName + "' erfolgreich erstellt.");
		} else {
			session.setAttribute("errorMessage",
					"Kategorie konnte nicht erstellt werden. Möglicherweise existiert der Name bereits.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	/**
	 * Handles the update of an existing file category's name.
	 */
	private void handleUpdate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String newName = request.getParameter("categoryName");
		String oldName = fileDAO.getCategoryNameById(categoryId);
		HttpSession session = request.getSession();

		if (newName == null || newName.trim().isEmpty()) {
			session.setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
		} else if (fileDAO.updateCategory(categoryId, newName)) {
			AdminLogService.log(adminUser.getUsername(), "UPDATE_FILE_CATEGORY",
					"Dateikategorie '" + oldName + "' (ID: " + categoryId + ") umbenannt in '" + newName + "'.");
			session.setAttribute("successMessage", "Kategorie erfolgreich umbenannt.");
		} else {
			session.setAttribute("errorMessage", "Kategorie konnte nicht umbenannt werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	/**
	 * Handles the deletion of a file category.
	 */
	private void handleDelete(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String categoryName = fileDAO.getCategoryNameById(categoryId);
		HttpSession session = request.getSession();

		if (fileDAO.deleteCategory(categoryId)) {
			AdminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY",
					"Dateikategorie '" + (categoryName != null ? categoryName : "ID: " + categoryId) + "' gelöscht.");
			session.setAttribute("successMessage", "Kategorie erfolgreich gelöscht.");
		} else {
			session.setAttribute("errorMessage",
					"Kategorie konnte nicht gelöscht werden. Stellen Sie sicher, dass keine untergeordneten Elemente vorhanden sind.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}
}