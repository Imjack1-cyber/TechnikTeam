package de.technikteam.api.v1;

import de.technikteam.dao.EventRoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.EventRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/event-roles")
@Tag(name = "Admin Event Roles", description = "Endpoints for managing predefined event roles.")
@SecurityRequirement(name = "bearerAuth")
public class AdminEventRoleResource {

	private final EventRoleDAO eventRoleDAO;

	@Autowired
	public AdminEventRoleResource(EventRoleDAO eventRoleDAO) {
		this.eventRoleDAO = eventRoleDAO;
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
			return ResponseEntity.ok(new ApiResponse(true, "Rolle erfolgreich aktualisiert.", role));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Rolle nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete an event role")
	public ResponseEntity<ApiResponse> deleteRole(@PathVariable int id) {
		if (eventRoleDAO.delete(id)) {
			return ResponseEntity.ok(new ApiResponse(true, "Rolle erfolgreich gelöscht.", null));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Rolle nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}