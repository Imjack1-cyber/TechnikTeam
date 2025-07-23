package de.technikteam.servlet.admin.action;

import de.technikteam.dao.RoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.UserService;
import de.technikteam.util.PasswordPolicyValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class CreateUserAction implements Action {

	private final UserService userService = new UserService();
	private final RoleDAO roleDAO = new RoleDAO();

	@Override
	public ApiResponse execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");

		if (!adminUser.getPermissions().contains("USER_CREATE") && !adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		String username = request.getParameter("username");
		String pass = request.getParameter("password");

		PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(pass);
		if (!validationResult.isValid()) {
			return ApiResponse.error("Fehler beim Erstellen des Benutzers: " + validationResult.getMessage());
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

		int newUserId = userService.createUserWithPermissions(newUser, pass, permissionIds);
		if (newUserId > 0) {
			newUser.setId(newUserId);
			// Enrich with role name for the JSON response
			roleDAO.getAllRoles().stream().filter(role -> role.getId() == newUser.getRoleId()).findFirst()
					.ifPresent(role -> newUser.setRoleName(role.getRoleName()));

			String logDetails = String.format(
					"Benutzer '%s' (ID: %d, Rolle-ID: %d, Klasse: %d %s) erstellt und Berechtigungen zugewiesen.",
					newUser.getUsername(), newUserId, newUser.getRoleId(), newUser.getClassYear(),
					newUser.getClassName());
			AdminLogService.log(adminUser.getUsername(), "CREATE_USER", logDetails);

			return ApiResponse.success("Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.", newUser);
		} else {
			return ApiResponse
					.error("Benutzer konnte nicht erstellt werden (ggf. existiert der Name oder die E-Mail bereits).");
		}
	}
}