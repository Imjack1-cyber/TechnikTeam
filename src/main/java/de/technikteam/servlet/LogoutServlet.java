package de.technikteam.servlet;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

		request.setAttribute("username", username);
		request.getRequestDispatcher("/logout").forward(request, response);
	}
}