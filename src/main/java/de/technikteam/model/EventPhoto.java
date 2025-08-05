package de.technikteam.model;

import java.time.LocalDateTime;

public class EventPhoto {
	private int id;
	private int eventId;
	private int fileId;
	private int uploaderUserId;
	private String caption;
	private LocalDateTime uploadedAt;

	// Transient fields from JOINs
	private String filepath;
	private String uploaderUsername;

	// Getters and Setters
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

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public int getUploaderUserId() {
		return uploaderUserId;
	}

	public void setUploaderUserId(int uploaderUserId) {
		this.uploaderUserId = uploaderUserId;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getUploaderUsername() {
		return uploaderUsername;
	}

	public void setUploaderUsername(String uploaderUsername) {
		this.uploaderUsername = uploaderUsername;
	}
}