package de.technikteam.model;

import java.time.LocalDateTime;

public class Venue {
	private int id;
	private String name;
	private String address;
	private String notes;
	private String mapImagePath;
	private LocalDateTime createdAt;

	// Getters and Setters
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getMapImagePath() {
		return mapImagePath;
	}

	public void setMapImagePath(String mapImagePath) {
		this.mapImagePath = mapImagePath;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}