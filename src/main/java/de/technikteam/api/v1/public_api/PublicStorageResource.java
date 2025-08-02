package de.technikteam.api.v1.public_api;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Event;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/storage")
@Tag(name = "Public Storage", description = "Endpoints for user interaction with the inventory.")
@SecurityRequirement(name = "bearerAuth")
public class PublicStorageResource {

	private final StorageService storageService;
	private final StorageDAO storageDAO;
	private final EventDAO eventDAO;

	@Autowired
	public PublicStorageResource(StorageService storageService, StorageDAO storageDAO, EventDAO eventDAO) {
		this.storageService = storageService;
		this.storageDAO = storageDAO;
		this.eventDAO = eventDAO;
	}

	@GetMapping
	@Operation(summary = "Get all storage data for display", description = "Retrieves all storage items grouped by location and a list of active events for the transaction modal.")
	public ResponseEntity<ApiResponse> getStoragePageData() {
		Map<String, List<StorageItem>> storageData = storageDAO.getAllItemsGroupedByLocation();
		List<Event> activeEvents = eventDAO.getActiveEvents();

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("storageData", storageData);
		responseData.put("activeEvents", activeEvents);

		return ResponseEntity.ok(new ApiResponse(true, "Storage data retrieved.", responseData));
	}

	@PostMapping("/transactions")
	@Operation(summary = "Perform a storage transaction", description = "Checks out or checks in a specified quantity of a storage item.")
	public ResponseEntity<ApiResponse> performTransaction(@RequestBody Map<String, Object> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		User user = securityUser.getUser();
		try {
			int itemId = ((Number) payload.get("itemId")).intValue();
			int quantity = ((Number) payload.get("quantity")).intValue();
			String type = (String) payload.get("type");
			String notes = (String) payload.get("notes");
			Integer eventId = payload.get("eventId") != null && !payload.get("eventId").toString().isEmpty()
					? ((Number) payload.get("eventId")).intValue()
					: null;

			if (storageService.processTransaction(itemId, quantity, type, user, eventId, notes)) {
				String action = "checkin".equals(type) ? "eingeräumt" : "entnommen";
				return ResponseEntity
						.ok(new ApiResponse(true, "Erfolgreich " + quantity + " Stück " + action + ".", null));
			} else {
				return ResponseEntity.badRequest().body(new ApiResponse(false, "Transaktion fehlgeschlagen.", null));
			}
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(new ApiResponse(false, "Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage(), null));
		}
	}
}