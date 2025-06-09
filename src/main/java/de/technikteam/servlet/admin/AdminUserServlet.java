package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;
import de.technikteam.service.AdminLogService;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	// DAOs for database access
	private UserDAO userDAO;
	private UserQualificationsDAO userQualificationsDAO;
	// We need EventDAO to show the user's history on the details page
	private de.technikteam.dao.EventDAO eventDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
		userQualificationsDAO = new UserQualificationsDAO();
		eventDAO = new de.technikteam.dao.EventDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		try {
			switch (action) {
			case "details":
				showUserDetails(request, response);
				break;
			case "list":
			default:
				listUsers(request, response);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in doGet of AdminUserServlet", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		if (action == null) {
			response.sendRedirect(request.getContextPath() + "/admin/users");
			return;
		}

		try {
			switch (action) {
			case "create":
				handleCreateUser(request, response);
				break;
			case "update":
				handleUpdateUser(request, response);
				break;
			case "delete":
				handleDeleteUser(request, response);
				break;
			case "updateQualification":
				handleUpdateQualification(request, response);
				break;
			default:
				response.sendRedirect(request.getContextPath() + "/admin/users");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in doPost of AdminUserServlet", e);
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> userList = userDAO.getAllUsers();
		request.setAttribute("userList", userList);
		request.getRequestDispatcher("/admin/admin_users.jsp").forward(request, response);
	}

	private void showUserDetails(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("id"));
		User user = userDAO.getUserById(userId);
		List<UserQualification> qualifications = userQualificationsDAO.getQualificationsForUser(userId);
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);

		request.setAttribute("userToEdit", user);
		request.setAttribute("qualifications", qualifications);
		request.setAttribute("eventHistory", eventHistory);

		request.getRequestDispatcher("/admin/admin_user_details.jsp").forward(request, response);
	}

	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User newUser = new User();
		newUser.setUsername(request.getParameter("username"));
		newUser.setRole(request.getParameter("role"));
		newUser.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		newUser.setClassName(request.getParameter("className"));
		String pass = request.getParameter("password");

		if (userDAO.createUser(newUser, pass)) {
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich erstellt.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht erstellt werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		User user = userDAO.getUserById(userId); // Get existing user to preserve createdAt

		user.setUsername(request.getParameter("username"));
		user.setRole(request.getParameter("role"));
		user.setClassYear(Integer.parseInt(request.getParameter("classYear")));
		user.setClassName(request.getParameter("className"));

		if (userDAO.updateUser(user)) {
			request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzerdaten konnten nicht aktualisiert werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users?action=details&id=" + userId);
	}

	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		User adminUser = (User) request.getSession().getAttribute("user");
		User userToDelete = userDAO.getUserById(userId);

		if (userDAO.deleteUser(userId)) {
			AdminLogService.log(adminUser, "BENUTZER GELÖSCHT", userToDelete.getUsername());
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleUpdateQualification(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String redirectUrl = request.getContextPath() + "/admin/matrix";
		try {
			int userId = Integer.parseInt(request.getParameter("userId"));
			int courseId = Integer.parseInt(request.getParameter("courseId"));
			String status = request.getParameter("status");
			String remarks = request.getParameter("remarks");

			LocalDate completionDate = null;
			String dateParam = request.getParameter("completionDate");
			if (dateParam != null && !dateParam.isEmpty()) {
				completionDate = LocalDate.parse(dateParam);
			}

			logger.info("Admin updating qualification for user: {}, course: {}, new status: {}", userId, courseId,
					status);

			boolean success = false;
			if ("NONE".equals(status)) {
				success = userQualificationsDAO.deleteQualification(userId, courseId);
			} else if (userQualificationsDAO.qualificationExists(userId, courseId)) {
				success = userQualificationsDAO.updateQualificationStatus(userId, courseId, status, completionDate,
						remarks);
			} else {
				success = userQualificationsDAO.createQualification(userId, courseId, status, completionDate, remarks);
			}

			if (success) {
				request.getSession().setAttribute("successMessage", "Qualifikation erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Operation an der Qualifikation ist fehlgeschlagen.");
			}
		} catch (Exception e) {
			logger.error("Error during qualification update.", e);
			request.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten.");
		}
		response.sendRedirect(redirectUrl);
	}
}