package de.technikteam.servlet.admin.action;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class DeleteUserAction implements Action {
	private final UserDAO userDAO = new UserDAO();

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
		User loggedInAdmin = (User) session.getAttribute("user");

		if (!loggedInAdmin.getPermissions().contains("USER_DELETE") && !loggedInAdmin.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		if (loggedInAdmin.getId() == userIdToDelete) {
			session.setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
			return "redirect:/admin/mitglieder";
		}

		User userToDelete = userDAO.getUserById(userIdToDelete);
		if (userToDelete == null) {
			session.setAttribute("errorMessage", "Benutzer mit ID " + userIdToDelete + " nicht gefunden.");
			return "redirect:/admin/mitglieder";
		}

		if (userToDelete.getPermissions().contains("ACCESS_ADMIN_PANEL")
				&& !loggedInAdmin.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			session.setAttribute("errorMessage", "Sie haben keine Berechtigung, einen Haupt-Administrator zu löschen.");
			return "redirect:/admin/mitglieder";
		}

		String deletedUsername = userToDelete.getUsername();
		String deletedRoleName = userToDelete.getRoleName();

		if (userDAO.deleteUser(userIdToDelete)) {
			String logDetails = String.format("Benutzer '%s' (ID: %d, Rolle: %s) wurde gelöscht.", deletedUsername,
					userIdToDelete, deletedRoleName);
			AdminLogService.log(loggedInAdmin.getUsername(), "DELETE_USER", logDetails);
			session.setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			session.setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}

		return "redirect:/admin/mitglieder";
	}
}