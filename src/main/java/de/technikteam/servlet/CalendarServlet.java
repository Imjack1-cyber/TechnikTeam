package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Singleton
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;

	@Inject
	public CalendarServlet(EventDAO eventDAO, MeetingDAO meetingDAO) {
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
		List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();

		List<Map<String, Object>> combinedList = new ArrayList<>();
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
		DateTimeFormatter monthAbbrFormatter = DateTimeFormatter.ofPattern("MMM", Locale.GERMANY);

		for (Event event : events) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("title", event.getName());
			entry.put("start", event.getEventDateTime());
			entry.put("day", event.getEventDateTime().format(dayFormatter));
			entry.put("monthAbbr", event.getEventDateTime().format(monthAbbrFormatter));
			entry.put("type", "Event");
			entry.put("typeClass", "termin-type-event");
			entry.put("url", request.getContextPath() + "/veranstaltungen/details?id=" + event.getId());
			combinedList.add(entry);
		}

		for (Meeting meeting : meetings) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("title", meeting.getParentCourseName() + ": " + meeting.getName());
			entry.put("start", meeting.getMeetingDateTime());
			entry.put("day", meeting.getMeetingDateTime().format(dayFormatter));
			entry.put("monthAbbr", meeting.getMeetingDateTime().format(monthAbbrFormatter));
			entry.put("type", "Lehrgang");
			entry.put("typeClass", "termin-type-lehrgang");
			entry.put("url", request.getContextPath() + "/meetingDetails?id=" + meeting.getId());
			combinedList.add(entry);
		}

		combinedList.sort(Comparator.comparing(m -> (LocalDateTime) m.get("start")));

		DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.GERMANY);
		Map<String, List<Map<String, Object>>> groupedByMonth = new LinkedHashMap<>();
		for (Map<String, Object> entry : combinedList) {
			LocalDateTime start = (LocalDateTime) entry.get("start");
			String monthKey = start.format(monthYearFormatter);
			groupedByMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(entry);
		}

		request.setAttribute("groupedEntries", groupedByMonth);
		request.getRequestDispatcher("/views/public/calendar.jsp").forward(request, response);
	}
}