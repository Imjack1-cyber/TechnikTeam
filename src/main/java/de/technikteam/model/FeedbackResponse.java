package de.technikteam.model; 

import java.time.LocalDateTime; 

public class FeedbackResponse {
	private int id; 
	private int formId; 
	private int userId; 
	private int rating; 
	private String comments; 
	private LocalDateTime submittedAt; 
	private String username; 
	
	public int getId() {
		return id; 
	}
	
	public void setId(int id) {
		this.id = id; 
	}
	
	public int getFormId() {
		return formId; 
	}
	
	public void setFormId(int formId) {
		this.formId = formId; 
	}
	
	public int getUserId() {
		return userId; 
	}
	
	public void setUserId(int userId) {
		this.userId = userId; 
	}
	
	public int getRating() {
		return rating; 
	}
	
	public void setRating(int rating) {
		this.rating = rating; 
	}
	
	public String getComments() {
		return comments; 
	}
	
	public void setComments(String comments) {
		this.comments = comments; 
	}
	
	public LocalDateTime getSubmittedAt() {
		return submittedAt; 
	}
	
	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt; 
	}
	
	public String getUsername() {
		return username; 
	}
	
	public void setUsername(String username) {
		this.username = username; 
	}
}