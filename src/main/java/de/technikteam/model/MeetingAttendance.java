// Create this new file
package de.technikteam.model;

public class MeetingAttendance {
    private int userId;
    private int meetingId;
    private boolean attended;
    private String remarks;
    
    // Getters and Setters
    
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