package de.technikteam.model;

import java.time.LocalDateTime;

public class ChatMessage {
	private long id;
	private int conversationId;
	private int senderId;
	private String senderUsername;
	private String messageText;
	private String status; 
	private LocalDateTime sentAt;
	private String chatColor;
	private boolean edited;
	private LocalDateTime editedAt;
	private boolean isDeleted;
	private LocalDateTime deletedAt;
	private Integer deletedByUserId;
	private String deletedByUsername; 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getConversationId() {
		return conversationId;
	}

	public void setConversationId(int conversationId) {
		this.conversationId = conversationId;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public String getSenderUsername() {
		return senderUsername;
	}

	public void setSenderUsername(String senderUsername) {
		this.senderUsername = senderUsername;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public String getChatColor() {
		return chatColor;
	}

	public void setChatColor(String chatColor) {
		this.chatColor = chatColor;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	public LocalDateTime getEditedAt() {
		return editedAt;
	}

	public void setEditedAt(LocalDateTime editedAt) {
		this.editedAt = editedAt;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean deleted) {
		isDeleted = deleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public Integer getDeletedByUserId() {
		return deletedByUserId;
	}

	public void setDeletedByUserId(Integer deletedByUserId) {
		this.deletedByUserId = deletedByUserId;
	}

	public String getDeletedByUsername() {
		return deletedByUsername;
	}

	public void setDeletedByUsername(String deletedByUsername) {
		this.deletedByUsername = deletedByUsername;
	}
}