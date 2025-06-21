package de.technikteam.servlet;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/*
 * Mapped to /login, this servlet is central to the application's authentication. It handles POST requests with username and password, validates them against the database via UserDAO, and if successful, creates a user session. It also handles GET requests to simply display the login.jsp page.
 */

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LoginServlet.class);
	private UserDAO userDAO;

	public void init() {
		userDAO = new UserDAO();
		logger.info("LoginServlet initialized.");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		logger.info("Login attempt for username: {}", username);

		User user = userDAO.validateUser(username, password);

		if (user != null) {
			HttpSession session = request.getSession();
			session.setAttribute("user", user);
			session.setAttribute("username", user.getUsername());
			session.setAttribute("role", user.getRole());
			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRole());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			logger.warn("Login failed for username: {}.", username);
			logger.warn("Password given: {}.", password);
			request.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("GET request received. Forwarding to login.jsp.");
		request.getRequestDispatcher("login.jsp").forward(request, response);
	}
}