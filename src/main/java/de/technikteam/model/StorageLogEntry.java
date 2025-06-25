package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A Data Transfer Object (DTO) to represent a single entry from the storage_log
 * table, enriched with the username of the person who performed the
 * transaction.
 */
public class StorageLogEntry {
	private int id;
	private int itemId;
	private int userId;
	private String username;
	private int quantityChange;
	private String notes;
	private int eventId; // Can be 0 if not linked
	private LocalDateTime transactionTimestamp;

	// Define the formatter here
	private static final DateTimeFormatter GERMAN_LOCALE_FORMATTER = DateTimeFormatter
			.ofPattern("dd.MM.yyyy, HH:mm:ss");

	// --- Getters and Setters ---

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
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

	public int getQuantityChange() {
		return quantityChange;
	}

	public void setQuantityChange(int quantityChange) {
		this.quantityChange = quantityChange;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public LocalDateTime getTransactionTimestamp() {
		return transactionTimestamp;
	}

	public void setTransactionTimestamp(LocalDateTime transactionTimestamp) {
		this.transactionTimestamp = transactionTimestamp;
	}

	// OLD METHOD - uses a different formatter
	public String getFormattedTimestamp() {
		return DateFormatter.formatDateTime(this.transactionTimestamp);
	}

	// NEW GETTER for the specific locale string required by the JSP
	public String getTransactionTimestampLocaleString() {
		if (this.transactionTimestamp == null) {
			return "";
		}
		return this.transactionTimestamp.format(GERMAN_LOCALE_FORMATTER);
	}
}