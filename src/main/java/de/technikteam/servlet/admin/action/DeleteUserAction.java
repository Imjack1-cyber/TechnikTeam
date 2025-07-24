package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@Singleton
public class DeleteUserAction implements Action {
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;

	@Inject
	public DeleteUserAction(UserDAO userDAO, AdminLogService adminLogService) {
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
		User loggedInAdmin = (User) session.getAttribute("user");

		if (!loggedInAdmin.getPermissions().contains("USER_DELETE") && !loggedInAdmin.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		if (loggedInAdmin.getId() == userIdToDelete) {
			return ApiResponse.error("Sie können sich nicht selbst löschen.");
		}

		User userToDelete = userDAO.getUserById(userIdToDelete);
		if (userToDelete == null) {
			return ApiResponse.error("Benutzer mit ID " + userIdToDelete + " nicht gefunden.");
		}

		if (userToDelete.getPermissions().contains("ACCESS_ADMIN_PANEL")
				&& !loggedInAdmin.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			return ApiResponse.error("Sie haben keine Berechtigung, einen Haupt-Administrator zu löschen.");
		}

		String deletedUsername = userToDelete.getUsername();
		String deletedRoleName = userToDelete.getRoleName();

		if (userDAO.deleteUser(userIdToDelete)) {
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s) wurde gelöscht.", deletedUsername,
					userIdToDelete, deletedRoleName);
			adminLogService.log(loggedInAdmin.getUsername(), "DELETE_USER", logDetails);

			NotificationService.getInstance().broadcastUIUpdate("user_deleted", Map.of("userId", userIdToDelete));

			return ApiResponse.success("Benutzer erfolgreich gelöscht.", Map.of("deletedUserId", userIdToDelete));
		} else {
			return ApiResponse.error("Benutzer konnte nicht gelöscht werden.");
		}
	}
}