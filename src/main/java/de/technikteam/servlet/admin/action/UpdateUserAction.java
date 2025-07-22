package de.technikteam.servlet.admin.action;

import de.technikteam.dao.UserDAO;
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
	private final UserService userService = new UserService();

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response)
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
			session.setAttribute("errorMessage", "Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
			return "redirect:/admin/mitglieder";
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
			if (adminUser.getId() == userId) {
				User refreshedUserInSession = userDAO.getUserById(userId);
				refreshedUserInSession.setPermissions(userDAO.getPermissionsForUser(userId));
				session.setAttribute("user", refreshedUserInSession);
				session.setAttribute("navigationItems",
						NavigationRegistry.getNavigationItemsForUser(refreshedUserInSession));
			}
			AdminLogService.log(adminUser.getUsername(), "UPDATE_USER",
					"Benutzer '" + originalUser.getUsername() + "' (ID: " + userId + ") aktualisiert.");
			session.setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
		} else {
			session.setAttribute("infoMessage",
					"Keine Änderungen an den Benutzerdaten vorgenommen oder ein Fehler ist aufgetreten.");
		}

		return "redirect:/admin/mitglieder";
	}
}