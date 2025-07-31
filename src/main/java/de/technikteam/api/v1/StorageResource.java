package de.technikteam.api.v1;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.StorageItem;
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
@RequestMapping("/api/v1/storage")
@Tag(name = "Admin Storage", description = "Endpoints for managing inventory items.")
@SecurityRequirement(name = "bearerAuth")
public class StorageResource {

	private final StorageDAO storageDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public StorageResource(StorageDAO storageDAO, AdminLogService adminLogService) {
		this.storageDAO = storageDAO;
		this.adminLogService = adminLogService;
	}

	@GetMapping
	@Operation(summary = "Get all storage items")
	@PreAuthorize("hasAuthority('STORAGE_READ')")
	public ResponseEntity<ApiResponse> getAllItems() {
		List<StorageItem> items = storageDAO.getAllItems();
		return ResponseEntity.ok(new ApiResponse(true, "Items retrieved successfully", items));
	}

	@PostMapping
	@Operation(summary = "Create a new storage item")
	@PreAuthorize("hasAuthority('STORAGE_CREATE')")
	public ResponseEntity<ApiResponse> createItem(@RequestBody StorageItem item,
			@AuthenticationPrincipal User adminUser) {
		if (storageDAO.createItem(item)) {
			adminLogService.log(adminUser.getUsername(), "CREATE_STORAGE_ITEM_API",
					"Item '" + item.getName() + "' created.");
			return new ResponseEntity<>(new ApiResponse(true, "Item created successfully", item), HttpStatus.CREATED);
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Could not create item.", null));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a storage item")
	@PreAuthorize("hasAuthority('STORAGE_UPDATE')")
	public ResponseEntity<ApiResponse> updateItem(@PathVariable int id, @RequestBody StorageItem item,
			@AuthenticationPrincipal User adminUser) {
		item.setId(id);
		if (storageDAO.updateItem(item)) {
			adminLogService.log(adminUser.getUsername(), "UPDATE_STORAGE_ITEM_API",
					"Item '" + item.getName() + "' updated.");
			return ResponseEntity.ok(new ApiResponse(true, "Item updated successfully", item));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Item not found or update failed.", null));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a storage item")
	@PreAuthorize("hasAuthority('STORAGE_DELETE')")
	public ResponseEntity<ApiResponse> deleteItem(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		StorageItem item = storageDAO.getItemById(id);
		if (item != null && storageDAO.deleteItem(id)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_STORAGE_ITEM_API",
					"Item '" + item.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Item deleted successfully", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Item not found or delete failed.", null));
	}
}