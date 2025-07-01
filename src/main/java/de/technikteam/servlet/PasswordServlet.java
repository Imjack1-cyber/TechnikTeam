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

/**
 * Mapped to `/passwort`, this servlet allows a logged-in user to change their
 * own password. It handles GET requests by displaying the change form
 * (`passwort.jsp`) and POST requests by processing the password change. This
 * includes validating the user's current password and ensuring the new password
 * confirmation matches before updating the database via `UserDAO`.
 */
@WebServlet("/passwort")
public class PasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PasswordServlet.class);
	private UserDAO userDAO;

	public void init() {
		userDAO = new UserDAO();
		logger.info("PasswordServlet initialized.");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("GET request received, showing password change form.");
		request.getRequestDispatcher("/views/public/passwort.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		logger.info("Password change attempt for user: {}", user.getUsername());

		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");

		// Validate that the user knows their current password
		User authenticatedUser = userDAO.validateUser(user.getUsername(), currentPassword);
		if (authenticatedUser == null) {
			logger.warn("Password change failed for {}: incorrect current password.", user.getUsername());
			request.setAttribute("errorMessage", "Das aktuelle Passwort ist nicht korrekt.");
			request.getRequestDispatcher("/views/public/passwort.jsp").forward(request, response);
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			logger.warn("Password change failed for {}: new passwords do not match.", user.getUsername());
			request.setAttribute("errorMessage", "Die neuen Passwörter stimmen nicht überein.");
			request.getRequestDispatcher("/views/public/passwort.jsp").forward(request, response);
			return;
		}

		if (newPassword.trim().isEmpty()) {
			logger.warn("Password change failed for {}: new password is empty.", user.getUsername());
			request.setAttribute("errorMessage", "Das neue Passwort darf nicht leer sein.");
			request.getRequestDispatcher("/views/public/passwort.jsp").forward(request, response);
			return;
		}

		boolean success = userDAO.changePassword(user.getId(), newPassword);
		if (success) {
			logger.info("Password successfully changed for user: {}", user.getUsername());
			request.setAttribute("successMessage", "Ihr Passwort wurde erfolgreich geändert.");
		} else {
			logger.error("Password change failed for {} due to a DAO error.", user.getUsername());
			request.setAttribute("errorMessage", "Ein interner Fehler ist aufgetreten. Bitte versuchen Sie es erneut.");
		}
		// CORRECTED: Forward to the actual JSP file path.
		request.getRequestDispatcher("/views/public/passwort.jsp").forward(request, response);
	}
}