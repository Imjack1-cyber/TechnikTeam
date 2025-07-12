package de.technikteam.servlet;

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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This servlet handles the request for the main calendar page. It fetches all
 * upcoming events and meetings, combines and sorts them, groups them by month,
 * and then forwards the data to the calendar.jsp page for rendering in a custom
 * list view.
 */
@WebServlet("/kalender")
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO();
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