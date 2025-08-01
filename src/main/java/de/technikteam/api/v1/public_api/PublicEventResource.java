package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.*;
import de.technikteam.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

	@Autowired
	public PublicEventResource(EventDAO eventDAO, EventService eventService, EventCustomFieldDAO customFieldDAO) {
		this.eventDAO = eventDAO;
		this.eventService = eventService;
		this.customFieldDAO = customFieldDAO;
	}

	@GetMapping
	@Operation(summary = "Get upcoming events for user", description = "Retrieves a list of upcoming events, indicating the user's current attendance and qualification status for each.")
	public ResponseEntity<ApiResponse> getUpcomingEventsForUser(@AuthenticationPrincipal User user) {
		List<Event> events = eventDAO.getUpcomingEventsForUser(user);
		return ResponseEntity.ok(new ApiResponse(true, "Events retrieved successfully.", events));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get event details", description = "Retrieves detailed information for a single event.")
	public ResponseEntity<ApiResponse> getEventDetails(
			@Parameter(description = "ID of the event") @PathVariable int id) {
		Event event = eventDAO.getEventById(id);
		if (event == null) {
			return ResponseEntity.status(404).body(new ApiResponse(false, "Event not found", null));
		}
		return ResponseEntity.ok(new ApiResponse(true, "Event details retrieved.", event));
	}

	@PostMapping("/{id}/signup")
	@Operation(summary = "Sign up for an event", description = "Allows the current user to sign up for an event and submit custom field responses.")
	public ResponseEntity<ApiResponse> signUpForEvent(@Parameter(description = "ID of the event") @PathVariable int id,
			@AuthenticationPrincipal User user, @RequestBody Map<String, String> customFieldResponses) {
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
		return ResponseEntity.ok(new ApiResponse(true, "Successfully signed up for the event.", null));
	}

	@PostMapping("/{id}/signoff")
	@Operation(summary = "Sign off from an event", description = "Allows the current user to sign off from an event.")
	public ResponseEntity<ApiResponse> signOffFromEvent(
			@Parameter(description = "ID of the event") @PathVariable int id, @AuthenticationPrincipal User user,
			@RequestBody Map<String, String> payload) {
		String reason = payload.get("reason");
		Event event = eventDAO.getEventById(id);
		if ("LAUFEND".equals(event.getStatus())) {
			eventService.signOffUserFromRunningEvent(user.getId(), user.getUsername(), id, reason);
		} else {
			eventDAO.signOffFromEvent(user.getId(), id);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Successfully signed off from the event.", null));
	}

	@GetMapping("/{id}/custom-fields")
	@Operation(summary = "Get custom fields for an event", description = "Retrieves the list of custom fields required for signing up for a specific event.")
	public ResponseEntity<ApiResponse> getEventCustomFields(
			@Parameter(description = "ID of the event") @PathVariable int id) {
		List<EventCustomField> fields = customFieldDAO.getCustomFieldsForEvent(id);
		return ResponseEntity.ok(new ApiResponse(true, "Custom fields retrieved.", fields));
	}
}