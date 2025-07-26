// src/main/java/de/technikteam/api/v1/public/PublicEventListResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class PublicEventListResource extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final EventDAO eventDAO;
    private final Gson gson;

    @Inject
    public PublicEventListResource(EventDAO eventDAO, Gson gson) {
        this.eventDAO = eventDAO;
        this.gson = gson;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        List<Event> allUpcomingEvents = eventDAO.getAllActiveAndUpcomingEvents();
		List<Event> qualifiedEvents = eventDAO.getUpcomingEventsForUser(user, 0);
		List<Integer> qualifiedEventIds = qualifiedEvents.stream().map(Event::getId).collect(Collectors.toList());

		for (Event event : allUpcomingEvents) {
			event.setUserQualified(qualifiedEventIds.contains(event.getId()));
			qualifiedEvents.stream().filter(qe -> qe.getId() == event.getId()).findFirst()
					.ifPresent(qe -> event.setUserAttendanceStatus(qe.getUserAttendanceStatus()));
			if (event.getUserAttendanceStatus() == null) {
				event.setUserAttendanceStatus("OFFEN");
			}
		}

        sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Events retrieved", allUpcomingEvents));
    }

    private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(apiResponse));
            out.flush();
        }
    }

    private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
    }
}