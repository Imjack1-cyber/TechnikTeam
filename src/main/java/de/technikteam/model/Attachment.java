package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a file attachment from the unified `attachments` table. It links a
 * file (with its path and name) to a specific parent entity (like an Event or
 * Meeting) and includes a `requiredRole` to control its visibility.
 */
public class Attachment {
	private int id;
	private String parentType; 
	private int parentId;
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

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
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