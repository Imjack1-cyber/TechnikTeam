package de.technikteam.api.v1.public_api;

import de.technikteam.dao.MaintenanceLogDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.MaintenanceLogEntry;
import de.technikteam.model.StorageItem;
import de.technikteam.model.StorageLogEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/storage")
@Tag(name = "Public Storage", description = "Endpoints for user interaction with the inventory.")
@SecurityRequirement(name = "bearerAuth")
public class PublicStorageDetailsResource {

	private final StorageDAO storageDAO;
	private final StorageLogDAO storageLogDAO;
	private final MaintenanceLogDAO maintenanceLogDAO;

	@Autowired
	public PublicStorageDetailsResource(StorageDAO storageDAO, StorageLogDAO storageLogDAO,
			MaintenanceLogDAO maintenanceLogDAO) {
		this.storageDAO = storageDAO;
		this.storageLogDAO = storageLogDAO;
		this.maintenanceLogDAO = maintenanceLogDAO;
	}

	@GetMapping("/{itemId}")
	@Operation(summary = "Get a single storage item by ID", description = "Retrieves detailed information for a single inventory item.")
	public ResponseEntity<ApiResponse> getStorageItemById(
			@Parameter(description = "ID of the storage item") @PathVariable int itemId) {
		StorageItem item = storageDAO.getItemById(itemId);
		if (item == null) {
			return new ResponseEntity<>(new ApiResponse(false, "Artikel nicht gefunden.", null), HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(new ApiResponse(true, "Artikeldetails erfolgreich abgerufen.", item));
	}

	@GetMapping("/{itemId}/history")
	@Operation(summary = "Get history for a storage item", description = "Retrieves the transaction and maintenance history for a single inventory item.")
	public ResponseEntity<ApiResponse> getStorageItemHistory(
			@Parameter(description = "ID of the storage item") @PathVariable int itemId) {
		List<StorageLogEntry> transactions = storageLogDAO.getHistoryForItem(itemId);
		List<MaintenanceLogEntry> maintenance = maintenanceLogDAO.getHistoryForItem(itemId);

		Map<String, Object> historyData = new HashMap<>();
		historyData.put("transactions", transactions);
		historyData.put("maintenance", maintenance);

		return ResponseEntity.ok(new ApiResponse(true, "Verlauf erfolgreich abgerufen.", historyData));
	}
}