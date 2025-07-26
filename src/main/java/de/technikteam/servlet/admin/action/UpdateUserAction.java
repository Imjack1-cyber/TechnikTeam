package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import de.technikteam.service.UserService;
import de.technikteam.util.NavigationRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Objects;

@Singleton
public class UpdateUserAction implements Action {
	private final UserDAO userDAO;
	private final RoleDAO roleDAO;
	private final UserService userService;
	private final AdminLogService adminLogService;

	@Inject
	public UpdateUserAction(UserDAO userDAO, RoleDAO roleDAO, UserService userService,
			AdminLogService adminLogService) {
		this.userDAO = userDAO;
		this.roleDAO = roleDAO;
		this.userService = userService;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		int userId = Integer.parseInt(request.getParameter("userId"));
		User adminUser = (User) session.getAttribute("user");

		if (!adminUser.getPermissions().contains("USER_UPDATE") && !adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		User originalUser = userDAO.getUserById(userId);
		if (originalUser == null) {
			return new ApiResponse(false, "Fehler: Benutzer mit ID " + userId + " nicht gefunden.", null);
		}

		StringBuilder changes = new StringBuilder();

		User updatedUser = new User();
		updatedUser.setId(userId);

		String newUsername = request.getParameter("username").trim();
		if (!Objects.equals(originalUser.getUsername(), newUsername)) {
			changes.append(String.format("Benutzername von '%s' zu '%s', ", originalUser.getUsername(), newUsername));
		}
		updatedUser.setUsername(newUsername);

		int roleId = Integer.parseInt(request.getParameter("roleId"));
		if (originalUser.getRoleId() != roleId) {
			changes.append(String.format("Rolle-ID von '%d' zu '%d', ", originalUser.getRoleId(), roleId));
		}
		updatedUser.setRoleId(roleId);

		String newClassName = request.getParameter("className");
		if (!Objects.equals(originalUser.getClassName(), newClassName)) {
			changes.append(String.format("Klasse von '%s' zu '%s', ", originalUser.getClassName(), newClassName));
		}
		updatedUser.setClassName(newClassName);

		String newEmail = request.getParameter("email");
		newEmail = (newEmail != null && !newEmail.trim().isEmpty()) ? newEmail.trim() : null;
		if (!Objects.equals(originalUser.getEmail(), newEmail)) {
			changes.append("E-Mail geändert, ");
		}
		updatedUser.setEmail(newEmail);

		try {
			int newClassYear = Integer.parseInt(request.getParameter("classYear"));
			if (originalUser.getClassYear() != newClassYear) {
				changes.append(String.format("Jahrgang von '%d' zu '%d', ", originalUser.getClassYear(), newClassYear));
			}
			updatedUser.setClassYear(newClassYear);
		} catch (NumberFormatException e) {
			updatedUser.setClassYear(0);
		}

		String[] permissionIds = request.getParameterValues("permissionIds");

		boolean success = userService.updateUserWithPermissions(updatedUser, permissionIds);

		if (success) {
			User refreshedUser = userDAO.getUserById(userId);
			roleDAO.getAllRoles().stream().filter(role -> role.getId() == refreshedUser.getRoleId()).findFirst()
					.ifPresent(role -> refreshedUser.setRoleName(role.getRoleName()));

			if (adminUser.getId() == userId) {
				refreshedUser.setPermissions(userDAO.getPermissionsForUser(userId));
				session.setAttribute("user", refreshedUser);
				session.setAttribute("navigationItems", NavigationRegistry.getNavigationItemsForUser(refreshedUser));
			}

			String logDetails = String.format("Benutzer '%s' (ID: %d) aktualisiert.", originalUser.getUsername(),
					userId);
			if (changes.length() > 2) {
				logDetails += " Änderungen: " + changes.substring(0, changes.length() - 2) + ".";
			}

			adminLogService.log(adminUser.getUsername(), "UPDATE_USER", logDetails);

			NotificationService.getInstance().broadcastUIUpdate("user_updated", refreshedUser);

			return new ApiResponse(true, "Benutzerdaten erfolgreich aktualisiert.", refreshedUser);
		} else {
			return new ApiResponse(false,
					"Keine Änderungen an den Benutzerdaten vorgenommen oder ein Fehler ist aufgetreten.", null);
		}
	}
}