package de.technikteam.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventChatMessage {
	private int id;
	private int eventId;
	private int userId;
	private String username;
	private String messageText;
	private boolean edited;
	private boolean isDeleted;
	private LocalDateTime deletedAt; // NEW: To store the deletion timestamp
	private String deletedByUsername; // NEW: To store who deleted the message
	private LocalDateTime sentAt;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

	public String getFormattedSentAt() {
		return sentAt != null ? sentAt.format(TIME_FORMATTER) : "";
	}

	// NEW: Formatter for the deletion timestamp
	public String getFormattedDeletedAt() {
		return deletedAt != null ? deletedAt.format(DATE_TIME_FORMATTER) : "";
	}

	// --- Getters and Setters ---

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public String getDeletedByUsername() {
		return deletedByUsername;
	}

	public void setDeletedByUsername(String deletedByUsername) {
		this.deletedByUsername = deletedByUsername;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}