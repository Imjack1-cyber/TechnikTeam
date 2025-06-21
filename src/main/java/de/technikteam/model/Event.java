package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a single event. It holds the event's data and also contains lists
 * to store related information like skill requirements and assigned attendees
 * for detailed views.
 */
public class Event {
	private int id;
	private String name;
	private LocalDateTime eventDateTime;
	private String description;
	private String status;
	private String userAttendanceStatus; // Specific to the logged-in user

	// Added for detailed views
	private List<SkillRequirement> skillRequirements;
	private List<User> assignedAttendees;

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

	public String getFormattedEventDateTime() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.eventDateTime);
	}
}