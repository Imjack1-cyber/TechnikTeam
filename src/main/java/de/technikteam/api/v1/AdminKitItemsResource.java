package de.technikteam.api.v1;

import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.InventoryKitItem;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/kits/{kitId}/items")
@Tag(name = "Admin Kits", description = "Endpoints for managing inventory kits.")
public class AdminKitItemsResource {

	private final InventoryKitDAO kitDAO;
	private final NotificationService notificationService;

	@Autowired
	public AdminKitItemsResource(InventoryKitDAO kitDAO, NotificationService notificationService) {
		this.kitDAO = kitDAO;
		this.notificationService = notificationService;
	}

	@PutMapping
	@Operation(summary = "Update the items within a kit")
	public ResponseEntity<ApiResponse> updateKitItems(@PathVariable int kitId,
			@RequestBody List<InventoryKitItem> items) {
		try {
			kitDAO.updateKitItems(kitId, items);
			notificationService.broadcastUIUpdate("KIT", "UPDATED", Map.of("id", kitId));
			return ResponseEntity.ok(new ApiResponse(true, "Kit-Inhalt erfolgreich aktualisiert.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Fehler beim Aktualisieren des Kit-Inhalts: " + e.getMessage(), null));
		}
	}
}