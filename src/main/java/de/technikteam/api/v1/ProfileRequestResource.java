package de.technikteam.api.v1;

import de.technikteam.dao.ProfileChangeRequestDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ProfileChangeRequest;
import de.technikteam.model.User;
import de.technikteam.service.ProfileRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile-requests")
@Tag(name = "Admin Profile Requests", description = "Endpoints for managing user profile change requests.")
@SecurityRequirement(name = "bearerAuth")
public class ProfileRequestResource {

	private final ProfileChangeRequestDAO requestDAO;
	private final ProfileRequestService requestService;

	@Autowired
	public ProfileRequestResource(ProfileChangeRequestDAO requestDAO, ProfileRequestService requestService) {
		this.requestDAO = requestDAO;
		this.requestService = requestService;
	}

	@GetMapping
	@Operation(summary = "Get all pending profile change requests")
	public ResponseEntity<ApiResponse> getPendingRequests() {
		List<ProfileChangeRequest> pendingRequests = requestDAO.getPendingRequests();
		return ResponseEntity.ok(new ApiResponse(true, "Pending requests retrieved.", pendingRequests));
	}

	@PostMapping("/{id}/approve")
	@Operation(summary = "Approve a profile change request")
	public ResponseEntity<ApiResponse> approveRequest(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		try {
			if (requestService.approveRequest(id, adminUser)) {
				return ResponseEntity.ok(new ApiResponse(true, "Change request approved.", Map.of("requestId", id)));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Failed to apply changes or update the request.", null));
			}
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/{id}/deny")
	@Operation(summary = "Deny a profile change request")
	public ResponseEntity<ApiResponse> denyRequest(@PathVariable int id, @AuthenticationPrincipal User adminUser) {
		try {
			if (requestService.denyRequest(id, adminUser)) {
				return ResponseEntity.ok(new ApiResponse(true, "Change request denied.", Map.of("requestId", id)));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Failed to deny the request.", null));
			}
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null));
		}
	}
}