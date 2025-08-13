package de.technikteam.model;

import java.time.LocalDateTime;

public class TrainingRequest {
	private int id;
	private String topic;
	private int requesterUserId;
	private String requesterUsername; 
	private LocalDateTime createdAt;
	private int interestCount; 

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getRequesterUserId() {
		return requesterUserId;
	}

	public void setRequesterUserId(int requesterUserId) {
		this.requesterUserId = requesterUserId;
	}

	public String getRequesterUsername() {
		return requesterUsername;
	}

	public void setRequesterUsername(String requesterUsername) {
		this.requesterUsername = requesterUsername;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getInterestCount() {
		return interestCount;
	}

	public void setInterestCount(int interestCount) {
		this.interestCount = interestCount;
	}
}