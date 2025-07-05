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

@WebServlet("/task-action")
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
		String action = request.getParameter("action");

		if (user == null || action == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		try {
			int taskId = Integer.parseInt(request.getParameter("taskId"));
			logger.info("User '{}' is performing action '{}' on task ID {}", user.getUsername(), action, taskId);

			switch (action) {
			case "updateStatus":
				String status = request.getParameter("status");
				if (taskDAO.updateTaskStatus(taskId, status)) {
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Status konnte nicht aktualisiert werden.");
				}
				break;
			case "claim":
				if (taskDAO.claimTask(taskId, user.getId())) {
					response.sendRedirect(request.getHeader("Referer"));
				} else {
					request.getSession().setAttribute("errorMessage",
							"Aufgabe konnte nicht übernommen werden (vielleicht schon voll?).");
					response.sendRedirect(request.getHeader("Referer"));
				}
				break;
			case "unclaim":
				if (taskDAO.unclaimTask(taskId, user.getId())) {
					response.sendRedirect(request.getHeader("Referer"));
				} else {
					request.getSession().setAttribute("errorMessage", "Aufgabe konnte nicht zurückgegeben werden.");
					response.sendRedirect(request.getHeader("Referer"));
				}
				break;
			default:
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unbekannte Aktion.");
				break;
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid task ID format in request.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid task ID.");
		}
	}
}