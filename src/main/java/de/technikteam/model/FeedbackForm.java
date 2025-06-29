package de.technikteam.model; 

import java.time.LocalDateTime; 

public class FeedbackForm {
	private int id;
	private int eventId; 
	private String title; 
	private LocalDateTime createdAt; 

	public int getId () {
		return id; 
	}
	
	public void setId(int id) {
		this.id = id; 
	}
	
	public int getEventId() {
		return eventId;
	}
	
	public void setEventId(int eventId) {
		this.eventId = eventId; 
	}
	
	public String getTitle() {
		return title; 
	}
	
	public void setTitle(String title) {
		this.title = title; 
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt; 
	}
	
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}