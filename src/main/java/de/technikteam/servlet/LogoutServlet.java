package de.technikteam.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Mapped to `/logout`, this servlet handles the user logout process. It
 * invalidates the current session, effectively logging the user out and
 * clearing all session attributes. It then redirects the user to a `logout.jsp`
 * confirmation page, passing the username as a parameter for a personalized
 * message.
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LogoutServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		String username = "Gast";

		if (session != null) {
			if (session.getAttribute("username") != null) {
				username = (String) session.getAttribute("username");
			}
			logger.info("Logging out user: {}. Invalidating session.", username);
			session.invalidate();
		} else {
			logger.warn("Logoutservlet called but no active session found.");
		}

		// Redirect to the logout page with the username as a URL parameter
		response.sendRedirect("logout.jsp?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.toString()));
	}
}