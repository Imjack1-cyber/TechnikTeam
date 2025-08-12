package de.technikteam.api.v1;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.FileService;
import de.technikteam.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/storage")
@Tag(name = "Admin Storage", description = "Endpoints for managing inventory items.")
public class StorageResource {

	private final StorageDAO storageDAO;
	private final StorageService storageService;
	private final AdminLogService adminLogService;
	private final FileService fileService;

	@Autowired
	public StorageResource(StorageDAO storageDAO, StorageService storageService, AdminLogService adminLogService,
			FileService fileService) {
		this.storageDAO = storageDAO;
		this.storageService = storageService;
		this.adminLogService = adminLogService;
		this.fileService = fileService;
	}

	@GetMapping
	@Operation(summary = "Get all storage items")
	public ResponseEntity<ApiResponse> getAllItems() {
		List<StorageItem> items = storageDAO.getAllItems();
		return ResponseEntity.ok(new ApiResponse(true, "Artikel erfolgreich abgerufen.", items));
	}

	@PostMapping
	@Operation(summary = "Create a new storage item")
	public ResponseEntity<ApiResponse> createItem(@ModelAttribute StorageItem item,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			if (imageFile != null && !imageFile.isEmpty()) {
				de.technikteam.model.File savedFile = fileService.storeFile(imageFile, null, "NUTZER",
						securityUser.getUser(), "images");
				item.setImagePath(savedFile.getFilepath());
			}

			if (storageDAO.createItem(item)) {
				adminLogService.log(securityUser.getUser().getUsername(), "CREATE_STORAGE_ITEM_API",
						"Item '" + item.getName() + "' created.");
				return new ResponseEntity<>(new ApiResponse(true, "Artikel erfolgreich erstellt.", item),
						HttpStatus.CREATED);
			}
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Artikel konnte nicht erstellt werden.", null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Fehler beim Erstellen des Artikels: " + e.getMessage(), null));
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a storage item's defect/repair status")
	public ResponseEntity<ApiResponse> updateItemStatus(@PathVariable int id, @RequestBody Map<String, Object> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			// This endpoint is now specifically for defect/repair actions
			storageService.handleItemStatusUpdate(id, payload, securityUser.getUser());
			return ResponseEntity.ok(new ApiResponse(true, "Artikelstatus erfolgreich aktualisiert.", null));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/{id}")
	@Operation(summary = "Update a storage item's core details")
	public ResponseEntity<ApiResponse> updateItemDetails(@PathVariable int id, @ModelAttribute StorageItem item,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			item.setId(id);
			if (imageFile != null && !imageFile.isEmpty()) {
				de.technikteam.model.File savedFile = fileService.storeFile(imageFile, null, "NUTZER",
						securityUser.getUser(), "images");
				item.setImagePath(savedFile.getFilepath());
			}

			if (storageDAO.updateItem(item)) {
				adminLogService.log(securityUser.getUser().getUsername(), "UPDATE_STORAGE_ITEM_API",
						"Item '" + item.getName() + "' updated.");
				return ResponseEntity.ok(new ApiResponse(true, "Artikel erfolgreich aktualisiert.", item));
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ApiResponse(false, "Artikel nicht gefunden oder Aktualisierung fehlgeschlagen.", null));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Fehler beim Aktualisieren des Artikels: " + e.getMessage(), null));
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a storage item")
	public ResponseEntity<ApiResponse> deleteItem(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		StorageItem item = storageDAO.getItemById(id);
		if (item != null && storageDAO.deleteItem(id)) {
			adminLogService.log(securityUser.getUser().getUsername(), "DELETE_STORAGE_ITEM_API",
					"Item '" + item.getName() + "' deleted.");
			return ResponseEntity.ok(new ApiResponse(true, "Artikel erfolgreich gelöscht.", Map.of("deletedId", id)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "Artikel nicht gefunden oder Löschung fehlgeschlagen.", null));
	}
}