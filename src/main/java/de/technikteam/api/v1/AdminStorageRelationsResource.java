package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.service.StorageItemRelationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/storage/{itemId}/relations")
@Tag(name = "Admin Storage", description = "Endpoints for managing inventory items.")
public class AdminStorageRelationsResource {

	private final StorageItemRelationService relationService;

	@Autowired
	public AdminStorageRelationsResource(StorageItemRelationService relationService) {
		this.relationService = relationService;
	}

	@GetMapping
	@Operation(summary = "Get related items for a storage item")
	public ResponseEntity<ApiResponse> getRelatedItems(@PathVariable int itemId) {
		return ResponseEntity
				.ok(new ApiResponse(true, "Related items retrieved.", relationService.findRelatedItems(itemId)));
	}

	@PutMapping
	@Operation(summary = "Update related items for a storage item")
	public ResponseEntity<ApiResponse> updateRelatedItems(@PathVariable int itemId,
			@RequestBody Map<String, List<Integer>> payload) {
		List<Integer> relatedItemIds = payload.get("relatedItemIds");
		if (relatedItemIds == null) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Payload must contain 'relatedItemIds'.", null));
		}
		relationService.updateRelations(itemId, relatedItemIds);
		return ResponseEntity.ok(new ApiResponse(true, "Related items updated successfully.", null));
	}
}