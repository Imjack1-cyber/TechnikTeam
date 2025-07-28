package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class PublicCalendarEntriesResource extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final Gson gson;

	@Inject
	public PublicCalendarEntriesResource(EventDAO eventDAO, MeetingDAO meetingDAO, Gson gson) {
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		List<Map<String, Object>> entries = new ArrayList<>();

		List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
		for (Event event : events) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("id", event.getId());
			entry.put("title", event.getName());
			entry.put("start", event.getEventDateTime());
			entry.put("end", event.getEndDateTime());
			entry.put("type", "Event");
			entry.put("url", "/veranstaltungen/details/" + event.getId());
			entries.add(entry);
		}

		List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();
		for (Meeting meeting : meetings) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("id", meeting.getId());
			entry.put("title", meeting.getParentCourseName() + ": " + meeting.getName());
			entry.put("start", meeting.getMeetingDateTime());
			entry.put("end", meeting.getEndDateTime());
			entry.put("type", "Lehrgang");
			entry.put("url", "/lehrgaenge/details/" + meeting.getId());
			entries.add(entry);
		}

		sendJsonResponse(resp, HttpServletResponse.SC_OK,
				new ApiResponse(true, "Calendar entries retrieved.", entries));
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}
}