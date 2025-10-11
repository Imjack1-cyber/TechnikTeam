package de.technikteam.model;

import java.time.LocalDateTime;
import java.util.List;

public class PollVote {
    private long id; // Now has its own ID
    private int pollId;
    private Integer userId;
    private String guestName;
    private Integer pollOptionId; // Nullable for non-option polls
    private String notes; // New field for general vote notes
    private LocalDateTime votedAt;
    // Transient for scheduling polls
    private List<PollDayVote> dayVotes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Integer getPollOptionId() {
        return pollOptionId;
    }

    public void setPollOptionId(Integer pollOptionId) {
        this.pollOptionId = pollOptionId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }

    public List<PollDayVote> getDayVotes() {
        return dayVotes;
    }

    public void setDayVotes(List<PollDayVote> dayVotes) {
        this.dayVotes = dayVotes;
    }
}