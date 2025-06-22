package de.technikteam.model;

/**
 * Represents a single task from the `event_tasks` table, associated with a
 * specific "running" event. It includes the task description, its status
 * ("OFFEN" or "ERLEDIGT"), and a transient field for displaying assigned users.
 */
public class EventTask {
	private int id;
	private int eventId;
	private String description;
	private String status; // e.g., "OFFEN" or "ERLEDIGT"
	private String assignedUsernames; // Comma-separated list for display, not a direct table column

	// --- Getters and Setters ---

	public int getId() {
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAssignedUsernames() {
		return assignedUsernames;
	}

	public void setAssignedUsernames(String assignedUsernames) {
		this.assignedUsernames = assignedUsernames;
	}
}