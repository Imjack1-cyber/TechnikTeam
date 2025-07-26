// src/main/java/de/technikteam/servlet/HomeServlet.java
package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(HomeServlet.class);

	@Inject
	public HomeServlet() {
		// Dependencies are no longer needed here
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		if (user == null) {
			logger.warn("HomeServlet accessed without an authenticated user in the session. Redirecting to login.");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// The servlet now only forwards to the JSP shell.
		// The home.js script will fetch and render all data.
		logger.debug("Forwarding user '{}' to home.jsp shell.", user.getUsername());
		request.getRequestDispatcher("/views/public/home.jsp").forward(request, response);
	}
}