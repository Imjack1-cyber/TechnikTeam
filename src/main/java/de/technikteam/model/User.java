// Pfad: src/main/java/de/technikteam/model/User.java
package de.technikteam.model;

import java.time.LocalDateTime;

public class User {
	private int id;
	private String username;
	private String role;
	private LocalDateTime createdAt;

	public User() {
	}

	public User(int id, String username, String role) {
		this(id, username, role, null);
	}

	public User(int id, String username, String role, LocalDateTime createdAt) {
		this.id = id;
		this.username = username;
		this.role = role;
		this.createdAt = createdAt;
	}

	// Getters and Setters...
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}