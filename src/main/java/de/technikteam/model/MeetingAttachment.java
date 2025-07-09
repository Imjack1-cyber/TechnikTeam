package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a file attachment from the `meeting_attachments` table. It links a
 * file (with its path and name) to a specific meeting and includes a
 * `requiredRole` to control its visibility to different types of users.
 */
public class MeetingAttachment {
	private int id;
	private int meetingId;
	private String filename;
	private String filepath;
	private LocalDateTime uploadedAt;
	private String requiredRole; 

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(int meetingId) {
		this.meetingId = meetingId;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public String getRequiredRole() {
		return requiredRole;
	}

	public void setRequiredRole(String requiredRole) {
		this.requiredRole = requiredRole;
	}
}