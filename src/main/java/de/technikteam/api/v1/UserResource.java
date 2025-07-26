// src/main/java/de/technikteam/api/v1/UserResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.UserService;
import de.technikteam.servlet.LoginServlet; // For LoginAttemptManager
import de.technikteam.util.PasswordPolicyValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A stateless, resource-oriented REST API endpoint for managing users. This
 * servlet handles all CRUD operations and other actions for the User resource.
 * Mapped to /api/v1/users/*
 */
@Singleton
public class UserResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(UserResource.class);

	private final UserService userService;
	private final UserDAO userDAO;
	private final RoleDAO roleDAO;
	private final AdminLogService adminLogService;
	private final Gson gson;

	@Inject
	public UserResource(UserService userService, UserDAO userDAO, RoleDAO roleDAO, AdminLogService adminLogService,
			Gson gson) {
		this.userService = userService;
		this.userDAO = userDAO;
		this.roleDAO = roleDAO;
		this.adminLogService = adminLogService;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/users -> Returns a list of all users. GET
	 * /api/v1/users/{id} -> Returns a single user.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user"); // Assumes a filter provides this
		if (adminUser == null || !adminUser.getPermissions().contains("USER_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();

		try {
			if (pathInfo == null || pathInfo.equals("/")) {
				// Get all users
				List<User> users = userDAO.getAllUsers();
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "Users retrieved successfully", users));
			} else {
				// Get single user by ID
				Integer userId = parseIdFromPath(pathInfo);
				if (userId != null) {
					User user = userDAO.getUserById(userId);
					if (user != null) {
						sendJsonResponse(resp, HttpServletResponse.SC_OK,
								new ApiResponse(true, "User retrieved successfully", user));
					} else {
						sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found");
					}
				} else {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
				}
			}
		} catch (Exception e) {
			logger.error("Error processing GET request for users", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles POST requests. POST /api/v1/users -> Creates a new user. POST
	 * /api/v1/users/{id}/reset-password -> Resets a user's password. POST
	 * /api/v1/users/{id}/unlock -> Unlocks a user account.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		User adminUser = (User) req.getAttribute("user");

		if (pathInfo == null || pathInfo.equals("/")) {
			handleCreateUser(req, resp, adminUser);
		} else {
			String[] pathParts = pathInfo.substring(1).split("/");
			if (pathParts.length == 2) {
				Integer userId = parseId(pathParts[0]);
				String action = pathParts[1];

				if (userId == null) {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID in URL.");
					return;
				}

				switch (action) {
				case "reset-password":
					handleResetPassword(req, resp, adminUser, userId);
					break;
				case "unlock":
					handleUnlockUser(req, resp, adminUser, userId);
					break;
				default:
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Unknown action for user.");
				}
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			}
		}
	}

	private void handleCreateUser(HttpServletRequest req, HttpServletResponse resp, User adminUser) throws IOException {
		if (adminUser == null || !adminUser.getPermissions().contains("USER_CREATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			User newUser = gson.fromJson(jsonPayload, User.class);
			Map<String, Object> payloadMap = gson.fromJson(jsonPayload, Map.class);
			String password = (String) payloadMap.get("password");

			// Handle permissionIds which might be an empty array
			List<Double> permissionIdDoubles = (List<Double>) payloadMap.get("permissionIds");
			String[] permissionIds = permissionIdDoubles.stream().map(d -> Integer.toString(d.intValue()))
					.toArray(String[]::new);

			if (password == null) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Password is required for new user.");
				return;
			}

			PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(password);
			if (!validationResult.isValid()) {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"Password does not meet policy: " + validationResult.getMessage());
				return;
			}

			int newUserId = userService.createUserWithPermissions(newUser, password, permissionIds,
					adminUser.getUsername());
			if (newUserId > 0) {
				User createdUser = userDAO.getUserById(newUserId);
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "User created successfully", createdUser));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
						"User could not be created (username or email may already exist).");
			}
		} catch (JsonSyntaxException e) {
			logger.warn("Invalid JSON format for create user request", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing POST request to create user", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	private void handleResetPassword(HttpServletRequest req, HttpServletResponse resp, User adminUser, int userId)
			throws IOException {
		if (adminUser == null || !adminUser.getPermissions().contains("USER_PASSWORD_RESET")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		User userToReset = userDAO.getUserById(userId);
		if (userToReset == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "User to reset not found.");
			return;
		}

		String newPassword = generateRandomPassword(12);
		if (userDAO.changePassword(userId, newPassword)) {
			String logDetails = String.format("Passwort für Benutzer '%s' (ID: %d) zurückgesetzt.",
					userToReset.getUsername(), userId);
			adminLogService.log(adminUser.getUsername(), "RESET_PASSWORD_API", logDetails);

			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Password for " + userToReset.getUsername() + " has been reset.",
							Map.of("username", userToReset.getUsername(), "newPassword", newPassword)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Password could not be reset.");
		}
	}

	private void handleUnlockUser(HttpServletRequest req, HttpServletResponse resp, User adminUser, int userId)
			throws IOException {
		if (adminUser == null || !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		User userToUnlock = userDAO.getUserById(userId);
		if (userToUnlock == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "User to unlock not found.");
			return;
		}

		String usernameToUnlock = userToUnlock.getUsername();
		LoginServlet.LoginAttemptManager.clearLoginAttempts(usernameToUnlock);
		adminLogService.log(adminUser.getUsername(), "UNLOCK_USER_API",
				"User account '" + usernameToUnlock + "' (ID: " + userId + ") unlocked via API.");
		sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true,
				"User account '" + usernameToUnlock + "' has been unlocked.", Map.of("unlockedUserId", userId)));
	}

	/**
	 * Handles PUT requests. PUT /api/v1/users/{id} -> Updates an existing user from
	 * a JSON body.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("USER_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer userId = parseIdFromPath(req.getPathInfo());
		if (userId == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID in URL.");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			User updatedUser = gson.fromJson(jsonPayload, User.class);
			updatedUser.setId(userId);

			Map<String, Object> payloadMap = gson.fromJson(jsonPayload, Map.class);
			List<Double> permissionIdDoubles = (List<Double>) payloadMap.get("permissionIds");
			String[] permissionIds = permissionIdDoubles.stream().map(d -> Integer.toString(d.intValue()))
					.toArray(String[]::new);

			boolean success = userService.updateUserWithPermissions(updatedUser, permissionIds);
			if (success) {
				User refreshedUser = userDAO.getUserById(userId);
				adminLogService.log(adminUser.getUsername(), "UPDATE_USER_API",
						"User '" + refreshedUser.getUsername() + "' (ID: " + userId + ") updated via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "User updated successfully", refreshedUser));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update user.");
			}
		} catch (JsonSyntaxException e) {
			logger.warn("Invalid JSON format for update user request", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
		} catch (Exception e) {
			logger.error("Error processing PUT request to update user", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred.");
		}
	}

	/**
	 * Handles DELETE requests. DELETE /api/v1/users/{id} -> Deletes a user.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("USER_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Integer userIdToDelete = parseIdFromPath(req.getPathInfo());
		if (userIdToDelete == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID in URL.");
			return;
		}

		if (adminUser.getId() == userIdToDelete) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "You cannot delete your own account.");
			return;
		}

		User userToDelete = userDAO.getUserById(userIdToDelete);
		if (userToDelete == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "User to delete not found.");
			return;
		}

		if (userToDelete.getPermissions().contains("ACCESS_ADMIN_PANEL") && !adminUser.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN,
					"You are not authorized to delete a super administrator.");
			return;
		}

		if (userDAO.deleteUser(userIdToDelete)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_USER_API",
					"User '" + userToDelete.getUsername() + "' (ID: " + userIdToDelete + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "User deleted successfully", Map.of("deletedUserId", userIdToDelete)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete user.");
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1) {
			return null;
		}
		try {
			return Integer.parseInt(pathInfo.substring(1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}

	private String generateRandomPassword(int length) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
		SecureRandom random = new SecureRandom();
		return random.ints(length, 0, chars.length()).mapToObj(chars::charAt)
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	}
}