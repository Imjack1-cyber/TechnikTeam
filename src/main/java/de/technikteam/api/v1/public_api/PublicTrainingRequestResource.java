package de.technikteam.api.v1.public_api;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.TrainingRequest;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.TrainingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/training-requests")
@Tag(name = "Public Training Requests", description = "Endpoints for user-initiated training requests.")
public class PublicTrainingRequestResource {

	private final TrainingRequestService trainingRequestService;

	@Autowired
	public PublicTrainingRequestResource(TrainingRequestService trainingRequestService) {
		this.trainingRequestService = trainingRequestService;
	}

	@PostMapping
	@Operation(summary = "Submit a new training request")
	public ResponseEntity<ApiResponse> submitRequest(@RequestBody Map<String, String> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String topic = payload.get("topic");
		if (topic == null || topic.isBlank()) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Topic cannot be empty.", null));
		}
		TrainingRequest request = trainingRequestService.create(topic, securityUser.getUser());
		return new ResponseEntity<>(new ApiResponse(true, "Training request submitted successfully.", request),
				HttpStatus.CREATED);
	}

	@PostMapping("/{id}/interest")
	@Operation(summary = "Register interest in a training request")
	public ResponseEntity<ApiResponse> registerInterest(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (trainingRequestService.registerInterest(id, securityUser.getUser().getId())) {
			return ResponseEntity.ok(new ApiResponse(true, "Interest registered successfully.", null));
		}
		return ResponseEntity.notFound().build();
	}
}