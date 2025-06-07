package de.technikteam.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.User;

@WebServlet("/event-action")
public class EventActionServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger(EventActionServlet.class);
	private EventDAO eventDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		int eventId = Integer.parseInt(request.getParameter("eventId"));

		if ("signup".equals(action)) {
			eventDAO.signUpForEvent(user.getId(), eventId);
		} else if ("signoff".equals(action)) {
			eventDAO.signOffFromEvent(user.getId(), eventId);
		}

		// Redirect back to the events page to see the result.
		response.sendRedirect(request.getContextPath() + "/events");
	}
}