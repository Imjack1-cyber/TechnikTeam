package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a single event from the `events` table. It holds the event's core
 * data (name, date, description, status) and also contains transient lists to
 * store related information like skill requirements, assigned attendees, tasks,
 * and chat messages for use in detailed views.
 */
public class Event {
	private int id;
	private String name;
	private LocalDateTime eventDateTime;
	private LocalDateTime endDateTime;
	private String description;
	private String location;
	private String status;
	private int leaderUserId;
	private Integer venueId;
	private Integer preflightTemplateId;
	private String userAttendanceStatus;

	private List<SkillRequirement> skillRequirements;
	private List<User> assignedAttendees;
	private List<EventTask> eventTasks;
	private List<EventChatMessage> chatMessages;
	private List<Attachment> attachments;
	private List<StorageItem> reservedItems;
	private String leaderUsername;
	private List<EventCustomField> customFields;
	private List<ChecklistItem> checklistItems;

	private boolean isUserQualified;

	public Event() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getEventDateTime() {
		return eventDateTime;
	}

	public void setEventDateTime(LocalDateTime eventDateTime) {
		this.eventDateTime = eventDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(LocalDateTime endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getLeaderUserId() {
		return leaderUserId;
	}

	public void setLeaderUserId(int leaderUserId) {
		this.leaderUserId = leaderUserId;
	}

	public Integer getVenueId() {
		return venueId;
	}

	public void setVenueId(Integer venueId) {
		this.venueId = venueId;
	}

	public Integer getPreflightTemplateId() {
		return preflightTemplateId;
	}

	public void setPreflightTemplateId(Integer preflightTemplateId) {
		this.preflightTemplateId = preflightTemplateId;
	}

	public String getUserAttendanceStatus() {
		return userAttendanceStatus;
	}

	public void setUserAttendanceStatus(String userAttendanceStatus) {
		this.userAttendanceStatus = userAttendanceStatus;
	}

	public List<SkillRequirement> getSkillRequirements() {
		return skillRequirements;
	}

	public void setSkillRequirements(List<SkillRequirement> skillRequirements) {
		this.skillRequirements = skillRequirements;
	}

	public List<User> getAssignedAttendees() {
		return assignedAttendees;
	}

	public void setAssignedAttendees(List<User> assignedAttendees) {
		this.assignedAttendees = assignedAttendees;
	}

	public List<EventTask> getEventTasks() {
		return eventTasks;
	}

	public void setEventTasks(List<EventTask> eventTasks) {
		this.eventTasks = eventTasks;
	}

	public List<EventChatMessage> getChatMessages() {
		return chatMessages;
	}

	public void setChatMessages(List<EventChatMessage> chatMessages) {
		this.chatMessages = chatMessages;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<StorageItem> getReservedItems() {
		return reservedItems;
	}

	public void setReservedItems(List<StorageItem> reservedItems) {
		this.reservedItems = reservedItems;
	}

	public String getLeaderUsername() {
		return leaderUsername;
	}

	public void setLeaderUsername(String leaderUsername) {
		this.leaderUsername = leaderUsername;
	}

	public List<EventCustomField> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(List<EventCustomField> customFields) {
		this.customFields = customFields;
	}

	public List<ChecklistItem> getChecklistItems() {
		return checklistItems;
	}

	public void setChecklistItems(List<ChecklistItem> checklistItems) {
		this.checklistItems = checklistItems;
	}

	public boolean isUserQualified() {
		return isUserQualified;
	}

	public void setUserQualified(boolean isUserQualified) {
		this.isUserQualified = isUserQualified;
	}

	public String getFormattedEventDateTime() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.eventDateTime);
	}

	public String getFormattedEventDateTimeRange() {
		return DateFormatter.formatDateTimeRange(this.eventDateTime, this.endDateTime);
	}
}