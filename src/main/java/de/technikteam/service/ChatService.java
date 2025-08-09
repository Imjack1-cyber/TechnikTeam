package de.technikteam.service;

import de.technikteam.dao.ChatDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ChatConversation;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ChatService {
	private static final Logger logger = LogManager.getLogger(ChatService.class);

	private final ChatDAO chatDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public ChatService(ChatDAO chatDAO, UserDAO userDAO, AdminLogService adminLogService) {
		this.chatDAO = chatDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
	}

	@Transactional
	public boolean leaveGroup(int conversationId, int userId) {
		boolean success = chatDAO.leaveGroup(conversationId, userId);
		if (success) {
			// Check if group is now empty
			List<User> participants = chatDAO.getParticipantsForConversation(conversationId);
			if (participants.isEmpty()) {
				chatDAO.deleteGroup(conversationId);
			}
		}
		return success;
	}

	@Transactional
	public int createGroupConversation(String name, User creator, List<Integer> participantIds) {
		int conversationId = chatDAO.createGroupConversation(name, creator.getId(), participantIds);

		List<String> participantNames = participantIds.stream().map(userDAO::getUserById).filter(Objects::nonNull)
				.map(User::getUsername).collect(Collectors.toList());

		String logDetails = String.format("User '%s' created group chat '%s' with participants: %s",
				creator.getUsername(), name, String.join(", ", participantNames));

		// Log to console for immediate visibility
		logger.info(logDetails);

		// Log to database audit trail if creator is an admin
		if (creator.hasAdminAccess()) {
			adminLogService.log(creator.getUsername(), "GROUP_CHAT_CREATE", logDetails);
		}

		return conversationId;
	}

	@Transactional
	public boolean deleteGroup(int conversationId, User currentUser) {
		ChatConversation conversation = chatDAO.getConversationById(conversationId);
		if (conversation == null) {
			throw new IllegalArgumentException("Gespräch nicht gefunden.");
		}

		// Authorization check: Only group creator or an admin can delete a group.
		if (!conversation.isGroupChat()) {
			throw new AccessDeniedException("Nur Gruppen-Chats können gelöscht werden.");
		}

		// Use Objects.equals to safely compare Integer with int.
		if (!Objects.equals(conversation.getCreatorId(), currentUser.getId()) && !currentUser.hasAdminAccess()) {
			throw new AccessDeniedException("Nur der Ersteller der Gruppe oder ein Admin kann diese löschen.");
		}

		return chatDAO.deleteGroup(conversationId);
	}

	@Transactional
	public boolean removeParticipantFromGroup(int conversationId, int userIdToRemove, User currentUser) {
		ChatConversation conversation = chatDAO.getConversationById(conversationId);
		if (conversation == null) {
			throw new IllegalArgumentException("Gespräch nicht gefunden.");
		}

		// Authorization check: Only group creator or an admin can remove users.
		if (!conversation.isGroupChat() || (!Objects.equals(conversation.getCreatorId(), currentUser.getId())
				&& !currentUser.hasAdminAccess())) {
			throw new AccessDeniedException("Nur der Ersteller der Gruppe oder ein Admin kann Mitglieder entfernen.");
		}

		// Prevent creator from removing themselves this way (they should use "Leave
		// Group")
		if (Objects.equals(conversation.getCreatorId(), userIdToRemove)) {
			throw new IllegalArgumentException("Der Ersteller der Gruppe kann sich nicht selbst entfernen.");
		}

		return chatDAO.removeParticipant(conversationId, userIdToRemove);
	}
}