package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.api.v1.dto.PollResponseRequest;
import de.technikteam.dao.AvailabilityDAO;
import de.technikteam.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private static final Logger logger = LogManager.getLogger(AvailabilityService.class);
    private final AvailabilityDAO availabilityDAO;
    private final Gson gson = new Gson();

    @Autowired
    public AvailabilityService(AvailabilityDAO availabilityDAO) {
        this.availabilityDAO = availabilityDAO;
    }

    public Map<String, Object> analyzePollResults(int pollId) {
        AvailabilityPoll poll = availabilityDAO.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found."));

        List<AvailabilityResponse> responses = availabilityDAO.findResponsesByPollId(pollId);
        List<AvailabilityDayResponse> allDayVotes = availabilityDAO.findDayResponsesByPollId(pollId);
        
        Map<String, Map<String, List<Object>>> analysis = new HashMap<>();

        for (AvailabilityDayResponse vote : allDayVotes) {
            String participantName = responses.stream()
                    .filter(r -> r.getId() == vote.getResponseId())
                    .map(r -> r.getUsername() != null ? r.getUsername() : r.getGuestName())
                    .findFirst().orElse("Unknown");

            String dateKey = vote.getVoteDate().format(DateTimeFormatter.ISO_LOCAL_DATE); // "yyyy-MM-dd"

            analysis.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .computeIfAbsent(vote.getStatus(), k -> new ArrayList<>())
                    .add("MAYBE".equals(vote.getStatus()) ? Map.of("user", participantName, "notes", vote.getNotes()) : participantName);
        }
        
        Type optionsType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> optionsMap = gson.fromJson(poll.getOptions(), optionsType);
        @SuppressWarnings("unchecked")
        List<String> adminAvailableDays = (List<String>) optionsMap.get("availableDays");


        Map<String, Object> resultData = new HashMap<>();
        resultData.put("poll", poll);
        resultData.put("responses", responses);
        resultData.put("analysis", analysis);
        resultData.put("adminAvailableDays", adminAvailableDays);

        return resultData;
    }

    @Transactional
    public void submitPollResponse(String uuid, PollResponseRequest request, User user) {
        AvailabilityPoll poll = availabilityDAO.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found."));

        // Verify code if required for guests
        if (user == null && poll.getVerificationCode() != null && !poll.getVerificationCode().isBlank()) {
            if (!poll.getVerificationCode().equals(request.verificationCode())) {
                throw new AccessDeniedException("Invalid verification code.");
            }
        }
        
        // Check if already responded
        if (availabilityDAO.findResponse(poll.getId(), user != null ? user.getId() : null, request.guestName()).isPresent()) {
            throw new IllegalStateException("You have already responded to this poll.");
        }

        AvailabilityResponse response = new AvailabilityResponse();
        response.setPollId(poll.getId());
        if (user != null) {
            response.setUserId(user.getId());
        } else {
            if (request.guestName() == null || request.guestName().isBlank()) {
                throw new IllegalArgumentException("Guest name is required for non-authenticated users.");
            }
            response.setGuestName(request.guestName());
        }
        response.setStatus(request.status());
        response.setNotes(request.notes());

        AvailabilityResponse savedResponse = availabilityDAO.saveResponse(response);
        
        // This logic now applies to all poll types that use day-based voting
        if (request.dayVotes() != null) {
            List<AvailabilityDayResponse> votesToSave = request.dayVotes().stream().map(dv -> {
                AvailabilityDayResponse vote = new AvailabilityDayResponse();
                vote.setResponseId(savedResponse.getId());
                vote.setVoteDate(dv.date());
                vote.setStatus(dv.status());
                vote.setNotes(dv.notes());
                return vote;
            }).collect(Collectors.toList());
            availabilityDAO.saveDayResponses(savedResponse.getId(), votesToSave);
        }
    }
}