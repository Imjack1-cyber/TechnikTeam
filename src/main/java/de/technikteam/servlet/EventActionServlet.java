package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.EventCustomField;
import de.technikteam.model.EventCustomFieldResponse;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class EventActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventActionServlet.class);
	private final EventDAO eventDAO;
	private final EventCustomFieldDAO customFieldDAO;

	@Inject
	public EventActionServlet(EventDAO eventDAO, EventCustomFieldDAO customFieldDAO) {
		this.eventDAO = eventDAO;
		this.customFieldDAO = customFieldDAO;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		String eventIdParam = request.getParameter("eventId");

		if (user == null || action == null || eventIdParam == null) {
			response.sendRedirect(request.getContextPath() + "/veranstaltungen");
			return;
		}

		try {
			int eventId = Integer.parseInt(eventIdParam);
			logger.info("User '{}' (ID: {}) is performing action '{}' on event ID {}", user.getUsername(), user.getId(),
					action, eventId);

			if ("signup".equals(action)) {
				eventDAO.signUpForEvent(user.getId(), eventId);
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
					}
				}
				request.getSession().setAttribute("successMessage", "Erfolgreich zum Event angemeldet.");
			} else if ("signoff".equals(action)) {
				eventDAO.signOffFromEvent(user.getId(), eventId);
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Event abgemeldet.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format in EventActionServlet.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/veranstaltungen");
	}
}