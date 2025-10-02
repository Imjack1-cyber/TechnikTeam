package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityResponse {
    private int id;
    private int pollId;
    private Integer userId;
    private String guestName;
    private String status; // AVAILABLE, UNAVAILABLE, MAYBE
    private String notes;
    private LocalDateTime updatedAt;

    // Transient
    private String username;
    private List<AvailabilityDayResponse> dayVotes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPollId() {
        return pollId;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<AvailabilityDayResponse> getDayVotes() {
        return dayVotes;
    }

    public void setDayVotes(List<AvailabilityDayResponse> dayVotes) {
        this.dayVotes = dayVotes;
    }
}