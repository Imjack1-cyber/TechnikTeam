// src/main/java/de/technikteam/servlet/EventDetailsServlet.java
package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.*;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class EventDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventDetailsServlet.class);

	// DAOs are no longer needed here as data is fetched by the API

	@Inject
	public EventDetailsServlet() {
		// Constructor is now empty
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		String eventIdParam = request.getParameter("id");
		if (eventIdParam == null || eventIdParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required 'id' parameter.");
			return;
		}

		// The servlet now only validates the ID format and forwards to the JSP shell.
		// The JSP's JavaScript will handle fetching and authorization.
		try {
			int eventId = Integer.parseInt(eventIdParam);
			request.setAttribute("eventId", eventId);
			request.getRequestDispatcher("/views/public/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		}
	}
}