package de.technikteam.model;

import java.time.LocalDateTime;

public class Meeting {
	private int id;
	private int courseId;
	private int parentMeetingId; // 0 if none
	private String name;
	private LocalDateTime meetingDateTime;
	private LocalDateTime endDateTime;
	private int leaderUserId;
	private String description;
	private String location;
	private Integer maxParticipants;
	private LocalDateTime signupDeadline;
	private String parentCourseName;
	private String leaderUsername;
	private String userAttendanceStatus;
	private int participantCount; // Transient
	private int waitlistCount; // Transient

	// getters / setters

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

	public int getParentMeetingId() {
		return parentMeetingId;
	}

	public void setParentMeetingId(int parentMeetingId) {
		this.parentMeetingId = parentMeetingId;
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

	public int getLeaderUserId() {
		return leaderUserId;
	}

	public void setLeaderUserId(int leaderUserId) {
		this.leaderUserId = leaderUserId;
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

	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	public LocalDateTime getSignupDeadline() {
		return signupDeadline;
	}

	public void setSignupDeadline(LocalDateTime signupDeadline) {
		this.signupDeadline = signupDeadline;
	}

	public String getParentCourseName() {
		return parentCourseName;
	}

	public void setParentCourseName(String parentCourseName) {
		this.parentCourseName = parentCourseName;
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

	public int getParticipantCount() {
		return participantCount;
	}

	public void setParticipantCount(int participantCount) {
		this.participantCount = participantCount;
	}

	public int getWaitlistCount() {
		return waitlistCount;
	}

	public void setWaitlistCount(int waitlistCount) {
		this.waitlistCount = waitlistCount;
	}
}