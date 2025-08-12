package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Event;
import de.technikteam.model.Meeting;
import de.technikteam.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public Calendar", description = "Endpoints for calendar data.")
@SecurityRequirement(name = "bearerAuth")
public class PublicCalendarResource {

	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final ConfigurationService configService;

	@Autowired
	public PublicCalendarResource(EventDAO eventDAO, MeetingDAO meetingDAO, ConfigurationService configService) {
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.configService = configService;
	}

	@GetMapping("/calendar.ics")
	@Operation(summary = "Get iCalendar Feed", description = "Provides an iCalendar (.ics) feed of all upcoming events and meetings.", responses = {
			@ApiResponse(responseCode = "200", description = "iCalendar feed generated successfully", content = @Content(mediaType = "text/calendar")),
			@ApiResponse(responseCode = "500", description = "Internal server error while generating the feed") })
	public ResponseEntity<byte[]> getICalendarFeed() {
		try {
			Calendar calendar = new Calendar();
			calendar.getProperties().add(new ProdId("-/ Calendar//iCal4j 3.2.4//DE"));
			calendar.getProperties().add(Version.VERSION_2_0);

			RandomUidGenerator uidGenerator = new RandomUidGenerator();
			String baseUrl = configService.getProperty("app.base-url");
			ZoneId systemZone = ZoneId.systemDefault();

			List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
			for (Event event : events) {
				VEvent vEvent = new VEvent();
				vEvent.getProperties().add(uidGenerator.generateUid());
				if (event.getEventDateTime() != null) {
					vEvent.getProperties().add(new DtStart(
							new DateTime(Date.from(event.getEventDateTime().atZone(systemZone).toInstant()))));
				}
				if (event.getEndDateTime() != null) {
					vEvent.getProperties().add(
							new DtEnd(new DateTime(Date.from(event.getEndDateTime().atZone(systemZone).toInstant()))));
				}
				vEvent.getProperties().add(new Summary(event.getName()));
				if (event.getDescription() != null)
					vEvent.getProperties().add(new Description(event.getDescription()));
				if (event.getLocation() != null)
					vEvent.getProperties().add(new Location(event.getLocation()));
				vEvent.getProperties().add(new Url(new URI(baseUrl + "/veranstaltungen/details/" + event.getId())));
				calendar.getComponents().add(vEvent);
			}

			List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();
			for (Meeting meeting : meetings) {
				String title = meeting.getParentCourseName() + ": " + meeting.getName();
				VEvent vMeeting = new VEvent();
				vMeeting.getProperties().add(uidGenerator.generateUid());
				if (meeting.getMeetingDateTime() != null) {
					vMeeting.getProperties().add(new DtStart(
							new DateTime(Date.from(meeting.getMeetingDateTime().atZone(systemZone).toInstant()))));
				}
				if (meeting.getEndDateTime() != null) {
					vMeeting.getProperties().add(new DtEnd(
							new DateTime(Date.from(meeting.getEndDateTime().atZone(systemZone).toInstant()))));
				}
				vMeeting.getProperties().add(new Summary(title));
				if (meeting.getDescription() != null)
					vMeeting.getProperties().add(new Description(meeting.getDescription()));
				if (meeting.getLocation() != null)
					vMeeting.getProperties().add(new Location(meeting.getLocation()));
				vMeeting.getProperties().add(new Url(new URI(baseUrl + "/lehrgaenge/details/" + meeting.getId())));
				calendar.getComponents().add(vMeeting);
			}

			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(calendar, boas);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType("text/calendar"));
			headers.setContentDispositionFormData("attachment", "technikteam-calendar.ics");

			return new ResponseEntity<>(boas.toByteArray(), headers, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}