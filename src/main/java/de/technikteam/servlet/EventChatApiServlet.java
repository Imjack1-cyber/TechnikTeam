package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.model.EventChatMessage;
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
public class EventChatApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventChatApiServlet.class);
	private final EventChatDAO chatDAO;
	private final Gson gson;

	@Inject
	public EventChatApiServlet(EventChatDAO chatDAO) {
		this.chatDAO = chatDAO;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

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