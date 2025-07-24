package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class AdminFileCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileCategoryServlet.class);
	private final FileDAO fileDAO;
	private final AdminLogService adminLogService;

	@Inject
	public AdminFileCategoryServlet(FileDAO fileDAO, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.adminLogService = adminLogService;
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

		String pathInfo = request.getPathInfo(); // E.g., "/erstellen", "/loeschen"
		User adminUser = (User) session.getAttribute("user");
		logger.debug("AdminFileCategoryServlet processing POST for action path: {}", pathInfo);

		try {
			if ("/erstellen".equals(pathInfo)) {
				handleCreate(request, response, adminUser);
			} else if ("/aktualisieren".equals(pathInfo)) {
				handleUpdate(request, response, adminUser);
			} else if ("/loeschen".equals(pathInfo)) {
				handleDelete(request, response, adminUser);
			} else {
				logger.warn("Unknown path received in AdminFileCategoryServlet: {}", request.getRequestURI());
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID format in AdminFileCategoryServlet for action {}", pathInfo, e);
			session.setAttribute("errorMessage", "Ungültige ID für Kategorie-Aktion.");
			response.sendRedirect(request.getContextPath() + "/admin/dateien");
		}
	}

	private void handleCreate(HttpServletRequest request, HttpServletResponse response, User adminUser)
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

	private void handleUpdate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String newName = request.getParameter("categoryName");
		String oldName = fileDAO.getCategoryNameById(categoryId);
		HttpSession session = request.getSession();
		if (newName == null || newName.trim().isEmpty()) {
			session.setAttribute("errorMessage", "Kategoriename darf nicht leer sein.");
		} else if (fileDAO.updateCategory(categoryId, newName)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_FILE_CATEGORY",
					"Dateikategorie '" + oldName + "' (ID: " + categoryId + ") umbenannt in '" + newName + "'.");
			session.setAttribute("successMessage", "Kategorie erfolgreich umbenannt.");
		} else {
			session.setAttribute("errorMessage", "Kategorie konnte nicht umbenannt werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}

	private void handleDelete(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException {
		int categoryId = Integer.parseInt(request.getParameter("categoryId"));
		String categoryName = fileDAO.getCategoryNameById(categoryId);
		HttpSession session = request.getSession();
		if (fileDAO.deleteCategory(categoryId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY",
					"Dateikategorie '" + (categoryName != null ? categoryName : "ID: " + categoryId) + "' gelöscht.");
			session.setAttribute("successMessage", "Kategorie erfolgreich gelöscht.");
		} else {
			session.setAttribute("errorMessage",
					"Kategorie konnte nicht gelöscht werden. Stellen Sie sicher, dass keine untergeordneten Elemente vorhanden sind.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/dateien");
	}
}