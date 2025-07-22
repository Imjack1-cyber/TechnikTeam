package de.technikteam.servlet.admin.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.RoleDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.UserService;
import de.technikteam.util.PasswordPolicyValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CreateUserAction implements Action {

	private final UserService userService = new UserService();
	private final RoleDAO roleDAO = new RoleDAO();
	private final Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			.create();

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		User adminUser = (User) session.getAttribute("user");
		boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

		if (!adminUser.getPermissions().contains("USER_CREATE") && !adminUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return null;
		}

		String username = request.getParameter("username");
		String pass = request.getParameter("password");

		PasswordPolicyValidator.ValidationResult validationResult = PasswordPolicyValidator.validate(pass);
		if (!validationResult.isValid()) {
			return handleFailure("Fehler beim Erstellen des Benutzers: " + validationResult.getMessage(), isAjax,
					request, response);
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

			return handleSuccess("Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.", newUser, isAjax,
					request, response);
		} else {
			return handleFailure(
					"Benutzer konnte nicht erstellt werden (ggf. existiert der Name oder die E-Mail bereits).", isAjax,
					request, response);
		}
	}

	private String handleSuccess(String message, User newUser, boolean isAjax, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (isAjax) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			Map<String, Object> jsonResponse = new HashMap<>();
			jsonResponse.put("success", true);
			jsonResponse.put("message", message);
			jsonResponse.put("newUser", newUser);
			out.print(gson.toJson(jsonResponse));
			out.flush();
			return null; // Null indicates the response is handled
		} else {
			request.getSession().setAttribute("successMessage", message);
			return "redirect:/admin/mitglieder";
		}
	}

	private String handleFailure(String message, boolean isAjax, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (isAjax) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			PrintWriter out = response.getWriter();
			Map<String, Object> jsonResponse = new HashMap<>();
			jsonResponse.put("success", false);
			jsonResponse.put("message", message);
			out.print(gson.toJson(jsonResponse));
			out.flush();
			return null;
		} else {
			request.getSession().setAttribute("errorMessage", message);
			return "redirect:/admin/mitglieder";
		}
	}
}