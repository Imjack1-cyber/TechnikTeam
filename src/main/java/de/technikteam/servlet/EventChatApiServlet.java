package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.model.EventChatMessage;
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
 * Provides a JSON API to fetch the historical messages for a given event chat.
 * New messages are handled by the EventChatSocket WebSocket.
 */
@WebServlet("/api/event-chat")
public class EventChatApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventChatApiServlet.class);
	private EventChatDAO chatDAO;
	private Gson gson;

	@Override
	public void init() {
		chatDAO = new EventChatDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	/**
	 * Handles GET requests to fetch the message history for an event.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int eventId = Integer.parseInt(request.getParameter("eventId"));
			logger.trace("GET request for chat history for event ID: {}", eventId);
			List<EventChatMessage> messages = chatDAO.getMessagesForEvent(eventId);
			String jsonResponse = gson.toJson(messages);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonResponse);
		} catch (NumberFormatException e) {
			logger.warn("Bad request to event chat history API: Invalid or missing eventId.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing eventId.");
		}
	}
}