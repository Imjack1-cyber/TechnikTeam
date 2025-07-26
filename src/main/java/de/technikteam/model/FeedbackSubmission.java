package de.technikteam.model;

import de.technikteam.config.DateFormatter;
import java.time.LocalDateTime;

/**
 * Represents a single feedback submission from a user. Corresponds to the
 * `feedback_submissions` table.
 */
public class FeedbackSubmission {
	private int id;
	private int userId;
	private String username; 
	private String subject;
	private String displayTitle; 
	private String content;
	private LocalDateTime submittedAt;
	private String status;
	private int displayOrder;

	public String getFormattedSubmittedAt() {
		return DateFormatter.formatDateTime(this.submittedAt);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDisplayTitle() {
		return displayTitle;
	}

	public void setDisplayTitle(String displayTitle) {
		this.displayTitle = displayTitle;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;
	}
}