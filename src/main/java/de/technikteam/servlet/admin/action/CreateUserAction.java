package de.technikteam.servlet.admin.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.RoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.UserService;
import de.technikteam.util.PasswordPolicyValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@Singleton
public class CreateUserAction implements Action {

	private final UserService userService;
	private final RoleDAO roleDAO;

	@Inject
	public CreateUserAction(UserService userService, RoleDAO roleDAO) {
		this.userService = userService;
		this.roleDAO = roleDAO;
	}

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
			return new ApiResponse(false, "Fehler beim Erstellen des Benutzers: " + validationResult.getMessage(),
					null);
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

		int newUserId = userService.createUserWithPermissions(newUser, pass, permissionIds, adminUser.getUsername());
		if (newUserId > 0) {
			newUser.setId(newUserId);
			roleDAO.getAllRoles().stream().filter(role -> role.getId() == newUser.getRoleId()).findFirst()
					.ifPresent(role -> newUser.setRoleName(role.getRoleName()));

			return new ApiResponse(true, "Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.", newUser);
		} else {
			return new ApiResponse(false,
					"Benutzer konnte nicht erstellt werden (ggf. existiert der Name oder die E-Mail bereits).", null);
		}
	}
}