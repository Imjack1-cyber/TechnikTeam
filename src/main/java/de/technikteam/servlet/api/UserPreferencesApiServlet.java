package de.technikteam.servlet.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;

@Singleton
public class UserPreferencesApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(UserPreferencesApiServlet.class);
	private static final Set<String> VALID_THEMES = Set.of("light", "dark");
	private final UserDAO userDAO;

	@Inject
	public UserPreferencesApiServlet(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token.");
			return;
		}

		User user = (User) session.getAttribute("user");
		String theme = request.getParameter("theme");

		if (theme == null || !VALID_THEMES.contains(theme)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid theme value.");
			return;
		}

		if (userDAO.updateUserTheme(user.getId(), theme)) {
			user.setTheme(theme);
			session.setAttribute("user", user);
			logger.info("Updated theme for user '{}' to '{}'.", user.getUsername(), theme);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			logger.error("Failed to update theme for user '{}' in database.", user.getUsername());
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not save theme preference.");
		}
	}
}