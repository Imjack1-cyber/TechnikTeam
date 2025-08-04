package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.EventUpdateRequest;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
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
			User adminUser = securityUser.getUser();
			Event event = new Event();
			mapDtoToEvent(eventData, event);

			int newEventId = eventService.createOrUpdateEvent(event, false, adminUser,
					eventData.requiredCourseIds().toArray(new String[0]),
					eventData.requiredPersons().toArray(new String[0]), eventData.itemIds().toArray(new String[0]),
					eventData.quantities().toArray(new String[0]), null, file, eventData.requiredRole());

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
			User adminUser = securityUser.getUser();
			Event event = eventDAO.getEventById(id);
			if (event == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(new ApiResponse(false, "Veranstaltung nicht gefunden.", null));
			}
			mapDtoToEvent(eventData, event);
			event.setId(id); // Ensure ID is set for update

			eventService.createOrUpdateEvent(event, true, adminUser,
					eventData.requiredCourseIds().toArray(new String[0]),
					eventData.requiredPersons().toArray(new String[0]), eventData.itemIds().toArray(new String[0]),
					eventData.quantities().toArray(new String[0]), null, file, eventData.requiredRole());

			return ResponseEntity.ok(new ApiResponse(true, "Veranstaltung erfolgreich aktualisiert.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(
					new ApiResponse(false, "Fehler beim Aktualisieren der Veranstaltung: " + e.getMessage(), null));
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

	private void mapDtoToEvent(EventUpdateRequest dto, Event event) {
		event.setName(dto.name());
		event.setEventDateTime(dto.eventDateTime());
		event.setEndDateTime(dto.endDateTime());
		event.setDescription(dto.description());
		event.setLocation(dto.location());
		event.setStatus(dto.status());
		event.setLeaderUserId(dto.leaderUserId() != null ? dto.leaderUserId() : 0);
	}
}