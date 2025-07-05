package de.technikteam.model;

import java.util.List;

/**
 * Represents a single task from the `event_tasks` table, associated with a
 * specific "running" event. It includes the task description, its status,
 * required personnel, ordering, and linked equipment.
 */
public class EventTask {
	private int id;
	private int eventId;
	private String description;
	private String status;
	private String eventName;

	// New fields for reworked task system
	private int displayOrder;
	private int requiredPersons; // 0 means direct assignment, >0 means a pool

	// Associations
	private List<User> assignedUsers;
	private List<StorageItem> requiredItems;
	private List<InventoryKit> requiredKits;

	// Transient field for display, still useful for simple lists
	private String assignedUsernames;

	public String getAssignedUsernames() {
		if (assignedUsers != null && !assignedUsers.isEmpty()) {
			StringBuilder names = new StringBuilder();
			for (User u : assignedUsers) {
				if (names.length() > 0) {
					names.append(", ");
				}
				names.append(u.getUsername());
			}
			return names.toString();
		}
		return "Niemand";
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public void setAssignedUsernames(String assignedUsernames) {
		this.assignedUsernames = assignedUsernames;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}

	public int getRequiredPersons() {
		return requiredPersons;
	}

	public void setRequiredPersons(int requiredPersons) {
		this.requiredPersons = requiredPersons;
	}

	public List<User> getAssignedUsers() {
		return assignedUsers;
	}

	public void setAssignedUsers(List<User> assignedUsers) {
		this.assignedUsers = assignedUsers;
	}

	public List<StorageItem> getRequiredItems() {
		return requiredItems;
	}

	public void setRequiredItems(List<StorageItem> requiredItems) {
		this.requiredItems = requiredItems;
	}

	public List<InventoryKit> getRequiredKits() {
		return requiredKits;
	}

	public void setRequiredKits(List<InventoryKit> requiredKits) {
		this.requiredKits = requiredKits;
	}
}