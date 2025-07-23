package de.technikteam.servlet.admin.action;

import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.UserService;
import de.technikteam.util.NavigationRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class UpdateUserAction implements Action {
	private final UserDAO userDAO = new UserDAO();
	private final RoleDAO roleDAO = new RoleDAO();
	private final UserService userService = new UserService();

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
			return ApiResponse.error("Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
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

		boolean success = userService.updateUserWithPermissions(updatedUser, permissionIds);

		if (success) {
			User refreshedUser = userDAO.getUserById(userId);

			// Enrich with role name for the JSON response
			roleDAO.getAllRoles().stream().filter(role -> role.getId() == refreshedUser.getRoleId()).findFirst()
					.ifPresent(role -> refreshedUser.setRoleName(role.getRoleName()));


			if (adminUser.getId() == userId) {
				refreshedUser.setPermissions(userDAO.getPermissionsForUser(userId));
				session.setAttribute("user", refreshedUser);
				session.setAttribute("navigationItems",
						NavigationRegistry.getNavigationItemsForUser(refreshedUser));
			}
			AdminLogService.log(adminUser.getUsername(), "UPDATE_USER",
					"Benutzer '" + originalUser.getUsername() + "' (ID: " + userId + ") aktualisiert.");

			return ApiResponse.success("Benutzerdaten erfolgreich aktualisiert.", refreshedUser);
		} else {
			return ApiResponse.error("Keine Änderungen an den Benutzerdaten vorgenommen oder ein Fehler ist aufgetreten.");
		}
	}
}