package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.model.EventCustomField;
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
 * A public API endpoint that securely provides the list of custom sign-up
 * fields for a specific event. This is called by the JavaScript on the main
 * events page to populate the sign-up modal window.
 */
@WebServlet("/api/public/event-custom-fields")
public class EventCustomFieldsApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(EventCustomFieldsApiServlet.class);
    private EventCustomFieldDAO customFieldDAO;
    private Gson gson;

    @Override
    public void init() {
        customFieldDAO = new EventCustomFieldDAO();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventIdParam = request.getParameter("eventId");
        if (eventIdParam == null || eventIdParam.trim().isEmpty()) {
            logger.warn("API call rejected: eventId parameter is missing.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required 'eventId' parameter.");
            return;
        }

        try {
            int eventId = Integer.parseInt(eventIdParam);
            logger.debug("API request for custom fields for event ID: {}", eventId);

            List<EventCustomField> fields = customFieldDAO.getCustomFieldsForEvent(eventId);
            String jsonResponse = gson.toJson(fields);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            logger.warn("API call rejected: Invalid eventId format '{}'.", eventIdParam);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid 'eventId' format. It must be a number.");
        }
    }
}