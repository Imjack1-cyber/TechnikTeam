package de.technikteam.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Achievement {
	private int id;
	private String achievementKey;
	private String name;
	private String description;
	private String iconClass;
	private LocalDateTime earnedAt;

	public String getFormattedEarnedAt() {
		if (earnedAt == null)
			return "";
		return earnedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAchievementKey() {
		return achievementKey;
	}

	public void setAchievementKey(String achievementKey) {
		this.achievementKey = achievementKey;
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

	public String getIconClass() {
		return iconClass;
	}

	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}

	public LocalDateTime getEarnedAt() {
		return earnedAt;
	}

	public void setEarnedAt(LocalDateTime earnedAt) {
		this.earnedAt = earnedAt;
	}
}