package de.technikteam.api.v1.public_api;

import de.technikteam.dao.UserNotificationDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.UserNotification;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/notifications")
@Tag(name = "Public Notifications", description = "Endpoints for viewing and managing user notifications.")
@SecurityRequirement(name = "bearerAuth")
public class PublicNotificationResource {

	private final UserNotificationDAO userNotificationDAO;
	private final NotificationService notificationService;

	@Autowired
	public PublicNotificationResource(UserNotificationDAO userNotificationDAO,
			NotificationService notificationService) {
		this.userNotificationDAO = userNotificationDAO;
		this.notificationService = notificationService;
	}

	@GetMapping("/sse")
	@Operation(summary = "Subscribe to SSE notifications", description = "Establishes a Server-Sent Events connection for real-time notifications.")
	public ResponseEntity<SseEmitter> handleSse(@AuthenticationPrincipal SecurityUser securityUser) {
		if (securityUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		SseEmitter emitter = notificationService.register(securityUser.getUser());
		if (emitter == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_EVENT_STREAM);
		headers.setCacheControl("no-cache");
		headers.setConnection("keep-alive");

		return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
	}

	@GetMapping
	@Operation(summary = "Get all notifications for the current user")
	public ResponseEntity<ApiResponse> getNotifications(@AuthenticationPrincipal SecurityUser securityUser) {
		List<UserNotification> notifications = userNotificationDAO.findByUser(securityUser.getUser().getId());
		return ResponseEntity.ok(new ApiResponse(true, "Benachrichtigungen abgerufen.", notifications));
	}

	@PostMapping("/mark-all-seen")
	@Operation(summary = "Mark all of the user's notifications as seen")
	public ResponseEntity<ApiResponse> markAllAsSeen(@AuthenticationPrincipal SecurityUser securityUser) {
		userNotificationDAO.markAllAsSeen(securityUser.getUser().getId());
		return ResponseEntity.ok(new ApiResponse(true, "Alle Benachrichtigungen als gelesen markiert.", null));
	}
}