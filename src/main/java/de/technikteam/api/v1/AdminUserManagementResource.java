package de.technikteam.api.v1;

import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminUserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin Users", description = "Admin endpoints for managing users, including suspension.")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserManagementResource {

	private final AdminUserManagementService adminService;

	@Autowired
	public AdminUserManagementResource(AdminUserManagementService adminService) {
		this.adminService = adminService;
	}

	@PostMapping("/{userId}/suspend")
	public ResponseEntity<ApiResponse> suspendUser(@PathVariable int userId, @RequestBody SuspendRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (request == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Request body required.", null));
		}
		try {
			boolean success = adminService.suspendUser(userId, request.duration, request.reason,
					securityUser.getUser());
			if (success) {
				return ResponseEntity.ok(new ApiResponse(true, "User suspended successfully.", null));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(new ApiResponse(false, "Failed to suspend user.", null));
			}
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage(), null));
		}
	}

	@PostMapping("/{userId}/unsuspend")
	public ResponseEntity<ApiResponse> unsuspendUser(@PathVariable int userId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		boolean success = adminService.unsuspendUser(userId, securityUser.getUser());
		if (success) {
			return ResponseEntity.ok(new ApiResponse(true, "User unsuspended and unlocked.", null));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ApiResponse(false, "Failed to unsuspend user.", null));
	}

	public record SuspendRequest(String duration, String reason) {
	}
}