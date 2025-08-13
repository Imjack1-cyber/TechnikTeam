package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.EventAssignmentDTO;
import de.technikteam.api.v1.dto.EventUpdateRequest;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import de.technikteam.security.SecurityUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Admin Events", description = "Endpoints for managing events.")
@SecurityRequirement(name = "bearerAuth")
public class AdminEventResource {

	private final EventDAO eventDAO;
	private final EventService eventService;

	@Autowired
	public AdminEventResource(EventDAO eventDAO, EventService eventService) {
		this.eventDAO = eventDAO;
		this.eventService = eventService;
	}

	@GetMapping
	@Operation(summary = "Get all events", description = "Retrieves a list of all events in the system, sorted by date.")
	public ResponseEntity<ApiResponse> getAllEvents() {
		List<Event> events = eventDAO.getAllEvents();
		return ResponseEntity.ok(new ApiResponse(true, "Veranstaltungen erfolgreich abgerufen.", events));
	}

	@PostMapping
	@Operation(summary = "Create a new event", description = "Creates a new event with attachments, skill requirements, and item reservations.")
	public ResponseEntity<ApiResponse> createEvent(@RequestPart("eventData") EventUpdateRequest eventData,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			Event event = new Event();
			mapDtoToEvent(eventData, event);

			int newEventId = eventService.createOrUpdateEvent(event, false, securityUser.getUser(),
					eventData.requiredCourseIds().toArray(new String[0]),
					eventData.requiredPersons().toArray(new String[0]), eventData.itemIds().toArray(new String[0]),
					eventData.quantities().toArray(new String[0]), null, file, eventData.requiredRole(),
					eventData.reminderMinutes());

			return new ResponseEntity<>(
					new ApiResponse(true, "Veranstaltung erfolgreich erstellt.", Map.of("id", newEventId)),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Fehler beim Erstellen der Veranstaltung: " + e.getMessage(), null));
		}
	}

	@PostMapping("/{id}")
	@Operation(summary = "Update an event", description = "Updates an existing event with attachments, skill requirements, and item reservations.")
	public ResponseEntity<ApiResponse> updateEvent(@PathVariable int id,
			@RequestPart("eventData") EventUpdateRequest eventData,
			@RequestPart(value = "file", required = false) MultipartFile file,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			Event event = eventDAO.getEventById(id);
			if (event == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Veranstaltung nicht gefunden.", null));
			}
			mapDtoToEvent(eventData, event);
			event.setId(id); 

			eventService.createOrUpdateEvent(event, true, securityUser.getUser(),
					eventData.requiredCourseIds().toArray(new String[0]),
					eventData.requiredPersons().toArray(new String[0]), eventData.itemIds().toArray(new String[0]),
					eventData.quantities().toArray(new String[0]), null, file, eventData.requiredRole(),
					eventData.reminderMinutes());

			return ResponseEntity.ok(new ApiResponse(true, "Veranstaltung erfolgreich aktualisiert.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(
					new ApiResponse(false, "Fehler beim Aktualisieren der Veranstaltung: " + e.getMessage(), null));
		}
	}

	@PostMapping("/{id}/clone")
	@Operation(summary = "Clone an event", description = "Creates a deep copy of an existing event, including its details, requirements, and tasks.")
	public ResponseEntity<ApiResponse> cloneEvent(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			Event clonedEvent = eventService.cloneEvent(id, securityUser.getUser());
			return new ResponseEntity<>(new ApiResponse(true, "Event erfolgreich geklont.", clonedEvent),
					HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Klonen des Events fehlgeschlagen: " + e.getMessage(), null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an event", description = "Permanently deletes an event and all associated data.")
	public ResponseEntity<ApiResponse> deleteEvent(@PathVariable int id) {
		if (eventDAO.deleteEvent(id)) {
			return ResponseEntity.ok(new ApiResponse(true, "Veranstaltung erfolgreich gelöscht.", null));
		} else {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Fehler beim Löschen der Veranstaltung.", null));
		}
	}

	@PostMapping("/{eventId}/assignments")
	@Operation(summary = "Update team assignments for an event", description = "Sets the entire team for an event, including their roles.")
	public ResponseEntity<ApiResponse> updateAssignments(@PathVariable int eventId,
			@RequestBody List<EventAssignmentDTO> assignments, @AuthenticationPrincipal SecurityUser securityUser) {
		try {
			eventService.updateTeamAssignmentsAndNotify(eventId, assignments, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Team-Zuweisungen erfolgreich aktualisiert.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Fehler beim Aktualisieren der Zuweisungen: " + e.getMessage(), null));
		}
	}

	private void mapDtoToEvent(EventUpdateRequest dto, Event event) {
		event.setName(dto.name());
		event.setEventDateTime(dto.eventDateTime());
		event.setEndDateTime(dto.endDateTime());
		event.setDescription(dto.description());
		event.setLocation(dto.location());
		event.setStatus(dto.status());
		event.setLeaderUserId(dto.leaderUserId() != null && dto.leaderUserId() != 0 ? dto.leaderUserId() : 0);
	}
}