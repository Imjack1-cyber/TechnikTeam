package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/calendar")
@Tag(name = "Public Calendar", description = "Endpoints for calendar data.")
@SecurityRequirement(name = "bearerAuth")
public class PublicCalendarEntriesResource { // Renamed from PublicCalendarEntriesResource to match Path

	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;

	@Autowired
	public PublicCalendarEntriesResource(EventDAO eventDAO, MeetingDAO meetingDAO) {
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
	}

	@GetMapping("/entries")
	@Operation(summary = "Get calendar entries", description = "Retrieves a combined list of upcoming events and meetings for display in a calendar.")
	public ResponseEntity<ApiResponse> getCalendarEntries() {
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

		return ResponseEntity.ok(new ApiResponse(true, "Calendar entries retrieved.", entries));
	}
}