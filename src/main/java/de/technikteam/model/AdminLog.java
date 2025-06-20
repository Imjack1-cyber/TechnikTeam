package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a single log entry for an administrative action.
 */
public class AdminLog {
	private int id;
	private String adminUsername;
	private String actionType;
	private String targetEntity;
	private LocalDateTime actionTimestamp;
	private String details;

	public AdminLog() {
	}

	// --- Getters and Setters ---

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

	public String getTargetEntity() {
		return targetEntity;
	}

	public void setTargetEntity(String targetEntity) {
		this.targetEntity = targetEntity;
	}

	public LocalDateTime getActionTimestamp() {
		return actionTimestamp;
	}

	public void setActionTimestamp(LocalDateTime actionTimestamp) {
		this.actionTimestamp = actionTimestamp;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	// Ersetzen Sie die bestehende getFormattedActionTimestamp-Methode

	public String getFormattedActionTimestamp() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.actionTimestamp);
	}
}