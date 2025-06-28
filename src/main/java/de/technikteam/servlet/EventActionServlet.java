package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.EventCustomField;
import de.technikteam.model.EventCustomFieldResponse;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/event-action")
public class EventActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventActionServlet.class);
	private EventDAO eventDAO;
	private EventCustomFieldDAO customFieldDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		customFieldDAO = new EventCustomFieldDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		String eventIdParam = request.getParameter("eventId");

		if (user == null || action == null || eventIdParam == null) {
			logger.warn("Invalid request to EventActionServlet. Missing user, action, or eventId parameter.");
			response.sendRedirect(request.getContextPath() + "/events");
			return;
		}

		try {
			int eventId = Integer.parseInt(eventIdParam);
			logger.info("User '{}' (ID: {}) is performing action '{}' on event ID {}", user.getUsername(), user.getId(),
					action, eventId);

			if ("signup".equals(action)) {
				eventDAO.signUpForEvent(user.getId(), eventId);

				// Handle custom field responses
				List<EventCustomField> fields = customFieldDAO.getCustomFieldsForEvent(eventId);
				for (EventCustomField field : fields) {
					String paramName = "customfield_" + field.getId();
					String paramValue = request.getParameter(paramName);

					if (paramValue != null) {
						EventCustomFieldResponse customResponse = new EventCustomFieldResponse();
						customResponse.setFieldId(field.getId());
						customResponse.setUserId(user.getId());
						customResponse.setResponseValue(paramValue);
						customFieldDAO.saveResponse(customResponse);
						logger.debug("Saved custom response for field ID {} and user ID {}.", field.getId(),
								user.getId());
					}
				}

				request.getSession().setAttribute("successMessage", "Erfolgreich zum Event angemeldet.");
			} else if ("signoff".equals(action)) {
				eventDAO.signOffFromEvent(user.getId(), eventId);
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Event abgemeldet.");
			} else {
				logger.warn("Unknown action received in EventActionServlet: {}", action);
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format in EventActionServlet.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}

		response.sendRedirect(request.getContextPath() + "/events");
	}
}