// src/main/java/de/technikteam/api/v1/public/PublicDashboardResource.java
package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PublicDashboardResource extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(PublicDashboardResource.class);

    private final EventDAO eventDAO;
    private final EventTaskDAO eventTaskDAO;
    private final Gson gson;

    @Inject
    public PublicDashboardResource(EventDAO eventDAO, EventTaskDAO eventTaskDAO, Gson gson) {
        this.eventDAO = eventDAO;
        this.eventTaskDAO = eventTaskDAO;
        this.gson = gson;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
            return;
        }

        try {
            List<Event> assignedEvents = eventDAO.getAssignedEventsForUser(user.getId(), 5);
            List<EventTask> openTasks = eventTaskDAO.getOpenTasksForUser(user.getId());
            List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 5);

            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("assignedEvents", assignedEvents);
            dashboardData.put("openTasks", openTasks);
            dashboardData.put("upcomingEvents", upcomingEvents);

            sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Dashboard data retrieved.", dashboardData));
        } catch (Exception e) {
            logger.error("Error fetching dashboard data for user {}", user.getUsername(), e);
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve dashboard data.");
        }
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