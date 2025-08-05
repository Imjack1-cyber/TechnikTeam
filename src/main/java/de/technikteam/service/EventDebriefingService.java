package de.technikteam.service;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.EventDebriefingDTO;
import de.technikteam.config.Permissions;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventDebriefingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventDebriefing;
import de.technikteam.model.User;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventDebriefingService {

	private final EventDebriefingDAO debriefingDAO;
	private final EventDAO eventDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final PolicyFactory richTextPolicy;
	private final Gson gson = new Gson();

	@Autowired
	public EventDebriefingService(EventDebriefingDAO debriefingDAO, EventDAO eventDAO, UserDAO userDAO,
			AdminLogService adminLogService, NotificationService notificationService,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.debriefingDAO = debriefingDAO;
		this.eventDAO = eventDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.richTextPolicy = richTextPolicy;
	}

	@Transactional
	public EventDebriefing saveDebriefing(int eventId, EventDebriefingDTO dto, User author) {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			throw new IllegalArgumentException("Event not found.");
		}
		if (!"ABGESCHLOSSEN".equals(event.getStatus())) {
			throw new IllegalStateException("Debriefings can only be submitted for completed events.");
		}

		EventDebriefing debriefing = debriefingDAO.findByEventId(eventId).orElse(new EventDebriefing());
		debriefing.setEventId(eventId);
		debriefing.setAuthorUserId(author.getId());
		debriefing.setWhatWentWell(richTextPolicy.sanitize(dto.whatWentWell()));
		debriefing.setWhatToImprove(richTextPolicy.sanitize(dto.whatToImprove()));
		debriefing.setEquipmentNotes(richTextPolicy.sanitize(dto.equipmentNotes()));
		debriefing.setStandoutCrewMembers(gson.toJson(dto.standoutCrewMemberIds()));

		EventDebriefing savedDebriefing = debriefingDAO.save(debriefing);
		adminLogService.log(author.getUsername(), "SUBMIT_DEBRIEFING",
				"Debriefing for event '" + event.getName() + "' submitted/updated.");

		// Placeholder for achievement logic based on being a "standout crew member"
		// for (Integer userId : dto.standoutCrewMemberIds()) { ... }

		// Notify admins with permission that a new debriefing is available
		List<Integer> adminIds = userDAO.findUserIdsByPermission(Permissions.EVENT_DEBRIEFING_VIEW);
		String title = "Neues Event-Debriefing";
		String description = String.format("Ein Debriefing für das Event '%s' wurde von %s eingereicht.",
				event.getName(), author.getUsername());
		for (Integer adminId : adminIds) {
			if (adminId != author.getId()) { // Don't notify the author
				Map<String, Object> payload = Map.of("title", title, "description", description, "level",
						"Informational", "url", "/admin/debriefings");
				notificationService.sendNotificationToUser(adminId, payload);
			}
		}
		return enrichDebriefing(savedDebriefing);
	}

	public EventDebriefing enrichDebriefing(EventDebriefing debriefing) {
		if (debriefing == null)
			return null;
		if (debriefing.getStandoutCrewMembers() != null && !debriefing.getStandoutCrewMembers().isBlank()) {
			List<Integer> userIds = gson.fromJson(debriefing.getStandoutCrewMembers(), List.class);
			List<User> userDetails = userIds.stream().map(id -> userDAO.getUserById(id.intValue()))
					.filter(java.util.Objects::nonNull).collect(Collectors.toList());
			debriefing.setStandoutCrewDetails(userDetails);
		}
		return debriefing;
	}
}