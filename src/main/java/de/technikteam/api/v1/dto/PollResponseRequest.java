package de.technikteam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PollResponseRequest(
    @Size(max = 255) String guestName,
    String verificationCode,
    String status, // For AVAILABILITY polls: AVAILABLE, UNAVAILABLE, MAYBE
    String notes,
    List<DayVote> dayVotes, // For SCHEDULING polls
    Integer pollOptionId, // For MULTIPLE_CHOICE polls
    @Size(max = 100) String word // For WORD_CLOUD polls
) {
    public record DayVote(
            @NotNull LocalDate date,
            @NotBlank String status,
            @Size(max = 255) String notes
    ) {}
}