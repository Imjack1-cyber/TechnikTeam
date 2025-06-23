package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;

/**
 * Represents a single, schedulable meeting from the `meetings` table. Each
 * meeting is a specific instance of a parent `Course` and has its own date,
 * time, leader, and description. It also holds transient user-specific data for
 * the UI.
 */
public class Meeting {
	private int id;
	private int courseId;
	private String name;
	private LocalDateTime meetingDateTime;
	private LocalDateTime endDateTime;
	private int leaderUserId;
	private String description;

	// Transient fields populated by DAO joins for UI display
	private String parentCourseName;
	private String leaderUsername; // For displaying the leader's name
	private String userAttendanceStatus; // User-specific status (ANGEMELDET, ABGEMELDET, OFFEN)

	// --- Formatted Helpers ---

	public String getFormattedMeetingDateTime() {
		return DateFormatter.formatDateTime(this.meetingDateTime);
	}

	public String getFormattedMeetingDateTimeRange() {
		return DateFormatter.formatDateTimeRange(this.meetingDateTime, this.endDateTime);
	}

	// --- Getters and Setters ---

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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCourseId() {
		return courseId;
	}

	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getMeetingDateTime() {
		return meetingDateTime;
	}

	public void setMeetingDateTime(LocalDateTime meetingDateTime) {
		this.meetingDateTime = meetingDateTime;
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

	public String getParentCourseName() {
		return parentCourseName;
	}

	public void setParentCourseName(String parentCourseName) {
		this.parentCourseName = parentCourseName;
	}
}