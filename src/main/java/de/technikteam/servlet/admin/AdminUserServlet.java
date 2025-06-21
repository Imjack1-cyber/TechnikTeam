package de.technikteam.servlet.admin;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * A critical and complex servlet mapped to /admin/users. It is the central point for user management, handling listing all users, displaying a detailed view of a single user (including their qualifications and event history), and processing POST requests for creating, updating, deleting users, and updating their qualifications. It uses admin_users.jsp and admin_user_details.jsp.
 */

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminUserServlet.class);

	private UserDAO userDAO;
	private UserQualificationsDAO userQualificationsDAO;
	private EventDAO eventDAO;
	private CourseDAO courseDAO; 

	@Override
	public void init() {
		userDAO = new UserDAO();
		userQualificationsDAO = new UserQualificationsDAO();
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO(); 
		logger.info("AdminUserServlet initialized.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action") == null ? "list" : request.getParameter("action");
		logger.debug("doGet received action: {}", action);

		try {
			switch (action) {
			case "details":
				showUserDetails(request, response);
				break;
			default:
				listUsers(request, response);
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doGet", e);
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		logger.debug("doPost received action: {}", action);

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
				logger.warn("Unknown POST action received: {}", action);
				response.sendRedirect(request.getContextPath() + "/admin/users");
				break;
			}
		} catch (Exception e) {
			logger.error("Error in AdminUserServlet doPost", e);
			request.getSession().setAttribute("errorMessage", "Ein schwerwiegender Fehler ist aufgetreten.");
			response.sendRedirect(request.getContextPath() + "/admin/users");
		}
	}

	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("Listing all users.");
		List<User> userList = userDAO.getAllUsers();
		request.setAttribute("userList", userList);
		request.getRequestDispatcher("/admin/admin_users.jsp").forward(request, response);
	}

	private void showUserDetails(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userId = Integer.parseInt(request.getParameter("id"));
		logger.debug("Showing details for user ID: {}", userId);
		User user = userDAO.getUserById(userId);
		List<UserQualification> qualifications = userQualificationsDAO.getQualificationsForUser(userId);
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(userId);

		request.setAttribute("userToEdit", user);
		request.setAttribute("qualifications", qualifications);
		request.setAttribute("eventHistory", eventHistory);

		request.getRequestDispatcher("/admin/admin_user_details.jsp").forward(request, response);
	}

	private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    int userId = Integer.parseInt(request.getParameter("userId"));
	    User adminUser = (User) request.getSession().getAttribute("user"); // Get admin user at the start

	    // 1. Fetch the original user state BEFORE any changes
	    User originalUser = userDAO.getUserById(userId);
	    if (originalUser == null) {
	        request.getSession().setAttribute("errorMessage", "Fehler: Benutzer mit ID " + userId + " nicht gefunden.");
	        response.sendRedirect(request.getContextPath() + "/admin/users");
	        return;
	    }

	    // 2. Get the new values from the form
	    String newUsername = request.getParameter("username").trim();
	    String newRole = request.getParameter("role");
	    int newClassYear = 0;
	    try {
	        newClassYear = Integer.parseInt(request.getParameter("classYear"));
	    } catch (NumberFormatException e) { /* default to 0 */ }
	    String newClassName = request.getParameter("className");

	    // 3. Compare fields and build a list of changes
	    List<String> changes = new ArrayList<>();
	    if (!originalUser.getUsername().equals(newUsername)) {
	        changes.add("Benutzername von '" + originalUser.getUsername() + "' zu '" + newUsername + "'");
	    }
	    if (!originalUser.getRole().equals(newRole)) {
	        changes.add("Rolle von '" + originalUser.getRole() + "' zu '" + newRole + "'");
	    }
	    if (originalUser.getClassYear() != newClassYear) {
	        changes.add("Jahrgang von '" + originalUser.getClassYear() + "' zu '" + newClassYear + "'");
	    }
	    // Handle potential null values for className
	    if (!Objects.equals(originalUser.getClassName(), newClassName)) {
	        changes.add("Klasse von '" + originalUser.getClassName() + "' zu '" + newClassName + "'");
	    }

	    // 4. If there are changes, update the user and log them
	    if (!changes.isEmpty()) {
	        originalUser.setUsername(newUsername);
	        originalUser.setRole(newRole);
	        originalUser.setClassYear(newClassYear);
	        originalUser.setClassName(newClassName);

	        if (userDAO.updateUser(originalUser)) {
	            String logDetails = "Benutzer '" + originalUser.getUsername() + "' (ID: " + userId + ") aktualisiert. Änderungen: " + String.join(", ", changes) + ".";
	            AdminLogService.log(adminUser.getUsername(), "UPDATE_USER", logDetails);
	            request.getSession().setAttribute("successMessage", "Benutzerdaten erfolgreich aktualisiert.");
	        } else {
	            request.getSession().setAttribute("errorMessage", "Fehler: Benutzerdaten konnten nicht in der DB aktualisiert werden.");
	        }
	    } else {
	        request.getSession().setAttribute("successMessage", "Keine Änderungen vorgenommen.");
	    }

	    response.sendRedirect(request.getContextPath() + "/admin/users?action=details&id=" + userId);
	}

	/**
	 * Handles the submission from the qualification editing modal in the matrix or
	 * details view. It creates, updates, or deletes a user's qualification record
	 * based on the selected status.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleUpdateQualification(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String redirectUrl = request.getContextPath() + "/admin/matrix"; // Default redirect
		int userId = 0; // Initialize to handle potential errors
		User adminUser = (User) request.getSession().getAttribute("user");

		try {
			String userIdStr = request.getParameter("userId");
			String courseIdStr = request.getParameter("courseId");
			String status = request.getParameter("status");
			String remarks = request.getParameter("remarks");
			String dateParam = request.getParameter("completionDate");

			logger.info("--- QUALIFICATION UPDATE RECEIVED ---");
			logger.info("Raw userId: '{}', courseId: '{}', status: '{}', date: '{}', remarks: '{}'", userIdStr,
					courseIdStr, status, dateParam, remarks);

			userId = Integer.parseInt(userIdStr);
			int courseId = Integer.parseInt(courseIdStr);

			LocalDate completionDate = null;
			if (dateParam != null && !dateParam.isEmpty()) {
				completionDate = LocalDate.parse(dateParam);
			}

			if (userQualificationsDAO.updateQualificationStatus(userId, courseId, status, completionDate, remarks)) {
				String courseName = courseDAO.getCourseById(courseId).getName();
				String userName = userDAO.getUserById(userId).getUsername();

				String details = String.format("Qualifikation für Nutzer '%s' (ID: %d) aktualisiert. Lehrgang: '%s' (ID: %d). Neuer Status: '%s', Abschlussdatum: '%s', Bemerkungen: '%s'.",
				    userName, userId, courseName, courseId, status, (completionDate != null ? completionDate.toString() : "N/A"), remarks);

				AdminLogService.log(adminUser.getUsername(), "UPDATE_QUALIFICATION", details);

				request.getSession().setAttribute("successMessage", "Qualifikation erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage", "Qualifikation konnte nicht aktualisiert werden.");
			}

			if ("userDetails".equals(request.getParameter("returnTo"))) {
				redirectUrl = request.getContextPath() + "/admin/users?action=details&id=" + userId;
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid ID format during qualification update.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID-Parameter empfangen.");
		} catch (Exception e) {
			logger.error("A generic error occurred during qualification update.", e);
			request.getSession().setAttribute("errorMessage", "Ein unerwarteter Fehler ist aufgetreten.");
		}

		response.sendRedirect(redirectUrl);
	}
	
	// In: src/main/java/de/technikteam/servlet/admin/AdminUserServlet.java

	/**
	 * Handles the creation of a new user from the form on the user list page.
	 * It validates input, creates the user, logs the action, and provides
	 * user feedback via session attributes.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleCreateUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    String username = request.getParameter("username");
	    String pass = request.getParameter("password");
	    String role = request.getParameter("role");

	    // --- Input Validation ---
	    if (username == null || username.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
	        request.getSession().setAttribute("errorMessage", "Benutzername und Passwort dürfen nicht leer sein.");
	        response.sendRedirect(request.getContextPath() + "/admin/users");
	        return;
	    }

	    User newUser = new User();
	    newUser.setUsername(username.trim());
	    newUser.setRole(role);
	    // These fields are not on the simple creation form, so they default to null/0.

	    int newUserId = userDAO.createUser(newUser, pass);

	    if (newUserId > 0) {
	        User adminUser = (User) request.getSession().getAttribute("user");
	        // GERMAN LOG:
	        String logDetails = "Benutzer '" + newUser.getUsername() + "' (ID: " + newUserId + ") wurde erstellt.";
	        AdminLogService.log(adminUser.getUsername(), "BENUTZER_ERSTELLT", logDetails);
	        
	        request.getSession().setAttribute("successMessage", "Benutzer '" + newUser.getUsername() + "' erfolgreich erstellt.");
	        logger.info("Admin '{}' created new user '{}' with ID {}", adminUser.getUsername(), newUser.getUsername(), newUserId);
	    } else {
	        request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht erstellt werden (ggf. existiert der Name bereits).");
	        logger.error("Failed to create new user '{}'", username);
	    }

	    response.sendRedirect(request.getContextPath() + "/admin/users");
	}

	/**
	 * Handles the deletion of a user. Includes a critical security check to
	 * prevent an administrator from deleting their own account.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws IOException If an I/O error occurs.
	 */
	private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
	    try {
	        int userIdToDelete = Integer.parseInt(request.getParameter("userId"));
	        User loggedInAdmin = (User) request.getSession().getAttribute("user");

	        // --- CRITICAL SECURITY CHECK: Prevent self-deletion ---
	        if (loggedInAdmin != null && loggedInAdmin.getId() == userIdToDelete) {
	            logger.warn("Admin '{}' (ID: {}) attempted to delete themselves. Operation denied.", loggedInAdmin.getUsername(), loggedInAdmin.getId());
	            request.getSession().setAttribute("errorMessage", "Sie können sich nicht selbst löschen.");
	            response.sendRedirect(request.getContextPath() + "/admin/users");
	            return;
	        }

	        // Fetch user details *before* deleting to use in the log message.
	        User userToDelete = userDAO.getUserById(userIdToDelete);
	        
	        if (userDAO.deleteUser(userIdToDelete)) {
	            // Check if we successfully fetched the user for a more detailed log.
	            if (userToDelete != null) {
	                // GERMAN LOG:
	                String logDetails = "Benutzer '" + userToDelete.getUsername() + "' (ID: " + userIdToDelete + ") wurde gelöscht.";
	                AdminLogService.log(loggedInAdmin.getUsername(), "BENUTZER_GELÖSCHT", logDetails);
	                logger.info("Admin '{}' deleted user '{}' (ID: {})", loggedInAdmin.getUsername(), userToDelete.getUsername(), userIdToDelete);
	            } else {
	                // Fallback log if user couldn't be fetched beforehand.
	                AdminLogService.log(loggedInAdmin.getUsername(), "BENUTZER_GELÖSCHT", "Benutzer mit ID " + userIdToDelete + " wurde gelöscht.");
	                logger.info("Admin '{}' deleted user with ID {}", loggedInAdmin.getUsername(), userIdToDelete);
	            }
	            request.getSession().setAttribute("successMessage", "Benutzer erfolgreich gelöscht.");
	        } else {
	            logger.error("Failed to delete user with ID: {}. DAO returned false.", userIdToDelete);
	            request.getSession().setAttribute("errorMessage", "Benutzer konnte nicht gelöscht werden.");
	        }
	    } catch (NumberFormatException e) {
	        logger.error("Invalid user ID format for deletion.", e);
	        request.getSession().setAttribute("errorMessage", "Ungültige Benutzer-ID.");
	    }
	    
	    response.sendRedirect(request.getContextPath() + "/admin/users");
	}
}