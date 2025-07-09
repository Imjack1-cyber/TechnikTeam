package de.technikteam.servlet.admin;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This servlet is uniquely mapped to multiple URL patterns
 * (`/admin/categories/*`) to handle specific CRUD actions for file categories.
 * It processes POST requests to create, update, or delete a category based on
 * the servlet path, logs the action, and then redirects back to the main admin
 * file management page.
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
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");

		if (!CSRFUtil.isTokenValid(req)) {
			logger.warn("CSRF token validation failed for file category action.");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = req.getServletPath();
		User adminUser = (User) req.getSession().getAttribute("user");
		logger.debug("AdminFileCategoryServlet processing POST for action path: {}", action);

		try {
			if (action.endsWith("/erstellen")) {
				handleCreate(req, adminUser);
			} else if (action.endsWith("/aktualisieren")) {
				handleUpdate(req, adminUser);
			} else if (action.endsWith("/loeschen")) {
				handleDelete(req, adminUser);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in AdminFileCategoryServlet for action {}", action, e);
			req.getSession().setAttribute("errorMessage", "Ungültige ID für Kategorie-Aktion.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred in AdminFileCategoryServlet", e);
			req.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
		}

		resp.sendRedirect(req.getContextPath() + "/admin/dateien");
	}

	private void handleCreate(HttpServletRequest req, User adminUser) {
		String categoryName = req.getParameter("categoryName");
		if (categoryName == null || categoryName.trim().isEmpty()) {
			req.getSession().setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
			return;
		}
		if (fileDAO.createCategory(categoryName)) {
			AdminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY",
					"Dateikategorie '" + categoryName + "' erstellt.");
			req.getSession().setAttribute("successMessage", "Kategorie '" + categoryName + "' erfolgreich erstellt.");
		} else {
			req.getSession().setAttribute("errorMessage",
					"Kategorie konnte nicht erstellt werden. Möglicherweise existiert der Name bereits.");
		}
	}

	private void handleUpdate(HttpServletRequest req, User adminUser) {
		int categoryId = Integer.parseInt(req.getParameter("categoryId"));
		String newName = req.getParameter("categoryName");
		String oldName = fileDAO.getCategoryNameById(categoryId);

		if (newName == null || newName.trim().isEmpty()) {
			req.getSession().setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
			return;
		}

		if (fileDAO.updateCategory(categoryId, newName)) {
			AdminLogService.log(adminUser.getUsername(), "UPDATE_FILE_CATEGORY",
					"Dateikategorie '" + oldName + "' (ID: " + categoryId + ") umbenannt in '" + newName + "'.");
			req.getSession().setAttribute("successMessage", "Kategorie erfolgreich umbenannt.");
		} else {
			req.getSession().setAttribute("errorMessage", "Kategorie konnte nicht umbenannt werden.");
		}
	}

	private void handleDelete(HttpServletRequest req, User adminUser) {
		int categoryId = Integer.parseInt(req.getParameter("categoryId"));
		String categoryName = fileDAO.getCategoryNameById(categoryId);
		if (fileDAO.deleteCategory(categoryId)) {
			AdminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY",
					"Dateikategorie '" + categoryName + "' (ID: " + categoryId + ") gelöscht.");
			req.getSession().setAttribute("successMessage", "Kategorie erfolgreich gelöscht.");
		} else {
			req.getSession().setAttribute("errorMessage", "Kategorie konnte nicht gelöscht werden.");
		}
	}
}