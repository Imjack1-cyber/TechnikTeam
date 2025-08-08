package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents the metadata for a single uploaded file from the `files` table. It
 * includes the file's name, its path on the server, its associated category,
 * and the required role ("NUTZER" or "ADMIN") needed to view or download it.
 */
public class File {
	private int id;
	private String filename;
	private String filepath;
	private Integer categoryId;
	private String categoryName;
	private LocalDateTime uploadedAt;
	private String requiredRole;
	private boolean needsWarning;
	private String content;

	public String getFormattedUploadedAt() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.uploadedAt);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Integer getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
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

	public boolean isNeedsWarning() {
		return needsWarning;
	}

	public void setNeedsWarning(boolean needsWarning) {
		this.needsWarning = needsWarning;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}