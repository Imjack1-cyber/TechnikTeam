package de.technikteam.servlet.admin.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * An API endpoint for finding qualified and available users for a specific
 * event. This is used by the "Crew Finder" feature in the event management
 * modal.
 */
@WebServlet("/api/admin/crew-finder")
public class CrewFinderApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CrewFinderApiServlet.class);
	private EventDAO eventDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
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

			// Security: User must have general assignment rights OR be the leader of this
			// specific event.
			boolean hasPermission = currentUser.getPermissions().contains("EVENT_MANAGE_ASSIGNMENTS")
					|| currentUser.getPermissions().contains("ACCESS_ADMIN_PANEL")
					|| currentUser.getId() == event.getLeaderUserId();

			if (!hasPermission) {
				logger.warn("User '{}' tried to find crew for event {} without permission.", currentUser.getUsername(),
						eventId);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
				return;
			}

			List<User> qualifiedUsers = eventDAO.getQualifiedAndAvailableUsersForEvent(eventId);
			logger.info("Found {} qualified and available users for event ID {}.", qualifiedUsers.size(), eventId);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(gson.toJson(qualifiedUsers));

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid eventId format.");
		}
	}
}