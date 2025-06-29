package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Role;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@WebServlet("/admin/mitglieder")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private UserDAO userDAO;
	private EventDAO eventDAO;
	private RoleDAO roleDAO;
	private Gson gson;

	@Override
	public void init() {
		userDAO = new UserDAO();
		eventDAO = new EventDAO();
		roleDAO = new RoleDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
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
			response.sendRedirect(request.getContextPath() + "/admin/dashboard");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if (action == null) {
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
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
				response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doPost", e);
			request.getSession().setAttribute("errorMessage",
					"Ein schwerwiegender Fehler ist aufgetreten: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Executing listUsers method.");
		List<User> userList = userDAO.getAllUsers();
		List<Role> allRoles = roleDAO.getAllRoles();
		logger.debug("Fetched {} users and {} roles from DAOs.", userList.size(), allRoles.size());
		request.setAttribute("userList", userList);
		request.setAttribute("allRoles", allRoles);
		request.getRequestDispatcher("/admin/mitglieder").forward(request, response);
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
		User user = userDAO.getUserById(userId);
		if (user == null) {
			request.getSession().setAttribute("errorMessage", "Benutzer nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);
		request.setAttribute("userToView", user);
		request.setAttribute("eventHistory", eventHistory);
		request.getRequestDispatcher("/admin/mitglieder/details").forward(request, response);
	}

	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String username = request.getParameter("username");
		String pass = request.getParameter("password");
		if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
			request.getSession().setAttribute("errorMessage", "Benutzername und Passwort dürfen nicht leer sein.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		int roleId = Integer.parseInt(request.getParameter("roleId"));

		User newUser = new User();
		newUser.setUsername(username.trim());
		newUser.setRoleId(roleId);
		try {
			newUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			newUser.setClassYear(0);
		}
		newUser.setClassName(request.getParameter("className"));
		newUser.setEmail(request.getParameter("email"));

		int newUserId = userDAO.createUser(newUser, pass);
		if (newUserId > 0) {
			User adminUser = (User) request.getSession().getAttribute("user");
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle-ID: %d, Klasse: %d %s) erstellt.",
					newUser.getUsername(), newUserId, newUser.getRoleId(), newUser.getClassYear(),
					newUser.getClassName());
			AdminLogService.log(adminUser.getUsername(), "CREATE_USER", logDetails);
			request.getSession().setAttribute("successMessage",
					"Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.");
		} else {
			request.getSession().setAttribute("errorMessage",
					"Benutzer konnte nicht erstellt werden (ggf. existiert der Name oder die E-Mail bereits).");
		}
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");
		User originalUser = userDAO.getUserById(userId);

		if (originalUser == null) {
			logger.error("Attempted to update non-existent user with ID: {}", userId);
			request.getSession().setAttribute("errorMessage", "Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		int roleId = Integer.parseInt(request.getParameter("roleId"));

		User updatedUser = new User();
		updatedUser.setId(userId);
		updatedUser.setUsername(request.getParameter("username").trim());
		updatedUser.setRoleId(roleId);
		updatedUser.setClassName(request.getParameter("className"));
		updatedUser.setEmail(request.getParameter("email"));
		try {
			updatedUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			updatedUser.setClassYear(0);
		}

		List<String> changes = new ArrayList<>();
		if (!Objects.equals(originalUser.getUsername(), updatedUser.getUsername()))
			changes.add("Benutzername von '" + originalUser.getUsername() + "' zu '" + updatedUser.getUsername() + "'");
		if (originalUser.getRoleId() != updatedUser.getRoleId())
			changes.add("Rolle-ID von '" + originalUser.getRoleId() + "' zu '" + updatedUser.getRoleId() + "'");
		if (originalUser.getClassYear() != updatedUser.getClassYear())
			changes.add("Jahrgang von '" + originalUser.getClassYear() + "' zu '" + updatedUser.getClassYear() + "'");
		if (!Objects.equals(originalUser.getClassName(), updatedUser.getClassName()))
			changes.add("Klasse von '" + originalUser.getClassName() + "' zu '" + updatedUser.getClassName() + "'");
		if (!Objects.equals(originalUser.getEmail(), updatedUser.getEmail()))
			changes.add("E-Mail geändert");

		if (!changes.isEmpty()) {
			if (userDAO.updateUser(updatedUser)) {
				if (adminUser.getId() == userId) {
					User refreshedUserInSession = userDAO.getUserById(userId);
					Set<String> newPermissions = userDAO.getPermissionsForRole(refreshedUserInSession.getRoleId());
					refreshedUserInSession.setPermissions(newPermissions);
					session.setAttribute("user", refreshedUserInSession);
				}
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
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
		User loggedInAdmin = (User) request.getSession().getAttribute("user");
		if (loggedInAdmin.getId() == userIdToDelete) {
			request.getSession().setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		User userToDelete = userDAO.getUserById(userIdToDelete);
		if (userDAO.deleteUser(userIdToDelete)) {
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s) wurde gelöscht.",
					(userToDelete != null ? userToDelete.getUsername() : "N/A"), userIdToDelete,
					(userToDelete != null ? userToDelete.getRoleName() : "N/A"));
			AdminLogService.log(loggedInAdmin.getUsername(), "DELETE_USER", logDetails);
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private String generateRandomPassword(int length) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}
}