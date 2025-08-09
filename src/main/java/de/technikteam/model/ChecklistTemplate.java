package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

public class ChecklistTemplate {
	private int id;
	private String name;
	private String description;
	private LocalDateTime createdAt;
	private List<ChecklistTemplateItem> items;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<ChecklistTemplateItem> getItems() {
		return items;
	}

	public void setItems(List<ChecklistTemplateItem> items) {
		this.items = items;
	}
}