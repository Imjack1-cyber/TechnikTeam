package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/calendar/entries")
public class CalendarApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private MeetingDAO meetingDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Map<String, String>> calendarEntries = new ArrayList<>();

		// Fetch events
		List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
		for (Event event : events) {
			Map<String, String> entry = new HashMap<>();
			entry.put("title", event.getName());
			entry.put("start", event.getEventDateTime().toString());
			if (event.getEndDateTime() != null) {
				entry.put("end", event.getEndDateTime().toString());
			}
			entry.put("url", request.getContextPath() + "/veranstaltungen/details?id=" + event.getId());
			entry.put("backgroundColor", "#dc3545"); // Danger color for events
			entry.put("borderColor", "#c82333");
			calendarEntries.add(entry);
		}

		// Fetch meetings
		List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();
		for (Meeting meeting : meetings) {
			Map<String, String> entry = new HashMap<>();
			entry.put("title", meeting.getParentCourseName() + ": " + meeting.getName());
			entry.put("start", meeting.getMeetingDateTime().toString());
			if (meeting.getEndDateTime() != null) {
				entry.put("end", meeting.getEndDateTime().toString());
			}
			entry.put("url", request.getContextPath() + "/meetingDetails?id=" + meeting.getId());
			entry.put("backgroundColor", "#007bff"); // Primary color for meetings
			entry.put("borderColor", "#0056b3");
			calendarEntries.add(entry);
		}

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(gson.toJson(calendarEntries));
	}
}