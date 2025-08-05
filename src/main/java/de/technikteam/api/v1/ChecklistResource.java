package de.technikteam.api.v1;

import de.technikteam.dao.ChecklistDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ChecklistItem;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/events/{eventId}/checklist")
@Tag(name = "Event Checklists", description = "Endpoints for managing event inventory checklists.")
public class ChecklistResource {

	private final ChecklistDAO checklistDAO;

	@Autowired
	public ChecklistResource(ChecklistDAO checklistDAO) {
		this.checklistDAO = checklistDAO;
	}

	@GetMapping
	@Operation(summary = "Get the checklist for an event")
	public ResponseEntity<ApiResponse> getChecklist(@PathVariable int eventId) {
		List<ChecklistItem> checklist = checklistDAO.getChecklistForEvent(eventId);
		return ResponseEntity.ok(new ApiResponse(true, "Checkliste erfolgreich abgerufen.", checklist));
	}

	@PostMapping("/generate")
	@Operation(summary = "Generate or refresh a checklist from reservations")
	public ResponseEntity<ApiResponse> generateChecklist(@PathVariable int eventId) {
		int rowsAffected = checklistDAO.generateChecklistFromReservations(eventId);
		return ResponseEntity
				.ok(new ApiResponse(true, rowsAffected + " Einträge in der Checkliste erstellt/aktualisiert.", null));
	}

	@PutMapping("/{checklistItemId}/status")
	@Operation(summary = "Update the status of a checklist item")
	public ResponseEntity<ApiResponse> updateStatus(@PathVariable int eventId, @PathVariable int checklistItemId,
			@RequestBody Map<String, String> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		String status = payload.get("status");
		if (status == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Status ist erforderlich.", null));
		}

		if (checklistDAO.updateChecklistItemStatus(checklistItemId, status, securityUser.getUser().getId())) {
			ChecklistItem updatedItem = checklistDAO.getChecklistItemById(checklistItemId);
			return ResponseEntity.ok(new ApiResponse(true, "Status erfolgreich aktualisiert.", updatedItem));
		} else {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Status konnte nicht aktualisiert werden.", null));
		}
	}
}