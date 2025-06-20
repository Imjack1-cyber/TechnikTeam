package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private UserDAO userDAO;
	private UserQualificationsDAO userQualificationsDAO;
	private EventDAO eventDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
		userQualificationsDAO = new UserQualificationsDAO();
		eventDAO = new EventDAO();
		logger.info("AdminUserServlet initialized.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		logger.debug("doGet received action: {}", action);

		try {
			switch (action) {
			case "details":
				showUserDetails(request, response);
				break;
			default:
				listUsers(request, response);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doGet", e);
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		logger.debug("doPost received action: {}", action);

		if (action == null) {
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}

		try {
			switch (action) {
			case "create":
				handleCreateUser(request, response);
				break;
			case "update":
				handleUpdateUser(request, response);
				break;
			case "delete":
				handleDeleteUser(request, response);
				break;
			case "updateQualification":
				handleUpdateQualification(request, response);
				break;
			default:
				logger.warn("Unknown POST action received: {}", action);
				response.sendRedirect(request.getContextPath() + "/admin/users");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doPost", e);
			request.getSession().setAttribute("errorMessage", "Ein schwerwiegender Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("Listing all users.");
		List<User> userList = userDAO.getAllUsers();
		request.setAttribute("userList", userList);
		request.getRequestDispatcher("/admin/admin_users.jsp").forward(request, response);
	}

	private void showUserDetails(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("id"));
		logger.debug("Showing details for user ID: {}", userId);
		User user = userDAO.getUserById(userId);
		List<UserQualification> qualifications = userQualificationsDAO.getQualificationsForUser(userId);
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);

		request.setAttribute("userToEdit", user);
		request.setAttribute("qualifications", qualifications);
		request.setAttribute("eventHistory", eventHistory);

		request.getRequestDispatcher("/admin/admin_user_details.jsp").forward(request, response);
	}

	/**
	 * Handles the creation of a new user from the form on the user list page. It
	 * validates input and provides user feedback via session attributes.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String username = request.getParameter("username");
		String pass = request.getParameter("password");
		String role = request.getParameter("role");
		String classYearStr = request.getParameter("classYear");
		String className = request.getParameter("className");

		logger.info("Attempting to create new user: {}", username);

		// --- Input Validation ---
		if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
			request.getSession().setAttribute("errorMessage", "Benutzername und Passwort dürfen nicht leer sein.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}

		try {
			User newUser = new User();
			newUser.setUsername(username.trim());
			newUser.setRole(role);
			// Handle potentially empty number field gracefully
			newUser.setClassYear(classYearStr != null && !classYearStr.isEmpty() ? Integer.parseInt(classYearStr) : 0);
			newUser.setClassName(className);

			int newUserId = userDAO.createUser(newUser, pass);

			if (newUserId > 0) {
				User adminUser = (User) request.getSession().getAttribute("user");
				AdminLogService.log(adminUser.getUsername(), "CREATE_USER",
						"Benutzer '" + newUser.getUsername() + "' (ID: " + newUserId + ") erstellt.");
				logger.info("Successfully created new user '{}' with ID: {}", username, newUserId);
				request.getSession().setAttribute("successMessage",
						"Benutzer '" + username + "' erfolgreich erstellt.");
			} else {
				logger.error("Failed to create new user: {}", username);
				request.getSession().setAttribute("errorMessage",
						"Benutzer konnte nicht erstellt werden (ggf. existiert der Name bereits).");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid format for 'classYear': {}", classYearStr, e);
			request.getSession().setAttribute("errorMessage", "Ungültiges Format für Jahrgang.");
		}

		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		logger.info("Attempting to update user data for ID: {}", userId);
		User user = userDAO.getUserById(userId);
		if (user != null) {
			user.setUsername(request.getParameter("username"));
			user.setRole(request.getParameter("role"));
			user.setClassYear(Integer.parseInt(request.getParameter("classYear")));
			user.setClassName(request.getParameter("className"));

			if (userDAO.updateUser(user)) {
				User adminUser = (User) request.getSession().getAttribute("user");
				AdminLogService.log(adminUser.getUsername(), "UPDATE_USER",
						"Daten für Benutzer '" + user.getUsername() + "' (ID: " + user.getId() + ") aktualisiert.");
				request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Benutzerdaten konnten nicht aktualisiert werden.");
			}
		}
		response.sendRedirect(request.getContextPath() + "/admin/users?action=details&id=" + userId);
	}

	/**
	 * Handles the deletion of a user. THIS IS THE FINAL, CORRECTED METHOD.
	 */
	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			int userId = Integer.parseInt(request.getParameter("userId"));
			logger.warn("Attempting to delete user with ID: {}", userId);

			User loggedInUser = (User) request.getSession().getAttribute("user");
			if (loggedInUser != null && loggedInUser.getId() == userId) {
				logger.warn("Admin {} attempted to delete themselves. Operation denied.", loggedInUser.getUsername());
				request.getSession().setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
				response.sendRedirect(request.getContextPath() + "/admin/users");
				return;
			}

			User userToDelete = userDAO.getUserById(userId); // Holen des Namens VOR dem Löschen
			if (userDAO.deleteUser(userId)) {
				User adminUser = (User) request.getSession().getAttribute("user");
				AdminLogService.log(adminUser.getUsername(), "DELETE_USER",
						"Benutzer '" + userToDelete.getUsername() + "' (ID: " + userId + ") gelöscht.");
				logger.info("Successfully deleted user with ID: {}", userId);
				request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
			} else {
				logger.error("Failed to delete user with ID: {}. DAO returned false.", userId);
				request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid user ID format for deletion.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Benutzer-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	/**
	 * Handles the submission from the qualification editing modal in the matrix or
	 * details view. It creates, updates, or deletes a user's qualification record
	 * based on the selected status.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleUpdateQualification(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String redirectUrl = request.getContextPath() + "/admin/matrix"; // Default redirect
		int userId = 0; // Initialize to handle potential errors

		try {
			String userIdStr = request.getParameter("userId");
			String courseIdStr = request.getParameter("courseId");
			String status = request.getParameter("status");
			String remarks = request.getParameter("remarks");
			String dateParam = request.getParameter("completionDate");

			logger.info("--- QUALIFICATION UPDATE RECEIVED ---");
			logger.info("Raw userId: '{}', courseId: '{}', status: '{}', date: '{}', remarks: '{}'", userIdStr,
					courseIdStr, status, dateParam, remarks);

			userId = Integer.parseInt(userIdStr);
			int courseId = Integer.parseInt(courseIdStr);

			LocalDate completionDate = null;
			if (dateParam != null && !dateParam.isEmpty()) {
				completionDate = LocalDate.parse(dateParam);
			}

			if (userQualificationsDAO.updateQualificationStatus(userId, courseId, status, completionDate, remarks)) {
				User adminUser = (User) request.getSession().getAttribute("user");
				String details = String.format("Status für Nutzer '%s' / Lehrgang '%d' auf '%s' gesetzt.",
						userDAO.getUserById(userId).getUsername(), courseId, status);
				AdminLogService.log(adminUser.getUsername(), "UPDATE_QUALIFICATION", details);

				request.getSession().setAttribute("successMessage", "Qualifikation erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Qualifikation konnte nicht aktualisiert werden.");
			}

			if ("userDetails".equals(request.getParameter("returnTo"))) {
				redirectUrl = request.getContextPath() + "/admin/users?action=details&id=" + userId;
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid ID format during qualification update.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID-Parameter empfangen.");
		} catch (Exception e) {
			logger.error("A generic error occurred during qualification update.", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
		}

		response.sendRedirect(redirectUrl);
	}
}