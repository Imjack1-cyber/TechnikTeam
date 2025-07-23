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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.technikteam.dao.UserDAO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);
	private UserDAO userDAO;

	// Manages login throttling and lockout state.
	public static class LoginAttemptManager {
		private static final int MAX_ATTEMPTS = 5;
		private static final long[] LOCKOUT_DURATIONS_MS = { TimeUnit.MINUTES.toMillis(1), // 1 minute
				TimeUnit.MINUTES.toMillis(2), // 2 minutes
				TimeUnit.MINUTES.toMillis(5), // 5 minutes
				TimeUnit.MINUTES.toMillis(10), // 10 minutes
				TimeUnit.MINUTES.toMillis(30) // 30 minutes
		};

		private static final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
		private static final Map<String, Long> lockoutTimestamps = new ConcurrentHashMap<>();
		private static final Map<String, Integer> lockoutLevel = new ConcurrentHashMap<>();

		public static long getLockoutEndTime(String username) {
			return lockoutTimestamps.getOrDefault(username, 0L);
		}

		public static int getLockoutLevel(String username) {
			return lockoutLevel.getOrDefault(username, 0);
		}

		public static boolean isLockedOut(String username) {
			Long lockoutTime = lockoutTimestamps.get(username);
			if (lockoutTime == null) {
				return false;
			}
			int currentLevel = lockoutLevel.getOrDefault(username, 0);
			long duration = LOCKOUT_DURATIONS_MS[Math.min(currentLevel, LOCKOUT_DURATIONS_MS.length - 1)];

			if (System.currentTimeMillis() - lockoutTime > duration) {
				// No need to clear here, a successful login will do that.
				return false;
			}
			return true;
		}

		public static void recordFailedLogin(String username) {
			int attempts = failedAttempts.compute(username, (k, v) -> (v == null) ? 1 : v + 1);
			logger.warn("Failed login attempt #{} for user: {}", attempts, username);

			if (attempts >= MAX_ATTEMPTS) {
				int currentLevel = lockoutLevel.compute(username, (k, v) -> (v == null) ? 0 : v + 1);
				long duration = LOCKOUT_DURATIONS_MS[Math.min(currentLevel, LOCKOUT_DURATIONS_MS.length - 1)];
				logger.warn("Locking out user {} for {} minutes due to {} failed login attempts.", username,
						TimeUnit.MILLISECONDS.toMinutes(duration), attempts);
				lockoutTimestamps.put(username, System.currentTimeMillis());
				failedAttempts.remove(username);
			}
		}

		public static void clearLoginAttempts(String username) {
			failedAttempts.remove(username);
			lockoutTimestamps.remove(username);
			lockoutLevel.remove(username);
			logger.info("Login throttling state cleared for user: {}", username);
		}
	}

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
		HttpSession session = request.getSession(true);

		if (LoginAttemptManager.isLockedOut(sanitizedUsername)) {
			long endTime = LoginAttemptManager.getLockoutEndTime(sanitizedUsername);
			int level = LoginAttemptManager.getLockoutLevel(sanitizedUsername);
			session.setAttribute("errorMessage",
					"Ihr Konto ist aufgrund zu vieler fehlgeschlagener Versuche vorübergehend gesperrt.");
			session.setAttribute("lockoutEndTime", endTime);
			session.setAttribute("lockoutLevel", level);
			session.setAttribute("failedUsername", username);
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		User user = userDAO.validateUser(username, password);

		if (user != null) {
			LoginAttemptManager.clearLoginAttempts(sanitizedUsername);

			session.invalidate();
			HttpSession newSession = request.getSession(true);
			newSession.setAttribute("user", user);

			CSRFUtil.storeToken(newSession);

			List<NavigationItem> navigationItems = NavigationRegistry.getNavigationItemsForUser(user);
			newSession.setAttribute("navigationItems", navigationItems);

			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRoleName());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			handleFailedLogin(sanitizedUsername, request); // FIX: Pass the request object
			session.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			session.setAttribute("failedUsername", username); // Keep username in field
			response.sendRedirect(request.getContextPath() + "/login");
		}
	}

	private void handleFailedLogin(String username, HttpServletRequest request) { // FIX: Accept the request object
		LoginAttemptManager.recordFailedLogin(username);
		if (LoginAttemptManager.isLockedOut(username)) {
			HttpSession session = request.getSession();
			session.setAttribute("lockoutEndTime", LoginAttemptManager.getLockoutEndTime(username));
			session.setAttribute("lockoutLevel", LoginAttemptManager.getLockoutLevel(username));
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}
}