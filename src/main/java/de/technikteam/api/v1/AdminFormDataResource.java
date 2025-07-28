package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Permission;
import de.technikteam.model.Role;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AdminFormDataResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFormDataResource.class);

	private final RoleDAO roleDAO;
	private final PermissionDAO permissionDAO;
	private final Gson gson;

	@Inject
	public AdminFormDataResource(RoleDAO roleDAO, PermissionDAO permissionDAO, Gson gson) {
		this.roleDAO = roleDAO;
		this.permissionDAO = permissionDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User user = (User) req.getAttribute("user");
		if (user == null || !user.hasAdminAccess()) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		try {
			List<Role> roles = roleDAO.getAllRoles();
			List<Permission> allPermissions = permissionDAO.getAllPermissions();

			// Group permissions by a simple category derived from the key
			Map<String, List<Permission>> groupedPermissions = allPermissions.stream()
					.collect(Collectors.groupingBy(p -> {
						String key = p.getPermissionKey();
						if (key.contains("_")) {
							return key.substring(0, key.indexOf("_"));
						}
						return "SYSTEM";
					}));

			Map<String, Object> formData = Map.of("roles", roles, "groupedPermissions", groupedPermissions);

			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Form data retrieved.", formData));

		} catch (Exception e) {
			logger.error("Error fetching admin form data", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve form data.");
		}
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}