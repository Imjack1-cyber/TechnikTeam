// src/main/java/de/technikteam/api/v1/public_api/PublicCalendarResource.java
package de.technikteam.api.v1.public_api;

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
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Singleton
public class PublicCalendarResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;

	@Inject
	public PublicCalendarResource(EventDAO eventDAO, MeetingDAO meetingDAO) {
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			Calendar calendar = new Calendar();
			calendar.getProperties().add(new ProdId("-//TechnikTeam Calendar//iCal4j 3.2.4//DE"));
			calendar.getProperties().add(Version.VERSION_2_0);

			RandomUidGenerator uidGenerator = new RandomUidGenerator();
			String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			ZoneId systemZone = ZoneId.systemDefault();

			List<Event> events = eventDAO.getAllActiveAndUpcomingEvents();
			for (Event event : events) {
				VEvent vEvent = new VEvent();
				vEvent.getProperties().add(uidGenerator.generateUid());

				if (event.getEventDateTime() != null) {
					ZonedDateTime zdtStart = event.getEventDateTime().atZone(systemZone);
					Date utilDateStart = Date.from(zdtStart.toInstant());
					vEvent.getProperties().add(new DtStart(new DateTime(utilDateStart)));
				}
				if (event.getEndDateTime() != null) {
					ZonedDateTime zdtEnd = event.getEndDateTime().atZone(systemZone);
					Date utilDateEnd = Date.from(zdtEnd.toInstant());
					vEvent.getProperties().add(new DtEnd(new DateTime(utilDateEnd)));
				}

				vEvent.getProperties().add(new Summary(event.getName()));
				if (event.getDescription() != null)
					vEvent.getProperties().add(new Description(event.getDescription()));
				if (event.getLocation() != null)
					vEvent.getProperties().add(new Location(event.getLocation()));
				try {
					vEvent.getProperties()
							.add(new Url(new URI(baseUrl + "/veranstaltungen/details?id=" + event.getId())));
				} catch (URISyntaxException ignored) {
				}
				calendar.getComponents().add(vEvent);
			}

			List<Meeting> meetings = meetingDAO.getAllUpcomingMeetings();
			for (Meeting meeting : meetings) {
				String title = meeting.getParentCourseName() + ": " + meeting.getName();
				VEvent vMeeting = new VEvent();
				vMeeting.getProperties().add(uidGenerator.generateUid());

				if (meeting.getMeetingDateTime() != null) {
					ZonedDateTime zdtStart = meeting.getMeetingDateTime().atZone(systemZone);
					Date utilDateStart = Date.from(zdtStart.toInstant());
					vMeeting.getProperties().add(new DtStart(new DateTime(utilDateStart)));
				}
				if (meeting.getEndDateTime() != null) {
					ZonedDateTime zdtEnd = meeting.getEndDateTime().atZone(systemZone);
					Date utilDateEnd = Date.from(zdtEnd.toInstant());
					vMeeting.getProperties().add(new DtEnd(new DateTime(utilDateEnd)));
				}

				vMeeting.getProperties().add(new Summary(title));
				if (meeting.getDescription() != null)
					vMeeting.getProperties().add(new Description(meeting.getDescription()));
				if (meeting.getLocation() != null)
					vMeeting.getProperties().add(new Location(meeting.getLocation()));
				try {
					vMeeting.getProperties().add(new Url(new URI(baseUrl + "/meetingDetails?id=" + meeting.getId())));
				} catch (URISyntaxException ignored) {
				}
				calendar.getComponents().add(vMeeting);
			}

			response.setContentType("text/calendar; charset=utf-8");
			response.setHeader("Content-Disposition", "inline; filename=\"technikteam-calendar.ics\"");
			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(calendar, response.getOutputStream());

		} catch (Exception e) {
			throw new ServletException("Error generating iCal feed", e);
		}
	}
}