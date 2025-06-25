package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; // Import GsonBuilder
import de.technikteam.config.LocalDateTimeAdapter; // Import the adapter
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private UserDAO userDAO;
	private EventDAO eventDAO;
	private Gson gson; // Keep Gson instance

	@Override
	public void init() {
		userDAO = new UserDAO();
		eventDAO = new EventDAO();
		// **FIXED:** Initialize Gson with the custom adapter
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
		logger.info("AdminUserServlet initialized.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		logger.debug("AdminUserServlet received GET with action: {}", action);
		try {
			switch (action) {
			case "details":
				showUserDetails(request, response);
				break;
			case "getUserData":
				getUserDataAsJson(request, response);
				break;
			default:
				listUsers(request, response);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doGet", e);
			request.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		logger.debug("AdminUserServlet received POST with action: {}", action);
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
			case "resetPassword":
				handleResetPassword(request, response);
				break;
			default:
				logger.warn("Unknown POST action received: {}", action);
				response.sendRedirect(request.getContextPath() + "/admin/users");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doPost", e);
			request.getSession().setAttribute("errorMessage",
					"Ein schwerwiegender Fehler ist aufgetreten: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Listing all users for admin view.");
		List<User> userList = userDAO.getAllUsers();
		request.setAttribute("userList", userList);
		request.getRequestDispatcher("/admin/admin_users.jsp").forward(request, response);
	}

	private void getUserDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int userId = Integer.parseInt(req.getParameter("id"));
			User user = userDAO.getUserById(userId);
			if (user != null) {
				String userJson = gson.toJson(user);
				resp.setContentType("application/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(userJson);
			} else {
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
			}
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid User ID");
		}
	}

	private void showUserDetails(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("id"));
		logger.info("Showing details for user ID: {}", userId);
		User user = userDAO.getUserById(userId);
		if (user == null) {
			request.getSession().setAttribute("errorMessage", "Benutzer nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);
		request.setAttribute("userToView", user);
		request.setAttribute("eventHistory", eventHistory);
		logger.debug("Forwarding to user details page for user '{}'", user.getUsername());
		request.getRequestDispatcher("/admin/admin_user_details.jsp").forward(request, response);
	}

	// --- Other POST handlers remain unchanged ---
	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Unchanged
		String username = request.getParameter("username");
		String pass = request.getParameter("password");
		String role = request.getParameter("role");
		if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
			request.getSession().setAttribute("errorMessage", "Benutzername und Passwort dürfen nicht leer sein.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}
		logger.info("Attempting to create new user '{}' with role '{}'", username, role);
		User newUser = new User();
		newUser.setUsername(username.trim());
		newUser.setRole(role);
		try {
			newUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			newUser.setClassYear(0); // Default value
		}
		newUser.setClassName(request.getParameter("className"));

		// FIXME: Passwords should be hashed in a production environment.
		int newUserId = userDAO.createUser(newUser, pass);
		if (newUserId > 0) {
			User adminUser = (User) request.getSession().getAttribute("user");
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s, Klasse: %d %s) erstellt.",
					newUser.getUsername(), newUserId, newUser.getRole(), newUser.getClassYear(),
					newUser.getClassName());
			AdminLogService.log(adminUser.getUsername(), "CREATE_USER", logDetails);
			request.getSession().setAttribute("successMessage",
					"Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.");
		} else {
			request.getSession().setAttribute("errorMessage",
					"Benutzer konnte nicht erstellt werden (ggf. existiert der Name bereits).");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Unchanged
		int userId = Integer.parseInt(request.getParameter("userId"));
		User adminUser = (User) request.getSession().getAttribute("user");
		User originalUser = userDAO.getUserById(userId);
		if (originalUser == null) {
			logger.error("Attempted to update non-existent user with ID: {}", userId);
			request.getSession().setAttribute("errorMessage", "Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}
		logger.info("Attempting to update user '{}' (ID: {})", originalUser.getUsername(), userId);

		User updatedUser = new User();
		updatedUser.setId(userId);
		updatedUser.setUsername(request.getParameter("username").trim());
		updatedUser.setRole(request.getParameter("role"));
		updatedUser.setClassName(request.getParameter("className"));
		try {
			updatedUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			updatedUser.setClassYear(0);
		}

		List<String> changes = new ArrayList<>();
		if (!Objects.equals(originalUser.getUsername(), updatedUser.getUsername()))
			changes.add("Benutzername von '" + originalUser.getUsername() + "' zu '" + updatedUser.getUsername() + "'");
		if (!Objects.equals(originalUser.getRole(), updatedUser.getRole()))
			changes.add("Rolle von '" + originalUser.getRole() + "' zu '" + updatedUser.getRole() + "'");
		if (originalUser.getClassYear() != updatedUser.getClassYear())
			changes.add("Jahrgang von '" + originalUser.getClassYear() + "' zu '" + updatedUser.getClassYear() + "'");
		if (!Objects.equals(originalUser.getClassName(), updatedUser.getClassName()))
			changes.add("Klasse von '" + originalUser.getClassName() + "' zu '" + updatedUser.getClassName() + "'");

		if (!changes.isEmpty()) {
			if (userDAO.updateUser(updatedUser)) {
				String logDetails = String.format("Benutzer '%s' (ID: %d) aktualisiert. Änderungen: %s.",
						originalUser.getUsername(), userId, String.join(", ", changes));
				AdminLogService.log(adminUser.getUsername(), "UPDATE_USER", logDetails);
				request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Fehler: Benutzerdaten konnten nicht in der DB aktualisiert werden.");
			}
		} else {
			request.getSession().setAttribute("infoMessage", "Keine Änderungen an den Benutzerdaten vorgenommen.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Unchanged
		int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
		User loggedInAdmin = (User) request.getSession().getAttribute("user");
		if (loggedInAdmin.getId() == userIdToDelete) {
			logger.warn("Admin '{}' (ID: {}) attempted to delete themselves. Operation denied.",
					loggedInAdmin.getUsername(), loggedInAdmin.getId());
			request.getSession().setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}

		User userToDelete = userDAO.getUserById(userIdToDelete);
		logger.warn("Admin '{}' is attempting to delete user '{}' (ID: {})", loggedInAdmin.getUsername(),
				userToDelete != null ? userToDelete.getUsername() : "N/A", userIdToDelete);

		if (userDAO.deleteUser(userIdToDelete)) {
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s) wurde gelöscht.",
					(userToDelete != null ? userToDelete.getUsername() : "N/A"), userIdToDelete,
					(userToDelete != null ? userToDelete.getRole() : "N/A"));
			AdminLogService.log(loggedInAdmin.getUsername(), "DELETE_USER", logDetails);
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Unchanged
		User adminUser = (User) request.getSession().getAttribute("user");
		try {
			int userId = Integer.parseInt(request.getParameter("userId"));
			User userToReset = userDAO.getUserById(userId);

			if (userToReset == null) {
				request.getSession().setAttribute("errorMessage", "Benutzer zum Zurücksetzen nicht gefunden.");
			} else {
				String newPassword = generateRandomPassword(8);
				if (userDAO.changePassword(userId, newPassword)) {
					String logDetails = String.format("Passwort für Benutzer '%s' (ID: %d) zurückgesetzt.",
							userToReset.getUsername(), userId);
					AdminLogService.log(adminUser.getUsername(), "RESET_PASSWORD", logDetails);

					String successMessage = String.format(
							"Passwort für '%s' wurde zurückgesetzt auf: <strong class=\"copyable-password\">%s</strong> (wurde in die Zwischenablage kopiert).",
							userToReset.getUsername(), newPassword);
					request.getSession().setAttribute("passwordResetInfo", successMessage);
				} else {
					request.getSession().setAttribute("errorMessage", "Passwort konnte nicht zurückgesetzt werden.");
				}
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid user ID for password reset.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Benutzer-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private String generateRandomPassword(int length) {
		// Unchanged
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}
}