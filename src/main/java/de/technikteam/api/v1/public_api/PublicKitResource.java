package de.technikteam.api.v1.public_api;

import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.InventoryKit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/kits")
@Tag(name = "Public Kits", description = "Endpoints for viewing inventory kits.")
@SecurityRequirement(name = "bearerAuth")
public class PublicKitResource {

	private final InventoryKitDAO kitDAO;

	@Autowired
	public PublicKitResource(InventoryKitDAO kitDAO) {
		this.kitDAO = kitDAO;
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get a single kit with its items")
	public ResponseEntity<ApiResponse> getKitById(@PathVariable int id) {
		InventoryKit kit = kitDAO.getKitWithItemsById(id);
		if (kit != null) {
			return ResponseEntity.ok(new ApiResponse(true, "Kit details retrieved.", kit));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Kit not found.", null));
	}
}