package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.ChatDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.AnnouncementDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.ChatConversation;
import de.technikteam.model.Meeting;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.model.Announcement;
import de.technikteam.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/dashboard")
@Tag(name = "Public Dashboard", description = "Endpoints for the user-facing dashboard.")
@SecurityRequirement(name = "bearerAuth")
public class PublicDashboardResource {

	private final EventDAO eventDAO;
	private final EventTaskDAO eventTaskDAO;
	private final ChatDAO chatDAO;
	private final MeetingDAO meetingDAO;
	private final StorageDAO storageDAO;
	private final AnnouncementDAO announcementDAO;

	@Autowired
	public PublicDashboardResource(EventDAO eventDAO, EventTaskDAO eventTaskDAO, ChatDAO chatDAO, MeetingDAO meetingDAO,
			StorageDAO storageDAO, AnnouncementDAO announcementDAO) {
		this.eventDAO = eventDAO;
		this.eventTaskDAO = eventTaskDAO;
		this.chatDAO = chatDAO;
		this.meetingDAO = meetingDAO;
		this.storageDAO = storageDAO;
		this.announcementDAO = announcementDAO;
	}

	@GetMapping
	@Operation(summary = "Get dashboard data", description = "Retrieves all necessary data for the user's main dashboard view.")
	public ResponseEntity<ApiResponse> getDashboardData(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();

		List<Event> assignedEvents = eventDAO.getAssignedEventsForUser(user.getId(), 5);
		List<EventTask> openTasks = eventTaskDAO.getOpenTasksForUser(user.getId());
		List<Event> upcomingEvents = eventDAO.getAllActiveAndUpcomingEvents(); 
		List<Event> recommendedEvents = eventDAO.getPersonalizedEventFeed(user.getId(), 3);
		List<ChatConversation> recentConversations = chatDAO.getConversationsForUser(user.getId());
		List<Meeting> signedUpMeetings = meetingDAO.getUpcomingMeetingsForUser(user);
		List<StorageItem> lowStockItems = storageDAO.getLowStockItems(5);

		Map<String, Object> dashboardData = new HashMap<>();
		dashboardData.put("assignedEvents", assignedEvents);
		dashboardData.put("openTasks", openTasks);
		dashboardData.put("upcomingEvents", upcomingEvents);
		dashboardData.put("recommendedEvents", recommendedEvents);
		dashboardData.put("recentConversations", recentConversations.stream().limit(5).toList());
		dashboardData.put("upcomingMeetings", signedUpMeetings.stream()
				.filter(m -> "ANGEMELDET".equals(m.getUserAttendanceStatus())).limit(5).toList());
		dashboardData.put("lowStockItems", lowStockItems);

		return ResponseEntity.ok(new ApiResponse(true, "Dashboard-Daten erfolgreich abgerufen.", dashboardData));
	}

	@GetMapping("/widget-data")
	@Operation(summary = "Get data for widgets", description = "Retrieves lightweight data specifically for home screen widgets.")
	public ResponseEntity<ApiResponse> getWidgetData(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();

		List<Event> nextAssignedEvent = eventDAO.getAssignedEventsForUser(user.getId(), 1);
		List<EventTask> openTasks = eventTaskDAO.getOpenTasksForUser(user.getId());
		List<Announcement> announcements = announcementDAO.findAll();

		Map<String, Object> widgetData = new HashMap<>();
		widgetData.put("nextEvent", nextAssignedEvent.isEmpty() ? null : nextAssignedEvent.get(0));
		widgetData.put("openTasks", openTasks);
		widgetData.put("latestAnnouncement", announcements.isEmpty() ? null : announcements.get(0));

		return ResponseEntity.ok(new ApiResponse(true, "Widget data retrieved successfully.", widgetData));
	}
}