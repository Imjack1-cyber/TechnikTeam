package de.technikteam.api.v1;

import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.InventoryKitItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kits/{kitId}/items")
@Tag(name = "Admin Kits", description = "Endpoints for managing inventory kits.")
public class AdminKitItemsResource {

	private final InventoryKitDAO kitDAO;

	@Autowired
	public AdminKitItemsResource(InventoryKitDAO kitDAO) {
		this.kitDAO = kitDAO;
	}

	@PutMapping
	@Operation(summary = "Update the items within a kit")
	public ResponseEntity<ApiResponse> updateKitItems(@PathVariable int kitId,
			@RequestBody List<InventoryKitItem> items) {
		try {
			kitDAO.updateKitItems(kitId, items);
			return ResponseEntity.ok(new ApiResponse(true, "Kit-Inhalt erfolgreich aktualisiert.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Fehler beim Aktualisieren des Kit-Inhalts: " + e.getMessage(), null));
		}
	}
}