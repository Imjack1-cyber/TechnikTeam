package de.technikteam.model;

import java.time.LocalDateTime;

public class DamageReport {
	private int id;
	private int itemId;
	private int reporterUserId;
	private String reportDescription;
	private LocalDateTime reportedAt;
	private String status;
	private Integer reviewedByAdminId;
	private LocalDateTime reviewedAt;
	private String adminNotes;

	private String itemName;
	private String reporterUsername;
	private String reviewedByAdminUsername;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getReporterUserId() {
		return reporterUserId;
	}

	public void setReporterUserId(int reporterUserId) {
		this.reporterUserId = reporterUserId;
	}

	public String getReportDescription() {
		return reportDescription;
	}

	public void setReportDescription(String reportDescription) {
		this.reportDescription = reportDescription;
	}

	public LocalDateTime getReportedAt() {
		return reportedAt;
	}

	public void setReportedAt(LocalDateTime reportedAt) {
		this.reportedAt = reportedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getReviewedByAdminId() {
		return reviewedByAdminId;
	}

	public void setReviewedByAdminId(Integer reviewedByAdminId) {
		this.reviewedByAdminId = reviewedByAdminId;
	}

	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(LocalDateTime reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public String getAdminNotes() {
		return adminNotes;
	}

	public void setAdminNotes(String adminNotes) {
		this.adminNotes = adminNotes;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getReporterUsername() {
		return reporterUsername;
	}

	public void setReporterUsername(String reporterUsername) {
		this.reporterUsername = reporterUsername;
	}

	public String getReviewedByAdminUsername() {
		return reviewedByAdminUsername;
	}

	public void setReviewedByAdminUsername(String reviewedByAdminUsername) {
		this.reviewedByAdminUsername = reviewedByAdminUsername;
	}
}