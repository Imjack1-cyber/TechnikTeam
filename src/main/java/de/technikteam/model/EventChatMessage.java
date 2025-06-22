package de.technikteam.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single chat message from the `event_chat_messages` table,
 * associated with a specific "running" event. It holds the message content,
 * sender information, and timestamp.
 */
public class EventChatMessage {
	private int id;
	private int eventId;
	private int userId;
	private String username;
	private String messageText;
	private LocalDateTime sentAt;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	/**
	 * A convenience method to get the sent-at timestamp as a formatted time string
	 * (e.g., "15:30"), suitable for display in the chat UI.
	 * 
	 * @return A formatted time string.
	 */
	public String getFormattedSentAt() {
		return sentAt != null ? sentAt.format(TIME_FORMATTER) : "";
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

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}