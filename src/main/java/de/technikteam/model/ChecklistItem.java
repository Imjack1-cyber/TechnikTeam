package de.technikteam.model;

import java.time.LocalDateTime;

public class ChecklistItem {
	private int id;
	private int eventId;
	private int itemId;
	private String itemName;
	private int quantity;
	private String status;
	private Integer lastUpdatedByUserId;
	private String lastUpdatedByUsername;
	private LocalDateTime lastUpdatedAt;

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

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getLastUpdatedByUserId() {
		return lastUpdatedByUserId;
	}

	public void setLastUpdatedByUserId(Integer lastUpdatedByUserId) {
		this.lastUpdatedByUserId = lastUpdatedByUserId;
	}

	public String getLastUpdatedByUsername() {
		return lastUpdatedByUsername;
	}

	public void setLastUpdatedByUsername(String lastUpdatedByUsername) {
		this.lastUpdatedByUsername = lastUpdatedByUsername;
	}

	public LocalDateTime getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}
}