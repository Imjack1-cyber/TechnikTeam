package de.technikteam.service;

import de.technikteam.api.v1.dto.PollResponseRequest;
import de.technikteam.dao.PollDAO;
import de.technikteam.model.Poll;
import de.technikteam.model.PollDayVote;
import de.technikteam.model.PollVote;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PollService {

    private final PollDAO pollDAO;
    private final NotificationService notificationService;

    @Autowired
    public PollService(PollDAO pollDAO, NotificationService notificationService) {
        this.pollDAO = pollDAO;
        this.notificationService = notificationService;
    }

    @Transactional
    public Poll createPoll(Poll poll, List<String> optionTexts, User creator) {
        poll.setCreatedByUserId(creator.getId());
        Poll createdPoll = pollDAO.create(poll, optionTexts);
        notificationService.broadcastUIUpdate("POLL", "CREATED", createdPoll);
        return createdPoll;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('POLL_MANAGE')")
    public Poll updatePoll(Poll poll) {
        pollDAO.update(poll);
        // Fetch with a neutral user ID to get the raw poll data for broadcasting
        Poll updatedPoll = pollDAO.findById(poll.getId(), 0).orElse(null);
        notificationService.broadcastUIUpdate("POLL", "UPDATED", updatedPoll);
        return updatedPoll;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('POLL_MANAGE')")
    public void deletePoll(int pollId) {
        if (pollDAO.delete(pollId)) {
            notificationService.broadcastUIUpdate("POLL", "DELETED", Map.of("id", pollId));
        }
    }

    @Transactional
    public PollVote submitResponse(String uuid, User user, PollResponseRequest request) {
        Poll poll = pollDAO.findByUuid(uuid, user != null ? user.getId() : null, request.guestName())
                .orElseThrow(() -> new IllegalArgumentException("Poll not found."));
        
        // Handle verification for guest responses
        if (user == null) {
            String verificationCode = poll.getVerificationCode();
            if (verificationCode != null && !verificationCode.isBlank()) {
                if (!verificationCode.equals(request.verificationCode())) {
                    throw new AccessDeniedException("Invalid verification code.");
                }
            }
        }
        
        return submitResponse(poll, user, request.guestName(), request);
    }
    
    @Transactional
    public PollVote submitResponse(Poll poll, User user, String guestName, Map<String, Object> payload) {
        // This is a legacy method for internal calls, we'll adapt it to use the new DTO structure
        Integer optionId = null;
        if (payload.containsKey("pollOptionId")) { // Check for camelCase
            optionId = ((Number) payload.get("pollOptionId")).intValue();
        } else if (payload.containsKey("poll_option_id")) { // Fallback to snake_case
            optionId = ((Number) payload.get("poll_option_id")).intValue();
        }

        @SuppressWarnings("unchecked")
        List<PollResponseRequest.DayVote> dayVotes = (List<PollResponseRequest.DayVote>) payload.get("dayVotes");
        
        PollResponseRequest request = new PollResponseRequest(
            guestName,
            null, // verification code not available here
            (String) payload.get("status"),
            (String) payload.get("notes"),
            dayVotes,
            optionId,
            (String) payload.get("word")
        );
        return submitResponse(poll, user, guestName, request);
    }
    
    private PollVote submitResponse(Poll poll, User user, String guestName, PollResponseRequest request) {
        if (poll == null || poll.isClosed()) {
            throw new IllegalStateException("Poll is not active or does not exist.");
        }
        
        Integer userId = user != null ? user.getId() : null;
        String finalGuestName = user == null ? request.guestName() : null;

        // Skip duplicate check for word clouds that allow multiple entries
        boolean allowMultiple = "WORD_CLOUD".equals(poll.getType()) && Boolean.TRUE.equals(poll.getOptionsMap().get("allowMultipleEntries"));
        if (!allowMultiple && pollDAO.hasVoted(poll.getId(), userId, finalGuestName)) {
            throw new IllegalStateException("You have already responded to this poll.");
        }
        
        switch(poll.getType()) {
            case "MULTIPLE_CHOICE":
                if (request.pollOptionId() == null) throw new IllegalArgumentException("Option ID is required for this poll type.");
                pollDAO.addOrUpdateVote(poll.getId(), userId, request.pollOptionId());
                break;
            case "WORD_CLOUD":
                if (request.word() == null) throw new IllegalArgumentException("Word is required for this poll type.");
                addWordCloudEntry(poll.getId(), userId, finalGuestName, request.word(), poll.getOptionsMap());
                break;
            case "SCHEDULING":
            case "AVAILABILITY":
                long voteId = pollDAO.addOrUpdateSchedulingVote(poll.getId(), userId, finalGuestName, request.notes());
                if (request.dayVotes() != null) {
                    List<PollDayVote> votesToSave = request.dayVotes().stream().map(dv -> {
                        PollDayVote vote = new PollDayVote();
                        vote.setVoteDate(dv.date());
                        vote.setStatus(dv.status());
                        vote.setNotes(dv.notes());
                        return vote;
                    }).collect(Collectors.toList());
                    pollDAO.saveDayVotes(voteId, votesToSave);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown poll type: " + poll.getType());
        }
        
        notificationService.broadcastUIUpdate("POLL", "UPDATED", pollDAO.findById(poll.getId(), userId != null ? userId : 0).orElse(null));

        return pollDAO.findVote(poll.getId(), userId, finalGuestName).orElse(null);
    }
    
    private void addWordCloudEntry(int pollId, Integer userId, String guestName, String word, Map<String, Object> options) {
        String normalizedWord = word.trim().toLowerCase();
        if (normalizedWord.isEmpty() || normalizedWord.length() > 100) {
            throw new IllegalArgumentException("Invalid word submission.");
        }

        pollDAO.addWordCloudEntry(pollId, userId, guestName, normalizedWord);
    }
}