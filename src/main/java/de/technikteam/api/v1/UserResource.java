package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.UserCreateRequest;
import de.technikteam.api.v1.dto.UserUpdateRequest;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.LoginAttemptService;
import de.technikteam.service.UserService;
import de.technikteam.util.PasswordPolicyValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Admin Users", description = "Endpoints for managing users.")
public class UserResource {

	private final UserService userService;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final LoginAttemptService loginAttemptService;

	@Autowired
	public UserResource(UserService userService, UserDAO userDAO, AdminLogService adminLogService,
			LoginAttemptService loginAttemptService) {
		this.userService = userService;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.loginAttemptService = loginAttemptService;
	}

	private String getSystemUsername() {
		return "SYSTEM";
	}

	@GetMapping
	@Operation(summary = "Get all users", description = "Retrieves a list of all users in the system.")
	public ResponseEntity<ApiResponse> getAllUsers() {
		List<User> users = userDAO.getAllUsers();
		return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", users));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by ID", description = "Retrieves a single user by their ID, including their permissions.")
	public ResponseEntity<ApiResponse> getUserById(
			@Parameter(description = "ID of the user to retrieve") @PathVariable int id) {
		User user = userDAO.getUserById(id);
		if (user != null) {
			return ResponseEntity.ok(new ApiResponse(true, "User retrieved successfully", user));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
		}
	}

	@PostMapping
	@Operation(summary = "Create a new user", description = "Creates a new user with a specified role and individual permissions.")
	public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserCreateRequest createRequest) {
		PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator
				.validate(createRequest.password());
		if (!validationResult.isValid()) {
			return ResponseEntity.badRequest().body(
					new ApiResponse(false, "Password does not meet policy: " + validationResult.getMessage(), null));
		}

		User newUser = new User();
		newUser.setUsername(createRequest.username());
		newUser.setRoleId(createRequest.roleId());
		newUser.setEmail(createRequest.email());
		newUser.setClassYear(createRequest.classYear() != null ? createRequest.classYear() : 0);
		newUser.setClassName(createRequest.className());

		String[] permissionIds = createRequest.permissionIds().stream().map(String::valueOf).toArray(String[]::new);

		int newUserId = userService.createUserWithPermissions(newUser, createRequest.password(), permissionIds,
				getSystemUsername());
		if (newUserId > 0) {
			User createdUser = userDAO.getUserById(newUserId);
			return new ResponseEntity<>(new ApiResponse(true, "User created successfully", createdUser),
					HttpStatus.CREATED);
		} else {
			return ResponseEntity.badRequest().body(
					new ApiResponse(false, "User could not be created (username or email may already exist).", null));
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a user", description = "Updates an existing user's profile details, role, and individual permissions.")
	public ResponseEntity<ApiResponse> updateUser(
			@Parameter(description = "ID of the user to update") @PathVariable int id,
			@Valid @RequestBody UserUpdateRequest updateRequest) {

		User userToUpdate = userDAO.getUserById(id);
		if (userToUpdate == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found.", null));
		}

		userToUpdate.setUsername(updateRequest.username());
		userToUpdate.setRoleId(updateRequest.roleId());
		userToUpdate.setEmail(updateRequest.email());
		userToUpdate.setClassYear(updateRequest.classYear() != null ? updateRequest.classYear() : 0);
		userToUpdate.setClassName(updateRequest.className());

		String[] permissionIds = updateRequest.permissionIds().stream().map(String::valueOf).toArray(String[]::new);

		if (userService.updateUserWithPermissions(userToUpdate, permissionIds)) {
			adminLogService.log(getSystemUsername(), "UPDATE_USER_API",
					"User '" + userToUpdate.getUsername() + "' (ID: " + id + ") updated via API.");
			User refreshedUser = userDAO.getUserById(id);
			return ResponseEntity.ok(new ApiResponse(true, "User updated successfully", refreshedUser));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to update user.", null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a user", description = "Permanently deletes a user from the system.")
	public ResponseEntity<ApiResponse> deleteUser(
			@Parameter(description = "ID of the user to delete") @PathVariable int id) {

		User userToDelete = userDAO.getUserById(id);
		if (userToDelete == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "User to delete not found.", null));
		}

		if (userToDelete.getId() == 1) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "The default admin account cannot be deleted.", null));
		}

		if (userDAO.deleteUser(id)) {
			adminLogService.log(getSystemUsername(), "DELETE_USER_API",
					"User '" + userToDelete.getUsername() + "' (ID: " + id + ") deleted via API.");
			return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully", Map.of("deletedUserId", id)));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to delete user.", null));
		}
	}

	@PostMapping("/{id}/reset-password")
	@Operation(summary = "Reset user's password", description = "Resets a user's password to a new, randomly generated password.")
	public ResponseEntity<ApiResponse> resetPassword(
			@Parameter(description = "ID of the user whose password will be reset") @PathVariable int id) {

		User userToReset = userDAO.getUserById(id);
		if (userToReset == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "User to reset not found.", null));
		}

		String newPassword = generateRandomPassword(12);
		if (userDAO.changePassword(id, newPassword)) {
			adminLogService.log(getSystemUsername(), "RESET_PASSWORD_API",
					"Password for user '" + userToReset.getUsername() + "' (ID: " + id + ") reset via API.");
			return ResponseEntity
					.ok(new ApiResponse(true, "Password for " + userToReset.getUsername() + " has been reset.",
							Map.of("username", userToReset.getUsername(), "newPassword", newPassword)));
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Password could not be reset.", null));
		}
	}

	@PostMapping("/{id}/unlock")
	@Operation(summary = "Unlock a user account", description = "Unlocks a user account that was locked due to too many failed login attempts.")
	public ResponseEntity<ApiResponse> unlockUser(
			@Parameter(description = "ID of the user to unlock") @PathVariable int id) {

		User userToUnlock = userDAO.getUserById(id);
		if (userToUnlock == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "User to unlock not found.", null));
		}

		loginAttemptService.clearLoginAttempts(userToUnlock.getUsername());
		adminLogService.log(getSystemUsername(), "UNLOCK_USER_API",
				"User account '" + userToUnlock.getUsername() + "' (ID: " + id + ") unlocked via API.");
		return ResponseEntity.ok(new ApiResponse(true,
				"User account '" + userToUnlock.getUsername() + "' has been unlocked.", Map.of("unlockedUserId", id)));
	}

	private String generateRandomPassword(int length) {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
		SecureRandom random = new SecureRandom();
		return random.ints(length, 0, chars.length()).mapToObj(chars::charAt)
				.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
	}
}