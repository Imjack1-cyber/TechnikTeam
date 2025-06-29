package de.technikteam.servlet;

import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Mapped to `/task-action`, this servlet handles task status updates initiated
 * by non-admin users, typically via AJAX from the event details page. It
 * processes a POST request to change a task's status (e.g., from "OFFEN" to
 * "ERLEDIGT") when a user checks a checkbox corresponding to their assigned
 * task.
 */
@WebServlet("/aufgaben/aktionen")
public class TaskActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(TaskActionServlet.class);
	private EventTaskDAO taskDAO;

	@Override
	public void init() {
		taskDAO = new EventTaskDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		try {
			int taskId = Integer.parseInt(request.getParameter("taskId"));
			String status = request.getParameter("status");
			logger.info("User '{}' is updating task ID {} to status '{}'", user.getUsername(), taskId, status);

			if (taskDAO.updateTaskStatus(taskId, status)) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				logger.error("Failed to update task status for task ID {}", taskId);
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Task status could not be updated.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format in task-action request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID.");
		}
	}
}