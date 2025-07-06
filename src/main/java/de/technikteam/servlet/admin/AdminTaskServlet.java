package de.technikteam.servlet.admin;

import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

@WebServlet("/admin/tasks")
public class AdminTaskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminTaskServlet.class);
	private EventTaskDAO taskDAO;

	@Override
	public void init() {
		taskDAO = new EventTaskDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String action = request.getParameter("action");
		User adminUser = (User) request.getSession().getAttribute("user");
		int eventId = Integer.parseInt(request.getParameter("eventId"));

		try {
			switch (action) {
			case "save":
				handleSaveTask(request, adminUser, eventId);
				break;
			case "delete":
				handleDelete(request, adminUser);
				break;
			default:
				logger.warn("Unknown POST action received: {}", action);
				break;
			}
		} catch (Exception e) {
			logger.error("Error processing admin task request", e);
			request.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
		}
		response.sendRedirect(request.getContextPath() + "/veranstaltungen/details?id=" + eventId);
	}

	private void handleSaveTask(HttpServletRequest request, User adminUser, int eventId) {
		boolean isUpdate = !request.getParameter("taskId").isEmpty();
		EventTask task = new EventTask();
		task.setEventId(eventId);
		task.setDescription(request.getParameter("description"));
		task.setDetails(request.getParameter("details")); // Set details
		task.setDisplayOrder(Integer.parseInt(request.getParameter("displayOrder")));

		if (isUpdate) {
			task.setId(Integer.parseInt(request.getParameter("taskId")));
			task.setStatus(request.getParameter("status"));
		}

		String assignmentType = request.getParameter("assignmentType");
		int[] userIds = null;
		if ("direct".equals(assignmentType)) {
			String[] userIdsStr = request.getParameterValues("userIds");
			userIds = userIdsStr == null ? new int[0] : Arrays.stream(userIdsStr).mapToInt(Integer::parseInt).toArray();
			task.setRequiredPersons(0);
		} else { // "pool"
			task.setRequiredPersons(Integer.parseInt(request.getParameter("requiredPersons")));
		}

		String[] itemIds = request.getParameterValues("itemIds");
		String[] itemQuantities = request.getParameterValues("itemQuantities");
		String[] kitIds = request.getParameterValues("kitIds");

		int taskId = taskDAO.saveTask(task, userIds, itemIds, itemQuantities, kitIds);

		if (taskId > 0) {
			String logAction = isUpdate ? "UPDATE_TASK" : "CREATE_TASK";
			String logDetails = String.format("Aufgabe '%s' (ID: %d) für Event-ID %d %s.", task.getDescription(),
					taskId, eventId, isUpdate ? "aktualisiert" : "erstellt");
			AdminLogService.log(adminUser.getUsername(), logAction, logDetails);
			request.getSession().setAttribute("successMessage", "Aufgabe erfolgreich gespeichert.");
		} else {
			request.getSession().setAttribute("errorMessage", "Fehler beim Speichern der Aufgabe.");
		}
	}

	private void handleDelete(HttpServletRequest request, User adminUser) {
		int taskId = Integer.parseInt(request.getParameter("taskId"));
		logger.warn("Admin '{}' attempting to delete task ID {}", adminUser.getUsername(), taskId);
		if (taskDAO.deleteTask(taskId)) {
			AdminLogService.log(adminUser.getUsername(), "DELETE_TASK", "Aufgabe mit ID " + taskId + " gelöscht.");
			request.getSession().setAttribute("successMessage", "Aufgabe erfolgreich gelöscht.");
		} else {
			request.getSession().setAttribute("errorMessage", "Aufgabe konnte nicht gelöscht werden.");
		}
	}
}