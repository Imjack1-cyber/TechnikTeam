package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final RoleDAO roleDAO;
	private final PermissionDAO permissionDAO;
	private final Gson gson;

	@Inject
	public AdminUserServlet(UserDAO userDAO, EventDAO eventDAO, RoleDAO roleDAO, PermissionDAO permissionDAO) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.roleDAO = roleDAO;
		this.permissionDAO = permissionDAO;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User currentUser = (User) request.getSession().getAttribute("user");
		if (!currentUser.getPermissions().contains("USER_READ") && !currentUser.hasAdminAccess()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		try {
			switch (action) {
			case "details":
				showUserDetails(request, response);
				break;
			case "getUserData":
				getUserDataAsJson(request, response);
				break;
			default:
				listUsers(request, response);
				break;
			}
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige ID angegeben.");
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doGet", e);
			request.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
			response.sendRedirect(request.getContextPath() + "/admin/dashboard");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> userList = userDAO.getAllUsers();
		List<Role> allRoles = roleDAO.getAllRoles();
		List<Permission> allPermissions = permissionDAO.getAllPermissions();

		Map<String, List<Permission>> groupedPermissions = allPermissions.stream().collect(Collectors.groupingBy(p -> {
			String key = p.getPermissionKey();
			int underscoreIndex = key.indexOf('_');
			return (underscoreIndex != -1) ? key.substring(0, underscoreIndex) : "ALLGEMEIN";
		}, LinkedHashMap::new, Collectors.toList()));

		request.setAttribute("userList", userList);
		request.setAttribute("allRoles", allRoles);
		request.setAttribute("groupedPermissionsJson", gson.toJson(groupedPermissions));
		request.getRequestDispatcher("/views/admin/admin_users.jsp").forward(request, response);
	}

	private void getUserDataAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int userId = Integer.parseInt(req.getParameter("id"));
		User user = userDAO.getUserById(userId);
		if (user != null) {
			Set<Integer> permissionIds = permissionDAO.getPermissionIdsForUser(userId);
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("user", user);
			responseData.put("permissionIds", permissionIds);

			String userJson = gson.toJson(responseData);
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(userJson);
		} else {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
		}
	}

	private void showUserDetails(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("id"));
		User user = userDAO.getUserById(userId);
		if (user == null) {
			request.getSession().setAttribute("errorMessage", "Benutzer nicht gefunden.");
			response.sendRedirect(request.getContextPath() + "/admin/mitglieder");
			return;
		}
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);
		request.setAttribute("userToView", user);
		request.setAttribute("eventHistory", eventHistory);
		request.getRequestDispatcher("/views/admin/admin_user_details.jsp").forward(request, response);
	}
}