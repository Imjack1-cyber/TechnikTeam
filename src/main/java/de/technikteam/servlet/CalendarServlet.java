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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

		String view = request.getParameter("view");
		if (view == null || view.isEmpty()) {
			view = "month";
		}

		LocalDate today = LocalDate.now();
		String monthParam = request.getParameter("month");
		String yearParam = request.getParameter("year");

		YearMonth currentYearMonth = YearMonth.now();
		if (monthParam != null && yearParam != null) {
			try {
				currentYearMonth = YearMonth.of(Integer.parseInt(yearParam), Integer.parseInt(monthParam));
			} catch (NumberFormatException e) {
				// Ignore invalid parameters and use current month
			}
		}

		request.setAttribute("currentDate", today);
		request.setAttribute("currentYearMonth", currentYearMonth);
		request.setAttribute("monthName",
				currentYearMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.GERMAN));
		request.setAttribute("year", currentYearMonth.getYear());
		request.setAttribute("prevMonth", currentYearMonth.minusMonths(1));
		request.setAttribute("nextMonth", currentYearMonth.plusMonths(1));

		List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
		List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();

		// Create a unified list of map objects, which is safe for JSP EL
		List<Map<String, Object>> unifiedList = new ArrayList<>();
		events.forEach(item -> {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "Event");
			map.put("object", item);
			unifiedList.add(map);
		});
		meetings.forEach(item -> {
			Map<String, Object> map = new HashMap<>();
			map.put("type", "Meeting");
			map.put("object", item);
			unifiedList.add(map);
		});

		Map<LocalDate, List<Map<String, Object>>> eventsByDate = unifiedList.stream()
				.collect(Collectors.groupingBy(itemMap -> {
					Object item = itemMap.get("object");
					if (item instanceof Event) {
						return ((Event) item).getEventDateTime().toLocalDate();
					} else {
						return ((Meeting) item).getMeetingDateTime().toLocalDate();
					}
				}));
		request.setAttribute("eventsByDate", eventsByDate);

		// --- Data for Monthly View ---
		LocalDate firstOfMonth = currentYearMonth.atDay(1);
		int startDayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); // Monday=1, ..., Sunday=7
		int startDayOfWeekOffset = (startDayOfWeekValue == 7) ? 0 : startDayOfWeekValue; // Convert to Sunday=0,
																							// Monday=1, ...
		request.setAttribute("startDayOfWeekOffset", startDayOfWeekOffset);
		request.setAttribute("daysInMonth", currentYearMonth.lengthOfMonth());

		// --- Data for Weekly View ---
		WeekFields weekFields = WeekFields.of(Locale.GERMANY);
		LocalDate startOfWeek = today.with(weekFields.dayOfWeek(), 1);
		List<Map<String, Object>> weekData = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			LocalDate date = startOfWeek.plusDays(i);
			Map<String, Object> dayData = new HashMap<>();
			dayData.put("date", date);
			dayData.put("dayName", date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMAN));
			dayData.put("dayOfMonth", date.getDayOfMonth());
			weekData.add(dayData);
		}
		request.setAttribute("weekData", weekData);

		// --- Data for Mobile List View ---
		List<Map<String, Object>> mobileList = prepareMobileList(events, meetings, request.getContextPath());
		request.setAttribute("mobileList", mobileList);

		request.setAttribute("view", view);
		request.getRequestDispatcher("/views/public/calendar.jsp").forward(request, response);
	}

	private List<Map<String, Object>> prepareMobileList(List<Event> events, List<Meeting> meetings,
			String contextPath) {
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
			entry.put("url", contextPath + "/veranstaltungen/details?id=" + event.getId());
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
			entry.put("url", contextPath + "/meetingDetails?id=" + meeting.getId());
			combinedList.add(entry);
		}

		combinedList.sort(Comparator.comparing(m -> (LocalDateTime) m.get("start")));
		return combinedList;
	}
}