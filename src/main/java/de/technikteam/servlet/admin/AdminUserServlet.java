package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Permission;
import de.technikteam.model.Role;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
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
import java.util.*;

@WebServlet("/admin/mitglieder")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private UserDAO userDAO;
	private EventDAO eventDAO;
	private RoleDAO roleDAO;
	private PermissionDAO permissionDAO;
	private Gson gson;

	@Override
	public void init() {
		userDAO = new UserDAO();
		eventDAO = new EventDAO();
		roleDAO = new RoleDAO();
		permissionDAO = new PermissionDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		User currentUser = (User) request.getSession().getAttribute("user");
		Set<String> permissions = currentUser.getPermissions();

		logger.debug("AdminUserServlet received GET with action: {}", action);
		try {
			if (!permissions.contains("USER_READ") && !permissions.contains("ACCESS_ADMIN_PANEL")) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
				return;
			}
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
		} catch (NumberFormatException e) {
			logger.warn("Invalid ID format in GET request: {}", e.getMessage());
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige ID angegeben.");
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

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for action '{}'.", action);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
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
		} catch (NumberFormatException e) {
			logger.warn("Invalid ID format in POST request: {}", e.getMessage());
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID übermittelt.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
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
		List<Permission> allPermissions = permissionDAO.getAllPermissions();

		Map<String, List<Permission>> groupedPermissions = new LinkedHashMap<>();
		for (Permission p : allPermissions) {
			String key = p.getPermissionKey();
			String groupName = "ALLGEMEIN";
			if (key.contains("_")) {
				groupName = key.substring(0, key.indexOf('_'));
			}
			groupedPermissions.computeIfAbsent(groupName, k -> new ArrayList<>()).add(p);
		}

		logger.debug("Fetched {} users, {} roles, and {} permissions from DAOs.", userList.size(), allRoles.size(),
				allPermissions.size());
		request.setAttribute("userList", userList);
		request.setAttribute("allRoles", allRoles);
		request.setAttribute("groupedPermissionsJson", gson.toJson(groupedPermissions));
		request.getRequestDispatcher("/views/admin/admin_users.jsp").forward(request, response);
	}

	private void getUserDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int userId = Integer.parseInt(req.getParameter("id"));
		User user = userDAO.getUserById(userId);
		if (user != null) {
			Set<Integer> permissionIds = permissionDAO.getPermissionIdsForUser(userId);
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("user", user);
			responseData.put("permissionIds", permissionIds);

			String userJson = gson.toJson(responseData);
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(userJson);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
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
		request.getRequestDispatcher("/views/admin/admin_user_details.jsp").forward(request, response);
	}

	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (!adminUser.getPermissions().contains("USER_CREATE")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String username = request.getParameter("username");
		String pass = request.getParameter("password");
		if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
			request.getSession().setAttribute("errorMessage", "Benutzername und Passwort dürfen nicht leer sein.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		int roleId = Integer.parseInt(request.getParameter("roleId"));
		String[] permissionIds = request.getParameterValues("permissionIds");

		User newUser = new User();
		newUser.setUsername(username.trim());
		newUser.setRoleId(roleId);
		try {
			newUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			newUser.setClassYear(0);
		}
		newUser.setClassName(request.getParameter("className"));

		String email = request.getParameter("email");
		newUser.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);

		int newUserId = userDAO.createUser(newUser, pass);
		if (newUserId > 0) {
			userDAO.updateUserPermissions(newUserId, permissionIds);
			String logDetails = String.format(
					"Benutzer '%s' (ID: %d, Rolle-ID: %d, Klasse: %d %s) erstellt und Berechtigungen zugewiesen.",
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

		if (!adminUser.getPermissions().contains("USER_UPDATE")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		User originalUser = userDAO.getUserById(userId);

		if (originalUser == null) {
			logger.error("Attempted to update non-existent user with ID: {}", userId);
			request.getSession().setAttribute("errorMessage", "Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		int roleId = Integer.parseInt(request.getParameter("roleId"));
		String[] permissionIds = request.getParameterValues("permissionIds");

		User updatedUser = new User();
		updatedUser.setId(userId);
		updatedUser.setUsername(request.getParameter("username").trim());
		updatedUser.setRoleId(roleId);
		updatedUser.setClassName(request.getParameter("className"));

		String email = request.getParameter("email");
		updatedUser.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);

		try {
			updatedUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		} catch (NumberFormatException e) {
			updatedUser.setClassYear(0);
		}

		boolean profileUpdated = userDAO.updateUser(updatedUser);
		boolean permissionsUpdated = userDAO.updateUserPermissions(userId, permissionIds);

		if (profileUpdated || permissionsUpdated) {
			if (adminUser.getId() == userId) {
				User refreshedUserInSession = userDAO.getUserById(userId);
				refreshedUserInSession.setPermissions(userDAO.getPermissionsForUser(userId));
				session.setAttribute("user", refreshedUserInSession);
			}
			AdminLogService.log(adminUser.getUsername(), "UPDATE_USER",
					"Benutzer '" + originalUser.getUsername() + "' (ID: " + userId + ") aktualisiert.");
			request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
		} else {
			request.getSession().setAttribute("infoMessage", "Keine Änderungen an den Benutzerdaten vorgenommen.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
		User loggedInAdmin = (User) request.getSession().getAttribute("user");

		if (!loggedInAdmin.getPermissions().contains("USER_DELETE")
				&& !loggedInAdmin.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		if (loggedInAdmin.getId() == userIdToDelete) {
			request.getSession().setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}

		User userToDelete = userDAO.getUserById(userIdToDelete);
		if (userToDelete == null) {
			request.getSession().setAttribute("errorMessage", "Benutzer mit ID " + userIdToDelete + " nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}

		Set<String> targetPermissions = userDAO.getPermissionsForUser(userIdToDelete);
		if (targetPermissions.contains("ACCESS_ADMIN_PANEL")
				&& !loggedInAdmin.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			logger.warn("Privilege Escalation Attempt: User '{}' tried to delete super-admin '{}'",
					loggedInAdmin.getUsername(), userToDelete.getUsername());
			request.getSession().setAttribute("errorMessage",
					"Sie haben keine Berechtigung, einen Haupt-Administrator zu löschen.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}

		String deletedUsername = userToDelete.getUsername();
		String deletedRoleName = userToDelete.getRoleName();

		if (userDAO.deleteUser(userIdToDelete)) {
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s) wurde gelöscht.", deletedUsername,
					userIdToDelete, deletedRoleName);
			AdminLogService.log(loggedInAdmin.getUsername(), "DELETE_USER", logDetails);
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
	}

	private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User adminUser = (User) request.getSession().getAttribute("user");
		if (!adminUser.getPermissions().contains("USER_PASSWORD_RESET")
				&& !adminUser.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		int userId = Integer.parseInt(request.getParameter("userId"));
		User userToReset = userDAO.getUserById(userId);

		if (userToReset == null) {
			request.getSession().setAttribute("errorMessage", "Benutzer zum Zurücksetzen nicht gefunden.");
		} else {
			String newPassword = generateRandomPassword(12);
			if (userDAO.changePassword(userId, newPassword)) {
				String logDetails = String.format("Passwort für Benutzer '%s' (ID: %d) zurückgesetzt.",
						userToReset.getUsername(), userId);
				AdminLogService.log(adminUser.getUsername(), "RESET_PASSWORD", logDetails);

				request.getSession().setAttribute("passwordResetUser", userToReset.getUsername());
				request.getSession().setAttribute("passwordResetNewPassword", newPassword);
			} else {
				request.getSession().setAttribute("errorMessage", "Passwort konnte nicht zurückgesetzt werden.");
			}
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