package de.technikteam.servlet.admin.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Singleton
public class CrewFinderApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CrewFinderApiServlet.class);
	private final EventDAO eventDAO;
	private final Gson gson;

	@Inject
	public CrewFinderApiServlet(EventDAO eventDAO) {
		this.eventDAO = eventDAO;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User currentUser = (User) request.getSession().getAttribute("user");
		String eventIdParam = request.getParameter("eventId");

		if (eventIdParam == null || eventIdParam.trim().isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing eventId parameter.");
			return;
		}

		try {
			int eventId = Integer.parseInt(eventIdParam);
			Event event = eventDAO.getEventById(eventId);
			if (event == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event not found.");
				return;
			}

			boolean hasPermission = currentUser.getPermissions().contains("EVENT_MANAGE_ASSIGNMENTS")
					|| currentUser.hasAdminAccess() || currentUser.getId() == event.getLeaderUserId();

			if (!hasPermission) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
				return;
			}

			List<User> qualifiedUsers = eventDAO.getQualifiedAndAvailableUsersForEvent(eventId);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(gson.toJson(qualifiedUsers));

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid eventId format.");
		}
	}
}