package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

public class EventDebriefing {
	private int id;
	private int eventId;
	private int authorUserId;
	private LocalDateTime submittedAt;
	private String whatWentWell;
	private String whatToImprove;
	private String equipmentNotes;
	private String standoutCrewMembers; 

	private String eventName;
	private String authorUsername;
	private List<User> standoutCrewDetails; 

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

	public int getAuthorUserId() {
		return authorUserId;
	}

	public void setAuthorUserId(int authorUserId) {
		this.authorUserId = authorUserId;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public String getWhatWentWell() {
		return whatWentWell;
	}

	public void setWhatWentWell(String whatWentWell) {
		this.whatWentWell = whatWentWell;
	}

	public String getWhatToImprove() {
		return whatToImprove;
	}

	public void setWhatToImprove(String whatToImprove) {
		this.whatToImprove = whatToImprove;
	}

	public String getEquipmentNotes() {
		return equipmentNotes;
	}

	public void setEquipmentNotes(String equipmentNotes) {
		this.equipmentNotes = equipmentNotes;
	}

	public String getStandoutCrewMembers() {
		return standoutCrewMembers;
	}

	public void setStandoutCrewMembers(String standoutCrewMembers) {
		this.standoutCrewMembers = standoutCrewMembers;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}

	public void setAuthorUsername(String authorUsername) {
		this.authorUsername = authorUsername;
	}

	public List<User> getStandoutCrewDetails() {
		return standoutCrewDetails;
	}

	public void setStandoutCrewDetails(List<User> standoutCrewDetails) {
		this.standoutCrewDetails = standoutCrewDetails;
	}
}