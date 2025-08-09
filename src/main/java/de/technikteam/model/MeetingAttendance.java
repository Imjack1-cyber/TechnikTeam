package de.technikteam.model;

/**
 * Represents a record from the `meeting_attendance` table, linking a user to a
 * specific meeting. It tracks whether the user attended the meeting and allows
 * for optional remarks (e.g., "excused absence").
 */
public class MeetingAttendance {
	private int userId;
	private int meetingId;
	private boolean attended;
	private String remarks;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(int meetingId) {
		this.meetingId = meetingId;
	}

	public boolean getAttended() {
		return attended;
	}

	public void setAttended(boolean attended) {
		this.attended = attended;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}