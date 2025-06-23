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
	private String status;
	private int leaderUserId;
	private String userAttendanceStatus; // Specific to the logged-in user (e.g., ANGEMELDET, ZUGEWIESEN)

	// These fields are populated on-demand for detailed views and are not direct
	// table columns.
	private List<SkillRequirement> skillRequirements;
	private List<User> assignedAttendees;
	private List<EventTask> eventTasks;
	private List<EventChatMessage> chatMessages;
	private String leaderUsername;

	public Event() {
	}

	// --- Getters and Setters ---

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

	public String getLeaderUsername() {
		return leaderUsername;
	}

	public void setLeaderUsername(String leaderUsername) {
		this.leaderUsername = leaderUsername;
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

	// --- Formatted Helpers ---

	public String getFormattedEventDateTime() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.eventDateTime);
	}

	public String getFormattedEventDateTimeRange() {
		return DateFormatter.formatDateTimeRange(this.eventDateTime, this.endDateTime);
	}
}