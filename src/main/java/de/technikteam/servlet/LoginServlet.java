package de.technikteam.servlet;

import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import de.technikteam.dao.UserDAO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);
	private UserDAO userDAO;

	private static final int MAX_ATTEMPTS = 5;
	private static final long LOCKOUT_TIME_MS = 15 * 60 * 1000;
	private static final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
	private static final Map<String, Long> lockoutTimestamps = new ConcurrentHashMap<>();

	@Override
	public void init() {
		userDAO = new UserDAO();
	}

	private String sanitizeForLogging(String input) {
		if (input == null)
			return "";
		return input.replace('\n', '_').replace('\r', '_');
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String sanitizedUsername = sanitizeForLogging(username);
		HttpSession session = request.getSession();

		logger.info("Login attempt for username: {}", sanitizedUsername);

		if (isLockedOut(sanitizedUsername)) {
			logger.warn("Login attempt for locked-out user: {}", sanitizedUsername);
			session.setAttribute("errorMessage",
					"Ihr Konto ist aufgrund zu vieler fehlgeschlagener Versuche vorübergehend gesperrt.");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = userDAO.validateUser(username, password);

		if (user != null) {
			failedAttempts.remove(sanitizedUsername);
			lockoutTimestamps.remove(sanitizedUsername);

			session.setAttribute("user", user);

			CSRFUtil.storeToken(session);

			List<NavigationItem> navigationItems = buildNavigationForUser(user.getPermissions());
			session.setAttribute("navigationItems", navigationItems);

			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRoleName());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			handleFailedLogin(sanitizedUsername);
			session.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			response.sendRedirect(request.getContextPath() + "/login");
		}
	}

	private boolean isLockedOut(String username) {
		Long lockoutTime = lockoutTimestamps.get(username);
		if (lockoutTime == null) {
			return false;
		}
		if (System.currentTimeMillis() - lockoutTime > LOCKOUT_TIME_MS) {
			lockoutTimestamps.remove(username);
			failedAttempts.remove(username);
			return false;
		}
		return true;
	}

	private void handleFailedLogin(String username) {
		int attempts = failedAttempts.compute(username, (k, v) -> (v == null) ? 1 : v + 1);

		if (attempts >= MAX_ATTEMPTS) {
			logger.warn("Locking out user {} due to {} failed login attempts.", username, attempts);
			lockoutTimestamps.put(username, System.currentTimeMillis());
			failedAttempts.remove(username);
		}
	}

	/**
	 * Builds a filtered list of navigation items based on the user's permissions.
	 * 
	 * @param userPermissions The set of permissions for the current user.
	 * @return A list of NavigationItem objects the user is allowed to see.
	 */
	private List<NavigationItem> buildNavigationForUser(Set<String> userPermissions) {
		List<NavigationItem> allPossibleItems = new ArrayList<>();

		allPossibleItems.add(new NavigationItem("Dashboard", "/home", "fa-home", null));
		allPossibleItems.add(new NavigationItem("Lehrgänge", "/lehrgaenge", "fa-graduation-cap", null));
		allPossibleItems.add(new NavigationItem("Veranstaltungen", "/veranstaltungen", "fa-calendar-check", null));
		allPossibleItems.add(new NavigationItem("Lager", "/lager", "fa-boxes", null));
		allPossibleItems.add(new NavigationItem("Dateien", "/dateien", "fa-folder-open", null));
		allPossibleItems.add(new NavigationItem("Kalender", "/kalender", "fa-calendar-alt", null));
		allPossibleItems.add(new NavigationItem("Admin Dashboard", "/admin/dashboard", "fa-tachometer-alt",
				"ADMIN_DASHBOARD_ACCESS"));
		allPossibleItems.add(new NavigationItem("Benutzer", "/admin/mitglieder", "fa-users-cog", "USER_READ"));
		allPossibleItems.add(new NavigationItem("Events", "/admin/veranstaltungen", "fa-calendar-plus", "EVENT_READ"));
		allPossibleItems.add(new NavigationItem("Lager", "/admin/lager", "fa-warehouse", "STORAGE_READ"));
		allPossibleItems.add(new NavigationItem("Dateien", "/admin/dateien", "fa-file-upload", "FILE_READ"));
		allPossibleItems.add(new NavigationItem("Lehrgangs-Vorlagen", "/admin/lehrgaenge", "fa-book", "COURSE_READ"));
		allPossibleItems.add(new NavigationItem("Kit-Verwaltung", "/admin/kits", "fa-box-open", "KIT_READ"));
		allPossibleItems.add(new NavigationItem("Defekte Artikel", "/admin/defekte", "fa-wrench", "STORAGE_READ"));
		allPossibleItems.add(new NavigationItem("Quali-Matrix", "/admin/matrix", "fa-th-list", "QUALIFICATION_READ"));
		allPossibleItems.add(new NavigationItem("Berichte", "/admin/berichte", "fa-chart-pie", "REPORT_READ"));
		allPossibleItems.add(new NavigationItem("Aktions-Log", "/admin/log", "fa-clipboard-list", "LOG_READ"));
		allPossibleItems.add(new NavigationItem("System", "/admin/system", "fa-server", "SYSTEM_READ"));

		boolean hasMasterAccess = userPermissions.contains("ACCESS_ADMIN_PANEL");

		boolean hasAnyAdminPermission = userPermissions.stream()
				.anyMatch(p -> !"ACCESS_ADMIN_PANEL".equals(p) && (p.contains("_READ") || p.contains("_MANAGE")
						|| p.contains("_UPDATE") || p.contains("_CREATE") || p.contains("_DELETE")));

		return allPossibleItems.stream().filter(item -> {
			String requiredPerm = item.getRequiredPermission();
			if (requiredPerm == null) {
				return true;
			}
			if ("ADMIN_DASHBOARD_ACCESS".equals(requiredPerm)) {
				return hasMasterAccess || hasAnyAdminPermission;
			}
			return hasMasterAccess || userPermissions.contains(requiredPerm);
		}).collect(Collectors.toList());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}
}