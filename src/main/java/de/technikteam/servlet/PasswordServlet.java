package de.technikteam.servlet;

import java.io.IOException;

import de.technikteam.util.PasswordPolicyValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
		HttpSession session = request.getSession(false);
		User user = (session != null) ? (User) session.getAttribute("user") : null;

		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for password change attempt by user '{}'", user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
			return;
		}

		logger.info("Password change attempt for user: {}", user.getUsername());

		String currentPassword = request.getParameter("currentPassword");
		String newPassword = request.getParameter("newPassword");
		String confirmPassword = request.getParameter("confirmPassword");

		User authenticatedUser = userDAO.validateUser(user.getUsername(), currentPassword);
		if (authenticatedUser == null) {
			logger.warn("Password change failed for {}: incorrect current password.", user.getUsername());
			session.setAttribute("errorMessage", "Das aktuelle Passwort ist nicht korrekt.");
			response.sendRedirect(request.getContextPath() + "/passwort");
			return;
		}

		if (!newPassword.equals(confirmPassword)) {
			logger.warn("Password change failed for {}: new passwords do not match.", user.getUsername());
			session.setAttribute("errorMessage", "Die neuen Passwörter stimmen nicht überein.");
			response.sendRedirect(request.getContextPath() + "/passwort");
			return;
		}

		PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(newPassword);
		if (!validationResult.isValid()) {
			logger.warn("Password change for user '{}' failed due to weak password: {}", user.getUsername(),
					validationResult.getMessage());
			session.setAttribute("errorMessage", validationResult.getMessage());
			response.sendRedirect(request.getContextPath() + "/passwort");
			return;
		}

		boolean success = userDAO.changePassword(user.getId(), newPassword);
		if (success) {
			logger.info("Password successfully changed for user: {}", user.getUsername());
			CSRFUtil.storeToken(session);
			session.setAttribute("successMessage", "Ihr Passwort wurde erfolgreich geändert.");
		} else {
			logger.error("Password change failed for {} due to a DAO error.", user.getUsername());
			session.setAttribute("errorMessage", "Ein interner Fehler ist aufgetreten. Bitte versuchen Sie es erneut.");
		}
		response.sendRedirect(request.getContextPath() + "/passwort");
	}
}