// Create this new file
package de.technikteam.model;

import java.time.LocalDateTime;

public class Meeting {
    private int id;
    private int courseId;
    private String name;
    private LocalDateTime meetingDateTime;
    private String leader;
    private String description;
    private String parentCourseName; 
    private String userAttendanceStatus;
    
    // Getters and Setters...
    
    public String getUserAttendanceStatus() {
    	return userAttendanceStatus; 
    }
    
    public void setUserAttendanceStatus(String userAttendanceStatus) {
    	this.userAttendanceStatus = userAttendanceStatus; 
    }
    
    public int getId() {
    	return id; 
    }
    
    public void setId(int id) {
    	this.id = id;
    }
    
    public int getCourseId() {
    	return courseId;
    }
    
    public void setCourseId(int courseId) {
    	this.courseId  =  courseId; 
    }
    
    public String getName() {
    	return name;
    }
    
    public void setName(String name) {
    	this.name  =  name;
    }
    
    public LocalDateTime getMeetingDateTime() {
    	return meetingDateTime;
    }
    
    public void setMeetingDateTime(LocalDateTime meetingDateTime) {
    	this.meetingDateTime  = meetingDateTime;  
    }
    
    public String getLeader() {
    	return leader;
    }
    
    public void setLeader(String leader) {
    	this.leader  = leader; 
    }
    
    public String getDescription() {
    	return description;
    }
    
    public void setDescription(String description) {
    	this.description  =  description;
    }
    
    public String getParentCourseName() {
    	return parentCourseName;
    }
    
    public void setParentCourseName(String parentCourseName) {
    	this.parentCourseName  =  parentCourseName;
    }
}