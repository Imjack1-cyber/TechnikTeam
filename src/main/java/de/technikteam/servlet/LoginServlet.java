package de.technikteam.servlet;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);
	private UserDAO userDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		logger.info("Login attempt for username: {}", username);

		User user = userDAO.validateUser(username, password);

		if (user != null) {
			HttpSession session = request.getSession();
			session.setAttribute("user", user);

			List<NavigationItem> navigationItems = buildNavigationForUser(user.getPermissions());
			session.setAttribute("navigationItems", navigationItems);

			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRoleName());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			logger.warn("Login failed for username: {}. Invalid credentials.", username);
			request.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
		}
	}

	private List<NavigationItem> buildNavigationForUser(Set<String> permissions) {
		List<NavigationItem> allPossibleItems = new ArrayList<>();

		// User Section
		allPossibleItems.add(new NavigationItem("Dashboard", "/home", "fa-home", null));
		allPossibleItems.add(new NavigationItem("Lehrgänge", "/lehrgaenge", "fa-graduation-cap", null));
		allPossibleItems.add(new NavigationItem("Veranstaltungen", "/veranstaltungen", "fa-calendar-check", null));
		allPossibleItems.add(new NavigationItem("Lager", "/lager", "fa-boxes", null));
		allPossibleItems.add(new NavigationItem("Dateien", "/dateien", "fa-folder-open", null));
		allPossibleItems.add(new NavigationItem("Kalender", "/kalender", "fa-calendar-alt", null));

		// Admin Section
		allPossibleItems.add(
				new NavigationItem("Admin Dashboard", "/admin/dashboard", "fa-tachometer-alt", "ACCESS_ADMIN_PANEL"));
		allPossibleItems.add(new NavigationItem("Benutzer", "/admin/mitglieder", "fa-users-cog", "ACCESS_ADMIN_PANEL"));
		allPossibleItems
				.add(new NavigationItem("Events", "/admin/veranstaltungen", "fa-calendar-plus", "ACCESS_ADMIN_PANEL"));
		allPossibleItems.add(new NavigationItem("Lager", "/admin/lager", "fa-warehouse", "ACCESS_ADMIN_PANEL"));
		allPossibleItems.add(new NavigationItem("Dateien", "/admin/dateien", "fa-file-upload", "ACCESS_ADMIN_PANEL"));
		allPossibleItems
				.add(new NavigationItem("Lehrgangs-Vorlagen", "/admin/lehrgaenge", "fa-book", "ACCESS_ADMIN_PANEL"));
		allPossibleItems.add(new NavigationItem("Kit-Verwaltung", "/admin/kits", "fa-box-open", "ACCESS_ADMIN_PANEL")); // NEW
		allPossibleItems
				.add(new NavigationItem("Defekte Artikel", "/admin/defekte", "fa-wrench", "ACCESS_ADMIN_PANEL")); // NEW
		allPossibleItems.add(new NavigationItem("Quali-Matrix", "/admin/matrix", "fa-th-list", "ACCESS_ADMIN_PANEL"));
		allPossibleItems.add(new NavigationItem("Ressourcen-Planer", "/admin/resource-calendar", "fa-calendar-check",
				"ACCESS_ADMIN_PANEL")); // NEW
		allPossibleItems.add(new NavigationItem("Berichte", "/admin/berichte", "fa-chart-pie", "ACCESS_ADMIN_PANEL"));
		allPossibleItems
				.add(new NavigationItem("Aktions-Log", "/admin/log", "fa-clipboard-list", "ACCESS_ADMIN_PANEL")); // NEW
		allPossibleItems.add(new NavigationItem("System", "/admin/system", "fa-server", "ACCESS_ADMIN_PANEL"));

		List<NavigationItem> accessibleItems = new ArrayList<>();
		for (NavigationItem item : allPossibleItems) {
			if (item.getRequiredPermission() == null || permissions.contains(item.getRequiredPermission())) {
				accessibleItems.add(item);
			}
		}
		return accessibleItems;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}
}