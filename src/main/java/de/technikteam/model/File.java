package de.technikteam.model;

import java.time.LocalDateTime;

public class File {
	private int id;
	private String filename;
	private String filepath;
	private String category;
	private LocalDateTime uploadedAt;
	// Optional: Add an upload timestamp if needed.
	// private LocalDateTime uploadedAt;

	// Constructors
	public File() {
	}

	// Getters and Setters

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
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

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}