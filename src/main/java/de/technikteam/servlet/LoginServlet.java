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

/**
 * Mapped to `/login`, this servlet is central to the application's
 * authentication. It handles GET requests to simply display the `login.jsp`
 * page. It handles POST requests by taking a username and password, validating
 * them against the database via `UserDAO`, and creating a user session upon
 * successful authentication. If authentication fails, it forwards back to the
 * login page with an error message.
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

		// NOTE: This uses plaintext password validation for simplicity.
		// In a production environment, use a strong hashing library like BCrypt.
		User user = userDAO.validateUser(username, password);

		if (user != null) {
			// Create a new session for the user
			HttpSession session = request.getSession();
			session.setAttribute("user", user); // Store the entire user object
			session.setAttribute("username", user.getUsername());
			session.setAttribute("role", user.getRole());

			logger.info("Login successful for user: {}. Role: {}. Redirecting to home.", user.getUsername(),
					user.getRole());
			response.sendRedirect(request.getContextPath() + "/home");
		} else {
			logger.warn("Login failed for username: {}. Invalid credentials.", username);
			request.setAttribute("errorMessage", "Benutzername oder Passwort ungültig.");
			request.getRequestDispatcher("login.jsp").forward(request, response);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("GET request received for /login. Forwarding to login.jsp.");
		request.getRequestDispatcher("login.jsp").forward(request, response);
	}
}