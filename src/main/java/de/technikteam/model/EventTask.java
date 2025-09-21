package de.technikteam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventTask {
	private int id;
	private int eventId;
	private Integer categoryId;
	private String name;
	private String description;
	private String status;
	private String eventName;
	private LocalDateTime updatedAt;

	private int displayOrder;
	private int requiredPersons;
	private boolean isImportant;

	private List<User> assignedUsers = new ArrayList<>();
	private List<StorageItem> requiredItems = new ArrayList<>();
	private List<InventoryKit> requiredKits = new ArrayList<>();
	private List<EventTask> dependsOn = new ArrayList<>(); 
	private List<EventTask> dependencyFor = new ArrayList<>(); 
    private List<Attachment> attachments = new ArrayList<>();


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

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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

	public boolean isImportant() {
		return isImportant;
	}

	public void setImportant(boolean important) {
		isImportant = important;
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

	public List<EventTask> getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(List<EventTask> dependsOn) {
		this.dependsOn = dependsOn;
	}

	@JsonIgnore
	public List<EventTask> getDependencyFor() {
		return dependencyFor;
	}

	public void setDependencyFor(List<EventTask> dependencyFor) {
		this.dependencyFor = dependencyFor;
	}

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}