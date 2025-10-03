package de.technikteam.api.v1;

import de.technikteam.dao.EventRoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.EventRole;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/event-roles")
@Tag(name = "Admin Event Roles", description = "Endpoints for managing predefined event roles.")
@SecurityRequirement(name = "bearerAuth")
public class AdminEventRoleResource {

	private final EventRoleDAO eventRoleDAO;
	private final NotificationService notificationService;

	@Autowired
	public AdminEventRoleResource(EventRoleDAO eventRoleDAO, NotificationService notificationService) {
		this.eventRoleDAO = eventRoleDAO;
		this.notificationService = notificationService;
	}

	@GetMapping
	@Operation(summary = "Get all event roles")
	public ResponseEntity<ApiResponse> getAllRoles() {
		List<EventRole> roles = eventRoleDAO.findAll();
		return ResponseEntity.ok(new ApiResponse(true, "Rollen erfolgreich abgerufen.", roles));
	}

	@PostMapping
	@Operation(summary = "Create a new event role")
	public ResponseEntity<ApiResponse> createRole(@Valid @RequestBody EventRole role) {
		if (eventRoleDAO.create(role)) {
			notificationService.broadcastUIUpdate("EVENT_ROLE", "CREATED", role);
			return new ResponseEntity<>(new ApiResponse(true, "Rolle erfolgreich erstellt.", role), HttpStatus.CREATED);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(false, "Rolle konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an event role")
	public ResponseEntity<ApiResponse> updateRole(@PathVariable int id, @Valid @RequestBody EventRole role) {
		role.setId(id);
		if (eventRoleDAO.update(role)) {
			notificationService.broadcastUIUpdate("EVENT_ROLE", "UPDATED", role);
			return ResponseEntity.ok(new ApiResponse(true, "Rolle erfolgreich aktualisiert.", role));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Rolle nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an event role")
	public ResponseEntity<ApiResponse> deleteRole(@PathVariable int id) {
		if (eventRoleDAO.delete(id)) {
			notificationService.broadcastUIUpdate("EVENT_ROLE", "DELETED", Map.of("id", id));
			return ResponseEntity.ok(new ApiResponse(true, "Rolle erfolgreich gelöscht.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Rolle nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}