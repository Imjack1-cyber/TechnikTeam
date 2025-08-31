package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
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

		if (!author.hasAdminAccess() && author.getId() != event.getLeaderUserId()
				&& !author.getPermissions().contains(Permissions.EVENT_DEBRIEFING_MANAGE)) {
			throw new AccessDeniedException("Sie haben keine Berechtigung, dieses Debriefing zu bearbeiten.");
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

		List<Integer> adminIds = userDAO.findUserIdsByPermission(Permissions.EVENT_DEBRIEFING_VIEW);
		String title = "Neues Event-Debriefing";
		String description = String.format("Ein Debriefing für das Event '%s' wurde von %s eingereicht.",
				event.getName(), author.getUsername());
		for (Integer adminId : adminIds) {
			if (adminId != author.getId()) {
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
            Type listType = new TypeToken<List<Double>>() {}.getType();
			List<Double> userIdsDouble = gson.fromJson(debriefing.getStandoutCrewMembers(), listType);
            List<Integer> userIds = userIdsDouble.stream().map(Double::intValue).collect(Collectors.toList());

			List<User> userDetails = userIds.stream().map(userDAO::getUserById)
					.filter(java.util.Objects::nonNull).collect(Collectors.toList());
			debriefing.setStandoutCrewDetails(userDetails);
		}
		return debriefing;
	}
}