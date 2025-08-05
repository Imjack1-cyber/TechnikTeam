package de.technikteam.api.v1.public_api;

import de.technikteam.dao.ChatDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/chat")
@Tag(name = "Public Chat", description = "Endpoints for user direct messaging.")
@SecurityRequirement(name = "bearerAuth")
public class PublicChatResource {

	private final ChatDAO chatDAO;

	@Autowired
	public PublicChatResource(ChatDAO chatDAO) {
		this.chatDAO = chatDAO;
	}

	@GetMapping("/conversations")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get user's conversations")
	public ResponseEntity<ApiResponse> getConversations(@AuthenticationPrincipal SecurityUser securityUser) {
		return ResponseEntity.ok(new ApiResponse(true, "Gespräche abgerufen.",
				chatDAO.getConversationsForUser(securityUser.getUser().getId())));
	}

	@GetMapping("/conversations/{id}/messages")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get messages for a conversation")
	public ResponseEntity<ApiResponse> getMessages(@PathVariable int id, @RequestParam(defaultValue = "50") int limit,
			@RequestParam(defaultValue = "0") int offset, @AuthenticationPrincipal SecurityUser securityUser) {
		if (!chatDAO.isUserInConversation(id, securityUser.getUser().getId())) {
			return new ResponseEntity<>(new ApiResponse(false, "Nicht autorisiert.", null), HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(
				new ApiResponse(true, "Nachrichten abgerufen.", chatDAO.getMessagesForConversation(id, limit, offset)));
	}

	@PostMapping("/conversations")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Find or create a conversation with a user")
	public ResponseEntity<ApiResponse> findOrCreateConversation(@RequestBody Map<String, Integer> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		Integer otherUserId = payload.get("userId");
		if (otherUserId == null) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Benutzer-ID fehlt.", null));
		}
		int conversationId = chatDAO.findOrCreateConversation(securityUser.getUser().getId(), otherUserId);
		return ResponseEntity.ok(
				new ApiResponse(true, "Gespräch gefunden oder erstellt.", Map.of("conversationId", conversationId)));
	}
}