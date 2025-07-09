package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a single entry from the `maintenance_log` table, tracking the
 * maintenance history of a storage item.
 */
public class MaintenanceLogEntry {
	private int id;
	private int itemId;
	private int userId;
	private LocalDateTime logDate;
	private String action;
	private String notes;
	private double cost;

	private String username;

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

	public LocalDateTime getLogDate() {
		return logDate;
	}

	public void setLogDate(LocalDateTime logDate) {
		this.logDate = logDate;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFormattedLogDate() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.logDate);
	}
}