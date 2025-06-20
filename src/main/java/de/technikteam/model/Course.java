package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a course entity. Includes fields for course details and the
 * attendance status of the currently logged-in user.
 */
public class Course {
	private int id;
	private String name;
	private String type;
	private String leader;
	private LocalDateTime courseDateTime;
	private String description;
	private String userAttendanceStatus; // Specific to the logged-in user, e.g., "ANGEMELDET"
	private String abbreviation;

	public Course() {
	}

	// --- Getters and Setters ---

	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public LocalDateTime getCourseDateTime() {
		return courseDateTime;
	}

	public void setCourseDateTime(LocalDateTime courseDateTime) {
		this.courseDateTime = courseDateTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserAttendanceStatus() {
		return userAttendanceStatus;
	}

	public void setUserAttendanceStatus(String userAttendanceStatus) {
		this.userAttendanceStatus = userAttendanceStatus;
	}

	public String getFormattedCourseDateTime() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.courseDateTime);
	}
}