package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;

/**
 * Represents a single audit log entry from the `admin_logs` table. It captures
 * who performed an action, what type of action it was, detailed information
 * about the action, and when it occurred.
 */
public class AdminLog {
	private int id;
	private String adminUsername;
	private String actionType;
	private String details;
	private LocalDateTime actionTimestamp;

	public AdminLog() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdminUsername() {
		return adminUsername;
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public LocalDateTime getActionTimestamp() {
		return actionTimestamp;
	}

	public void setActionTimestamp(LocalDateTime actionTimestamp) {
		this.actionTimestamp = actionTimestamp;
	}

	/**
	 * A convenience method to get the action timestamp as a formatted string,
	 * suitable for display in the user interface.
	 * 
	 * @return A German-style date-time string (e.g., "10.06.2025 17:45").
	 */
	public String getFormattedActionTimestamp() {
		return DateFormatter.formatDateTime(this.actionTimestamp);
	}
}