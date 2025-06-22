package de.technikteam.servlet;

import com.google.gson.Gson;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.model.EventChatMessage;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Mapped to `/api/event-chat`, this servlet provides a JSON API for the
 * real-time event chat feature. A GET request fetches all messages for a given
 * event ID. A POST request allows a logged-in user to submit a new message for
 * an event. It uses the Gson library for JSON serialization.
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
		gson = new Gson();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int eventId = Integer.parseInt(request.getParameter("eventId"));
			logger.trace("GET request for chat messages for event ID: {}", eventId);
			List<EventChatMessage> messages = chatDAO.getMessagesForEvent(eventId);
			String jsonResponse = gson.toJson(messages);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jsonResponse);
		} catch (NumberFormatException e) {
			logger.warn("Bad request to event chat API: Invalid or missing eventId.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing eventId.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		try {
			int eventId = Integer.parseInt(request.getParameter("eventId"));
			String messageText = request.getParameter("messageText");
			User user = (User) request.getSession().getAttribute("user");

			if (user == null || messageText == null || messageText.trim().isEmpty()) {
				logger.warn("Bad POST request to event chat API: Missing user session or message text for event ID {}",
						eventId);
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing user or message text.");
				return;
			}

			logger.info("User '{}' posting message to event chat for event ID {}: '{}'", user.getUsername(), eventId,
					messageText);

			EventChatMessage newMessage = new EventChatMessage();
			newMessage.setEventId(eventId);
			newMessage.setUserId(user.getId());
			newMessage.setUsername(user.getUsername());
			newMessage.setMessageText(messageText);

			if (chatDAO.postMessage(newMessage)) {
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				logger.error("Failed to post chat message for user '{}' to event ID {}", user.getUsername(), eventId);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (NumberFormatException e) {
			logger.warn("Bad request to event chat API: Invalid eventId in POST.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid event ID.");
		}
	}
}