package de.technikteam.api.v1;

import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kits")
@Tag(name = "Admin Kits", description = "Endpoints for managing inventory kits.")
@SecurityRequirement(name = "bearerAuth")
public class KitResource {

	private final InventoryKitDAO kitDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public KitResource(InventoryKitDAO kitDAO, AdminLogService adminLogService) {
		this.kitDAO = kitDAO;
		this.adminLogService = adminLogService;
	}

	@GetMapping
	@Operation(summary = "Get all kits with their items")
	@PreAuthorize("hasAuthority('KIT_READ')")
	public ResponseEntity<ApiResponse> getAllKits() {
		List<InventoryKit> kits = kitDAO.getAllKitsWithItems();
		return ResponseEntity.ok(new ApiResponse(true, "Kits erfolgreich abgerufen.", kits));
	}

	@PostMapping
	@Operation(summary = "Create a new kit")
	@PreAuthorize("hasAuthority('KIT_CREATE')")
	public ResponseEntity<ApiResponse> createKit(@RequestBody InventoryKit kit,
			@AuthenticationPrincipal User adminUser) {
		int newId = kitDAO.createKit(kit);
		if (newId > 0) {
			kit.setId(newId);
			adminLogService.log(adminUser.getUsername(), "CREATE_KIT_API", "Kit '" + kit.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Kit erfolgreich erstellt.", kit), HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Kit konnte nicht erstellt werden.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a kit's metadata")
	@PreAuthorize("hasAuthority('KIT_UPDATE')")
	public ResponseEntity<ApiResponse> updateKit(@PathVariable int id, @RequestBody InventoryKit kit,
			@AuthenticationPrincipal User adminUser) {
		kit.setId(id);
		if (kitDAO.updateKit(kit)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_KIT_API", "Kit '" + kit.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Kit erfolgreich aktualisiert.", kit));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Kit nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a kit")
	@PreAuthorize("hasAuthority('KIT_DELETE')")
	public ResponseEntity<ApiResponse> deleteKit(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		InventoryKit kit = kitDAO.getKitById(id);
		if (kit != null && kitDAO.deleteKit(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_KIT_API", "Kit '" + kit.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Kit erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Kit nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}