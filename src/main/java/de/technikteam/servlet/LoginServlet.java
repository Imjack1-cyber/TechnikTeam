package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.NavigationItem;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import de.technikteam.util.NavigationRegistry;
import jakarta.servlet.ServletException;
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

@Singleton
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);
	private final UserDAO userDAO;

	public static class LoginAttemptManager {
		private static final int MAX_ATTEMPTS = 5;
		private static final long[] LOCKOUT_DURATIONS_MS = { TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(2),
				TimeUnit.MINUTES.toMillis(5), TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(30) };
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
			if (lockoutTime == null)
				return false;
			int currentLevel = lockoutLevel.getOrDefault(username, 0);
			long duration = LOCKOUT_DURATIONS_MS[Math.min(currentLevel, LOCKOUT_DURATIONS_MS.length - 1)];
			return System.currentTimeMillis() - lockoutTime <= duration;
		}

		public static void recordFailedLogin(String username) {
			int attempts = failedAttempts.compute(username, (k, v) -> (v == null) ? 1 : v + 1);
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
		}
	}

	@Inject
	public LoginServlet(UserDAO userDAO) {
		this.userDAO = userDAO;
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
			session.setAttribute("errorMessage",
					"Ihr Konto ist aufgrund zu vieler fehlgeschlagener Versuche vorübergehend gesperrt.");
			session.setAttribute("lockoutEndTime", LoginAttemptManager.getLockoutEndTime(sanitizedUsername));
			session.setAttribute("lockoutLevel", LoginAttemptManager.getLockoutLevel(sanitizedUsername));
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
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			LoginAttemptManager.recordFailedLogin(sanitizedUsername);
			session.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			session.setAttribute("failedUsername", username);
			if (LoginAttemptManager.isLockedOut(sanitizedUsername)) {
				session.setAttribute("lockoutEndTime", LoginAttemptManager.getLockoutEndTime(sanitizedUsername));
				session.setAttribute("lockoutLevel", LoginAttemptManager.getLockoutLevel(sanitizedUsername));
			}
			response.sendRedirect(request.getContextPath() + "/login");
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
	}
}