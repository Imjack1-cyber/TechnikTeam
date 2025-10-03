package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.*;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.EventService;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/events")
@Tag(name = "Public Events", description = "Endpoints for user interactions with events.")
@SecurityRequirement(name = "bearerAuth")
public class PublicEventResource {

	private final EventDAO eventDAO;
	private final EventService eventService;
	private final EventCustomFieldDAO customFieldDAO;
	private final FileService fileService;
	private final EventChatDAO eventChatDAO;

	@Autowired
	public PublicEventResource(EventDAO eventDAO, EventService eventService, EventCustomFieldDAO customFieldDAO,
			FileService fileService, EventChatDAO eventChatDAO) {
		this.eventDAO = eventDAO;
		this.eventService = eventService;
		this.customFieldDAO = customFieldDAO;
		this.fileService = fileService;
		this.eventChatDAO = eventChatDAO;
	}

	@GetMapping
	@Operation(summary = "Get upcoming events for user", description = "Retrieves a list of upcoming events, indicating the user's current attendance and qualification status for each.")
	public ResponseEntity<ApiResponse> getUpcomingEventsForUser(@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		List<Event> events = eventDAO.getUpcomingEventsForUser(user);
		return ResponseEntity.ok(new ApiResponse(true, "Veranstaltungen erfolgreich abgerufen.", events));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get event details", description = "Retrieves detailed information for a single event.")
	public ResponseEntity<ApiResponse> getEventDetails(@Parameter(description = "ID of the event") @PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Event event = eventDAO.getEventById(id);
		if (event == null) {
			return ResponseEntity.status(404).body(new ApiResponse(false, "Veranstaltung nicht gefunden.", null));
		}
		if (securityUser != null) {
			String status = eventDAO.getUserAttendanceStatus(id, securityUser.getUser().getId());
			event.setUserAttendanceStatus(status);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Veranstaltungsdetails erfolgreich abgerufen.", event));
	}

	@PostMapping("/{id}/signup")
	@Operation(summary = "Sign up for an event", description = "Allows the current user to sign up for an event and submit custom field responses.")
	public ResponseEntity<ApiResponse> signUpForEvent(@Parameter(description = "ID of the event") @PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser, @RequestBody Map<String, String> customFieldResponses) {
		Event event = eventDAO.getEventById(id);
		if (event == null) {
			return new ResponseEntity<>(new ApiResponse(false, "Veranstaltung nicht gefunden.", null), HttpStatus.NOT_FOUND);
		}
		if (!"GEPLANT".equals(event.getStatus()) && !"LAUFEND".equals(event.getStatus())) {
			return new ResponseEntity<>(new ApiResponse(false, "Anmeldungen sind nur für geplante oder laufende Veranstaltungen möglich.", null), HttpStatus.BAD_REQUEST);
		}

		User user = securityUser.getUser();
		eventDAO.signUpForEvent(user.getId(), id);
		if (customFieldResponses != null) {
			customFieldResponses.forEach((key, value) -> {
				if (key.startsWith("customfield_")) {
					int fieldId = Integer.parseInt(key.substring("customfield_".length()));
					EventCustomFieldResponse response = new EventCustomFieldResponse();
					response.setFieldId(fieldId);
					response.setUserId(user.getId());
					response.setResponseValue(value);
					customFieldDAO.saveResponse(response);
				}
			});
		}
		return ResponseEntity.ok(new ApiResponse(true, "Erfolgreich für die Veranstaltung angemeldet.", null));
	}

	@PostMapping("/{id}/signoff")
	@Operation(summary = "Sign off from an event", description = "Allows the current user to sign off from an event.")
	public ResponseEntity<ApiResponse> signOffFromEvent(
			@Parameter(description = "ID of the event") @PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser, @RequestBody Map<String, String> payload) {
		User user = securityUser.getUser();
		String reason = payload.get("reason");
		Event event = eventDAO.getEventById(id);
		if ("LAUFEND".equals(event.getStatus())) {
			eventService.signOffUserFromRunningEvent(user.getId(), user.getUsername(), id, reason);
		} else {
			eventDAO.signOffFromEvent(user.getId(), id);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Erfolgreich von der Veranstaltung abgemeldet.", null));
	}

	@GetMapping("/{id}/custom-fields")
	@Operation(summary = "Get custom fields for an event", description = "Retrieves the list of custom fields required for signing up for a specific event.")
	public ResponseEntity<ApiResponse> getEventCustomFields(
			@Parameter(description = "ID of the event") @PathVariable int id) {
		List<EventCustomField> fields = customFieldDAO.getCustomFieldsForEvent(id);
		return ResponseEntity.ok(new ApiResponse(true, "Zusatzfelder erfolgreich abgerufen.", fields));
	}

	@PostMapping("/{eventId}/chat/upload")
	@Operation(summary = "Upload a file to an event chat")
	public ResponseEntity<ApiResponse> uploadEventChatFile(@PathVariable int eventId,
			@RequestParam("file") MultipartFile file, @AuthenticationPrincipal SecurityUser securityUser) {
		if (!eventDAO.isUserAssociatedWithEvent(eventId, securityUser.getUser().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ApiResponse(false, "Nicht berechtigt, Dateien in diesem Chat hochzuladen.", null));
		}
		try {
			de.technikteam.model.File savedFile = fileService.storeFile(file, null, "NUTZER", securityUser.getUser(),
					"eventchat/" + eventId);
			return ResponseEntity.ok(new ApiResponse(true, "Datei hochgeladen.", savedFile));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Datei-Upload fehlgeschlagen: " + e.getMessage(), null));
		}
	}

	@GetMapping("/{eventId}/chat/messages")
	@Operation(summary = "Get chat messages for an event")
	public ResponseEntity<ApiResponse> getEventChatMessages(@PathVariable int eventId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (!eventDAO.isUserAssociatedWithEvent(eventId, securityUser.getUser().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ApiResponse(false, "You are not a participant of this event's chat.", null));
		}
		List<EventChatMessage> messages = eventChatDAO.getMessagesForEvent(eventId);
		return ResponseEntity.ok(new ApiResponse(true, "Chat messages retrieved successfully.", messages));
	}
}