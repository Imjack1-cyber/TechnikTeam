package de.technikteam.servlet;

import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import de.technikteam.util.NavigationRegistry;
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

		logger.info("Login attempt for username: {}", sanitizedUsername);

		if (isLockedOut(sanitizedUsername)) {
			logger.warn("Login attempt for locked-out user: {}", sanitizedUsername);
			request.getSession().setAttribute("errorMessage",
					"Ihr Konto ist aufgrund zu vieler fehlgeschlagener Versuche vorübergehend gesperrt.");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = userDAO.validateUser(username, password);

		if (user != null) {
			failedAttempts.remove(sanitizedUsername);
			lockoutTimestamps.remove(sanitizedUsername);

			// Invalidate any old session to prevent session fixation
			HttpSession oldSession = request.getSession(false);
			if (oldSession != null) {
				oldSession.invalidate();
			}

			// Create a new session for the authenticated user
			HttpSession newSession = request.getSession(true);
			newSession.setAttribute("user", user);

			CSRFUtil.storeToken(newSession);

			List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
			newSession.setAttribute("navigationItems", navigationItems);

			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRoleName());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			handleFailedLogin(sanitizedUsername);
			// Get or create a session to store the error message
			HttpSession session = request.getSession(true);
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

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}
}