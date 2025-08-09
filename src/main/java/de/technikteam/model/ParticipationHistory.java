package de.technikteam.model;

import java.time.LocalDateTime;

/**
 * A Data Transfer Object (DTO) specifically designed for creating reports. It
 * combines data from users and events to create a historical record of
 * participation, showing who participated in which event, when, and with what
 * status. This class does not map to a single database table.
 */
public class ParticipationHistory {
	private String username;
	private String eventName;
	private LocalDateTime eventDate;
	private String status;

	public ParticipationHistory() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public LocalDateTime getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}