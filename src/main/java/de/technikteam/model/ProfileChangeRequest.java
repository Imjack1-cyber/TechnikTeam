package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * Represents a single request from a user to change their profile data.
 * Corresponds to the `profile_change_requests` table.
 */
public class ProfileChangeRequest {
	private int id;
	private int userId;
	private String username; 
	private String requestedChanges; 
	private String status;
	private LocalDateTime requestedAt;
	private Integer reviewedByAdminId;
	private String reviewedByAdminName; 
	private LocalDateTime reviewedAt;

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

	public String getRequestedChanges() {
		return requestedChanges;
	}

	public void setRequestedChanges(String requestedChanges) {
		this.requestedChanges = requestedChanges;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Integer getReviewedByAdminId() {
		return reviewedByAdminId;
	}

	public void setReviewedByAdminId(Integer reviewedByAdminId) {
		this.reviewedByAdminId = reviewedByAdminId;
	}

	public String getReviewedByAdminName() {
		return reviewedByAdminName;
	}

	public void setReviewedByAdminName(String reviewedByAdminName) {
		this.reviewedByAdminName = reviewedByAdminName;
	}

	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(LocalDateTime reviewedAt) {
		this.reviewedAt = reviewedAt;
	}
}