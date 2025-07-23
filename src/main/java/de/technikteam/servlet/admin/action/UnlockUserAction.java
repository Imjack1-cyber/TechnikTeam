package de.technikteam.servlet.admin.action;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.servlet.LoginServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

public class UnlockUserAction implements Action {

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");

		if (!adminUser.hasAdminAccess()) { // Only admins can unlock
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		String usernameToUnlock = request.getParameter("username");
		if (usernameToUnlock != null && !usernameToUnlock.isEmpty()) {
			LoginServlet.LoginAttemptManager.clearLoginAttempts(usernameToUnlock);
			AdminLogService.log(adminUser.getUsername(), "UNLOCK_USER_ACCOUNT",
					"Benutzerkonto '" + usernameToUnlock + "' manuell entsperrt.");
			return ApiResponse.success("Benutzerkonto '" + usernameToUnlock + "' wurde erfolgreich entsperrt.",
					Map.of("unlockedUsername", usernameToUnlock));
		} else {
			return ApiResponse.error("Benutzername zum Entsperren wurde nicht angegeben.");
		}
	}
}