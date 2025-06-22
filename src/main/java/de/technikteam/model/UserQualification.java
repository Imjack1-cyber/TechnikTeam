package de.technikteam.model;

import java.time.LocalDate;

/**
 * Represents the link between a user and a course they have attended or
 * completed, acting as a record of their qualification or skill. This model
 * corresponds to a record in the `user_qualifications` table.
 */
public class UserQualification {
	private int userId;
	private int courseId;
	private String courseName; // For display, joined from 'courses' table
	private String status; // e.g., "BESUCHT", "ABSOLVIERT"
	private LocalDate completionDate;
	private String remarks;

	// --- Getters and Setters ---

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getCourseId() {
		return courseId;
	}

	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDate getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(LocalDate completionDate) {
		this.completionDate = completionDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}