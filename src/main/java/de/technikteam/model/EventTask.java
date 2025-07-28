package de.technikteam.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single task from the `event_tasks` table, associated with a
 * specific "running" event. It includes the task description, its status,
 * required personnel, ordering, and linked equipment.
 */
public class EventTask {
	private int id;
	private int eventId;
	private String description;
	private String details;
	private String status;
	private String eventName;

	private int displayOrder;
	private int requiredPersons;

	private List<User> assignedUsers;
	private List<StorageItem> requiredItems;
	private List<InventoryKit> requiredKits;

	public String getAssignedUsernames() {
		if (assignedUsers != null && !assignedUsers.isEmpty()) {
			return assignedUsers.stream().map(User::getUsername).collect(Collectors.joining(", "));
		}
		return "Niemand";
	}

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

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
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