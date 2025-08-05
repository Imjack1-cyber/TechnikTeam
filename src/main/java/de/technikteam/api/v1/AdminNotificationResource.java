package de.technikteam.api.v1;

import de.technikteam.api.v1.dto.NotificationRequest;
import de.technikteam.config.Permissions;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@Tag(name = "Admin Notifications", description = "Endpoints for sending and receiving notifications.")
@SecurityRequirement(name = "bearerAuth")
public class AdminNotificationResource {

	private final NotificationService notificationService;

	@Autowired
	public AdminNotificationResource(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/sse")
	@Operation(summary = "Subscribe to SSE notifications", description = "Establishes a Server-Sent Events connection for real-time notifications.")
	public SseEmitter handleSse(@AuthenticationPrincipal SecurityUser securityUser) {
		return notificationService.register(securityUser.getUser());
	}

	@PostMapping
	@Operation(summary = "Send a broadcast notification", description = "Sends a real-time notification to a specified group of users.")
	public ResponseEntity<ApiResponse> sendNotification(@Valid @RequestBody NotificationRequest request,
			@AuthenticationPrincipal SecurityUser securityUser) {

		User adminUser = securityUser.getUser();
		// Manual authorization check instead of @PreAuthorize
		if (adminUser.getPermissions() == null || !adminUser.getPermissions().contains(Permissions.NOTIFICATION_SEND)) {
			return new ResponseEntity<>(new ApiResponse(false, "Zugriff verweigert.", null), HttpStatus.FORBIDDEN);
		}

		try {
			int recipients = notificationService.sendBroadcastNotification(request, adminUser);
			return ResponseEntity
					.ok(new ApiResponse(true, "Benachrichtigung an " + recipients + " Empfänger gesendet.", null));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(
					new ApiResponse(false, "Senden der Benachrichtigung fehlgeschlagen: " + e.getMessage(), null));
		}
	}
}