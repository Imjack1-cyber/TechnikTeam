package de.technikteam.model;

/**
 * Represents the attendance record of a single user for a single event.
 * Includes user details for display purposes.
 */
public class EventAttendance {
	private int eventId;
	private int userId;
	private String username; // To display the user's name
	private String signupStatus; // ANGEMELDET, ABGEMELDET
	private String commitmentStatus; // BESTÄTIGT, OFFEN

	public EventAttendance() {
	}

	// --- Getters and Setters ---

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