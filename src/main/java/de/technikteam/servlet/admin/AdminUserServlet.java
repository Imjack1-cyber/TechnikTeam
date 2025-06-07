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

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);
	private UserDAO userDAO;
	private EventDAO eventDAO = new EventDAO();
	private UserQualificationsDAO userQualificationsDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
		userQualificationsDAO = new UserQualificationsDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		switch (action) {
		case "details":
			showUserDetails(request, response);
			break;
		default:
			listUsers(request, response);
			break;
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
		try {
			int userId = Integer.parseInt(request.getParameter("id"));
			User user = userDAO.getUserById(userId);
			List<UserQualification> qualifications = userQualificationsDAO.getQualificationsForUser(userId);
			List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId); // Neue Daten holen

			request.setAttribute("userToEdit", user);
			request.setAttribute("qualifications", qualifications);
			request.setAttribute("eventHistory", eventHistory); // Neue Daten an JSP übergeben

			request.getRequestDispatcher("/admin/admin_user_details.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			/* ... */ }
	}

	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User newUser = new User();
		newUser.setUsername(request.getParameter("username"));
		newUser.setRole(request.getParameter("role"));
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
		String username = request.getParameter("username");
		String role = request.getParameter("role");
		User user = new User(userId, username, role);

		if (userDAO.updateUser(user)) {
			request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzerdaten konnten nicht aktualisiert werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users?action=details&id=" + userId);
	}

	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		if (userDAO.deleteUser(userId)) {
			request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	private void handleUpdateQualification(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int userId = Integer.parseInt(request.getParameter("userId"));
		int courseId = Integer.parseInt(request.getParameter("courseId"));
		String status = request.getParameter("status");
		LocalDate completionDate = null;
		String dateParam = request.getParameter("completionDate");
		if (dateParam != null && !dateParam.isEmpty()) {
			completionDate = LocalDate.parse(dateParam);
		}

		if (userQualificationsDAO.updateQualificationStatus(userId, courseId, status, completionDate)) {
			request.getSession().setAttribute("successMessage", "Qualifikation erfolgreich aktualisiert.");
		} else {
			request.getSession().setAttribute("errorMessage", "Qualifikation konnte nicht aktualisiert werden.");
		}
		response.sendRedirect(request.getContextPath() + "/admin/users?action=details&id=" + userId);
	}
}