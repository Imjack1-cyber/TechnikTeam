// Pfad: src/main/java/de/technikteam/model/User.java
package de.technikteam.model;

import java.time.LocalDateTime;

/* 
 *  The core model for an application user, containing their ID, username, role, and other profile information.
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

	// Getters and Setters...
	public int getClassYear() {
		return classYear;
	} // KORRIGIERT

	public void setClassYear(int classYear) {
		this.classYear = classYear;
	} // KORRIGIERT

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

	public String getFormattedCreatedAt() {
		return de.technikteam.config.DateFormatter.formatDateTime(this.createdAt);
	}
}