package de.technikteam.model;

import java.time.LocalDateTime;

public class ChatConversation {
	private int id;
	private LocalDateTime createdAt;

	// Transient fields for UI
	private int otherParticipantId;
	private String otherParticipantUsername;
	private String lastMessage;
	private LocalDateTime lastMessageTimestamp;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
}