package de.technikteam.model;

import java.time.LocalDateTime;

// Represents a course entity.
public class Course {
	private int id;
	private String name;
	private String type;
	private String leader; // leitende Person
	private LocalDateTime courseDateTime;
	private String description; // <-- FIX: Added missing field

	// Constructors, Getters, and Setters
	public Course() {
	}

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public LocalDateTime getCourseDateTime() {
		return courseDateTime;
	}

	public void setCourseDateTime(LocalDateTime courseDateTime) {
		this.courseDateTime = courseDateTime;
	}

	// --- FIX: Added missing getters and setters for description ---
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	// --- End of Fix ---
}