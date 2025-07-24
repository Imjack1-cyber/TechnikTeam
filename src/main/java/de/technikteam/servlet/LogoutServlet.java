package de.technikteam.servlet;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Singleton
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session != null) {
			User user = (User) session.getAttribute("user");
			String username = (user != null) ? user.getUsername() : "Gast";

			logger.info("Logging out user: {}. Invalidating session.", username);
			session.invalidate();
		} else {
			logger.warn("LogoutServlet called but no active session found.");
		}

		HttpSession newSession = request.getSession(true);
		newSession.setAttribute("successMessage", "Sie wurden erfolgreich ausgeloggt.");
		response.sendRedirect(request.getContextPath() + "/login");
	}
}