package de.technikteam.api.v1.public_api;

import de.technikteam.dao.ChatDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.ChatConversation;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.ChatService;
import de.technikteam.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/chat")
@Tag(name = "Public Chat", description = "Endpoints for user direct messaging.")
@SecurityRequirement(name = "bearerAuth")
public class PublicChatResource {

	private final ChatDAO chatDAO;
	private final FileService fileService;
	private final ChatService chatService;

	@Autowired
	public PublicChatResource(ChatDAO chatDAO, FileService fileService, ChatService chatService) {
		this.chatDAO = chatDAO;
		this.fileService = fileService;
		this.chatService = chatService;
	}

	@GetMapping("/conversations")
	@Operation(summary = "Get user's conversations")
	public ResponseEntity<ApiResponse> getConversations(@AuthenticationPrincipal SecurityUser securityUser) {
		return ResponseEntity.ok(new ApiResponse(true, "Gespräche abgerufen.",
				chatDAO.getConversationsForUser(securityUser.getUser().getId())));
	}

	@GetMapping("/conversations/{id}")
	@Operation(summary = "Get a single conversation's details")
	public ResponseEntity<ApiResponse> getConversationById(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (!chatDAO.isUserInConversation(id, securityUser.getUser().getId())) {
			throw new AccessDeniedException("Sie sind kein Mitglied dieses Gesprächs.");
		}
		ChatConversation conversation = chatDAO.getConversationById(id);
		return ResponseEntity.ok(new ApiResponse(true, "Gespräch abgerufen.", conversation));
	}

	@GetMapping("/conversations/{id}/messages")
	@Operation(summary = "Get messages for a conversation")
	public ResponseEntity<ApiResponse> getMessages(@PathVariable int id, @RequestParam(defaultValue = "50") int limit,
			@RequestParam(defaultValue = "0") int offset, @AuthenticationPrincipal SecurityUser securityUser) {
		if (!chatDAO.isUserInConversation(id, securityUser.getUser().getId())) {
			throw new AccessDeniedException("Sie sind kein Mitglied dieses Gesprächs.");
		}
		return ResponseEntity.ok(
				new ApiResponse(true, "Nachrichten abgerufen.", chatDAO.getMessagesForConversation(id, limit, offset)));
	}

	@PostMapping("/conversations")
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

	@PostMapping("/conversations/group")
	@Operation(summary = "Create a new group conversation")
	public ResponseEntity<ApiResponse> createGroupConversation(@RequestBody Map<String, Object> payload,
			@AuthenticationPrincipal SecurityUser securityUser) {
		String name = (String) payload.get("name");
		@SuppressWarnings("unchecked")
		List<Integer> participantIds = (List<Integer>) payload.get("participantIds");

		if (name == null || name.isBlank() || participantIds == null || participantIds.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Gruppenname und Teilnehmer sind erforderlich.", null));
		}

		int conversationId = chatService.createGroupConversation(name, securityUser.getUser(), participantIds);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponse(true, "Gruppe erfolgreich erstellt.", Map.of("conversationId", conversationId)));
	}

	@PostMapping("/conversations/{id}/participants")
	@Operation(summary = "Add participants to a group")
	public ResponseEntity<ApiResponse> addParticipants(@PathVariable int id,
			@RequestBody Map<String, List<Integer>> payload, @AuthenticationPrincipal SecurityUser securityUser) {
		ChatConversation conversation = chatDAO.getConversationById(id);
		if (conversation == null) {
			return new ResponseEntity<>(new ApiResponse(false, "Gespräch nicht gefunden.", null), HttpStatus.NOT_FOUND);
		}
		if (!conversation.isGroupChat() || conversation.getCreatorId() != securityUser.getUser().getId()) {
			throw new AccessDeniedException("Nur der Ersteller der Gruppe kann Mitglieder hinzufügen.");
		}
		List<Integer> userIds = payload.get("userIds");
		if (userIds == null || userIds.isEmpty()) {
			return ResponseEntity.badRequest().body(new ApiResponse(false, "Keine Benutzer-IDs angegeben.", null));
		}

		chatDAO.addParticipantsToGroup(id, userIds);
		return ResponseEntity.ok(new ApiResponse(true, "Teilnehmer erfolgreich hinzugefügt.", null));
	}

	@DeleteMapping("/conversations/{id}/participants/{userId}")
	@Operation(summary = "Remove a participant from a group")
	public ResponseEntity<ApiResponse> removeParticipant(@PathVariable int id, @PathVariable int userId,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			if (chatService.removeParticipantFromGroup(id, userId, securityUser.getUser())) {
				return ResponseEntity.ok(new ApiResponse(true, "Teilnehmer erfolgreich entfernt.", null));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Entfernen des Teilnehmers fehlgeschlagen.", null));
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
		} catch (AccessDeniedException e) {
			return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/conversations/{id}/leave")
	@Operation(summary = "Leave a group conversation")
	public ResponseEntity<ApiResponse> leaveGroup(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		if (chatService.leaveGroup(id, securityUser.getUser().getId())) {
			return ResponseEntity.ok(new ApiResponse(true, "Gruppe erfolgreich verlassen.", null));
		}
		return ResponseEntity.badRequest().body(new ApiResponse(false, "Verlassen der Gruppe fehlgeschlagen.", null));
	}

	@DeleteMapping("/conversations/{id}")
	@Operation(summary = "Delete a group conversation")
	public ResponseEntity<ApiResponse> deleteGroup(@PathVariable int id,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			if (chatService.deleteGroup(id, securityUser.getUser())) {
				return ResponseEntity.ok(new ApiResponse(true, "Gruppe erfolgreich gelöscht.", null));
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Löschen der Gruppe fehlgeschlagen.", null));
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.NOT_FOUND);
		} catch (AccessDeniedException e) {
			return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.FORBIDDEN);
		}
	}

	@PostMapping("/upload")
	@Operation(summary = "Upload a file for chat")
	public ResponseEntity<ApiResponse> uploadChatFile(@RequestParam("file") MultipartFile file,
			@AuthenticationPrincipal SecurityUser securityUser) {
		try {
			de.technikteam.model.File savedFile = fileService.storeFile(file, null, "NUTZER", securityUser.getUser(),
					"chat");
			return ResponseEntity.ok(new ApiResponse(true, "Datei hochgeladen.", savedFile));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Datei-Upload fehlgeschlagen: " + e.getMessage(), null));
		}
	}
}