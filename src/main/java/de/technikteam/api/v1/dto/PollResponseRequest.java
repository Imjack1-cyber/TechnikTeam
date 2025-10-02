package de.technikteam.api.v1.dto;

import java.time.LocalDate;
import java.util.List;

public record PollResponseRequest(
    String guestName,
    String verificationCode,
    String status, // For AVAILABILITY polls: AVAILABLE, UNAVAILABLE, MAYBE
    String notes,
    List<DayVote> dayVotes // For SCHEDULING polls
) {
    public record DayVote(LocalDate date, String status, String notes) {}
}