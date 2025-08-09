package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

public class ChatConversation {
	private int id;
	private boolean isGroupChat;
	private String name;
	private Integer creatorId;
	private LocalDateTime createdAt;

	// Transient fields for UI
	private int otherParticipantId;
	private String otherParticipantUsername;
	private String lastMessage;
	private LocalDateTime lastMessageTimestamp;
	private List<User> participants;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isGroupChat() {
		return isGroupChat;
	}

	public void setGroupChat(boolean groupChat) {
		isGroupChat = groupChat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getOtherParticipantId() {
		return otherParticipantId;
	}

	public void setOtherParticipantId(int otherParticipantId) {
		this.otherParticipantId = otherParticipantId;
	}

	public String getOtherParticipantUsername() {
		return otherParticipantUsername;
	}

	public void setOtherParticipantUsername(String otherParticipantUsername) {
		this.otherParticipantUsername = otherParticipantUsername;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public LocalDateTime getLastMessageTimestamp() {
		return lastMessageTimestamp;
	}

	public void setLastMessageTimestamp(LocalDateTime lastMessageTimestamp) {
		this.lastMessageTimestamp = lastMessageTimestamp;
	}

	public List<User> getParticipants() {
		return participants;
	}

	public void setParticipants(List<User> participants) {
		this.participants = participants;
	}
}