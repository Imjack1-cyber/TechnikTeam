package de.technikteam.model;

/**
 * A model that represents a record from the `event_attendance` table. It links
 * a user to an event, capturing their sign-up status (e.g.,
 * ANGEMELDET/ABGEMELDET) and their commitment level (e.g., BESTÄTIGT/OFFEN),
 * which can be updated by an admin.
 */
public class EventAttendance {
	private int eventId;
	private int userId;
	private String username; 
	private String signupStatus; 
	private String commitmentStatus;

	public EventAttendance() {
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
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

	public String getSignupStatus() {
		return signupStatus;
	}

	public void setSignupStatus(String signupStatus) {
		this.signupStatus = signupStatus;
	}

	public String getCommitmentStatus() {
		return commitmentStatus;
	}

	public void setCommitmentStatus(String commitmentStatus) {
		this.commitmentStatus = commitmentStatus;
	}
}