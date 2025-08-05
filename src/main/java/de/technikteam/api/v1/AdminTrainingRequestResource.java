package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.TrainingRequest;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.TrainingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/training-requests")
@Tag(name = "Admin Training Requests", description = "Endpoints for managing user-initiated training requests.")
public class AdminTrainingRequestResource {

	private final TrainingRequestService trainingRequestService;

	@Autowired
	public AdminTrainingRequestResource(TrainingRequestService trainingRequestService) {
		this.trainingRequestService = trainingRequestService;
	}

	@GetMapping
	@Operation(summary = "Get all training requests")
	public ResponseEntity<ApiResponse> getAllRequests() {
		List<TrainingRequest> requests = trainingRequestService.findAllWithInterestCount();
		return ResponseEntity.ok(new ApiResponse(true, "Training requests retrieved successfully.", requests));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a training request")
	public ResponseEntity<ApiResponse> deleteRequest(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (trainingRequestService.delete(id, securityUser.getUser())) {
			return ResponseEntity.ok(new ApiResponse(true, "Training request deleted successfully.", null));
		}
		return ResponseEntity.notFound().build();
	}
}