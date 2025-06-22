package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * The core model for an application user, representing a record from the
 * `users` table. It contains the user's ID, username, role ("ADMIN" or
 * "NUTZER"), and other profile information like class year and creation date.
 */
public class User {
	private int id;
	private String username;
	private String role;
	private LocalDateTime createdAt;
	private int classYear;
	private String className;

	public User() {
	}

	public User(int id, String username, String role) {
		this(id, username, role, null, 0, null);
	}

	public User(int id, String username, String role, LocalDateTime createdAt, int classYear, String className) {
		this.id = id;
		this.username = username;
		this.role = role;
		this.createdAt = createdAt;
		this.classYear = classYear;
		this.className = className;
	}

	// --- Getters and Setters ---

	public int getClassYear() {
		return classYear;
	}

	public void setClassYear(int classYear) {
		this.classYear = classYear;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

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

	/**
	 * A convenience method to get the creation timestamp as a formatted string,
	 * suitable for display in the user interface.
	 * 
	 * @return A German-style date-time string (e.g., "10.06.2025 17:45").
	 */
	public String getFormattedCreatedAt() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.createdAt);
	}
}