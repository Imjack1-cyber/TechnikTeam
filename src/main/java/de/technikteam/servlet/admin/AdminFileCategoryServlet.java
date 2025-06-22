package de.technikteam.servlet.admin;

import java.io.IOException;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
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
@WebServlet({ "/admin/categories/create", "/admin/categories/update", "/admin/categories/delete" })
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
		String action = req.getServletPath();
		User adminUser = (User) req.getSession().getAttribute("user");
		logger.debug("AdminFileCategoryServlet processing POST for action path: {}", action);

		try {
			if (action.endsWith("/create")) {
				String categoryName = req.getParameter("categoryName");
				if (fileDAO.createCategory(categoryName)) {
					AdminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY",
							"Dateikategorie '" + categoryName + "' erstellt.");
					req.getSession().setAttribute("successMessage",
							"Kategorie '" + categoryName + "' erfolgreich erstellt.");
				}
			} else if (action.endsWith("/update")) {
				int categoryId = Integer.parseInt(req.getParameter("categoryId"));
				String newName = req.getParameter("categoryName");
				String oldName = fileDAO.getCategoryNameById(categoryId); // Get old name for logging
				if (fileDAO.updateCategory(categoryId, newName)) {
					AdminLogService.log(adminUser.getUsername(), "UPDATE_FILE_CATEGORY", "Dateikategorie '" + oldName
							+ "' (ID: " + categoryId + ") umbenannt in '" + newName + "'.");
					req.getSession().setAttribute("successMessage", "Kategorie erfolgreich umbenannt.");
				}
			} else if (action.endsWith("/delete")) {
				int categoryId = Integer.parseInt(req.getParameter("categoryId"));
				String categoryName = fileDAO.getCategoryNameById(categoryId); // Get name for logging
				if (fileDAO.deleteCategory(categoryId)) {
					AdminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY",
							"Dateikategorie '" + categoryName + "' (ID: " + categoryId + ") gelöscht.");
					req.getSession().setAttribute("successMessage", "Kategorie erfolgreich gelöscht.");
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in AdminFileCategoryServlet for action {}", action, e);
			req.getSession().setAttribute("errorMessage", "Ungültige ID für Kategorie-Aktion.");
		}

		resp.sendRedirect(req.getContextPath() + "/admin/files");
	}
}