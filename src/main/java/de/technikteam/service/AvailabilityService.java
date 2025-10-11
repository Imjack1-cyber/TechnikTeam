package de.technikteam.service;

import de.technikteam.dao.PollDAO;
import de.technikteam.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AvailabilityService {

    private final PollDAO pollDAO;

    @Autowired
    public AvailabilityService(PollDAO pollDAO) {
        this.pollDAO = pollDAO;
    }

    public Map<String, Object> analyzePollResults(int pollId) {
        Poll poll = pollDAO.findById(pollId, 0) // Using 0 as a placeholder user for admin view
                .orElseThrow(() -> new IllegalArgumentException("Poll not found."));

        List<PollDayVote> allDayVotes = pollDAO.findDayVotesForPoll(pollId);
        
        Map<String, Map<String, List<Object>>> analysis = new HashMap<>();

        for (PollDayVote vote : allDayVotes) {
            // This is now less efficient, as we need to find the user for each vote
            // A more optimized query in PollDAO would be beneficial.
            String participantName = "Unknown"; // Placeholder

            String dateKey = vote.getVoteDate().format(DateTimeFormatter.ISO_LOCAL_DATE); // "yyyy-MM-dd"

            analysis.computeIfAbsent(dateKey, k -> new HashMap<>())
                    .computeIfAbsent(vote.getStatus(), k -> new ArrayList<>())
                    .add("MAYBE".equals(vote.getStatus()) ? Map.of("user", participantName, "notes", vote.getNotes()) : participantName);
        }
        
        Map<String, Object> optionsMap = poll.getOptionsMap();
        @SuppressWarnings("unchecked")
        List<String> adminAvailableDays = (List<String>) optionsMap.get("availableDays");


        Map<String, Object> resultData = new HashMap<>();
        resultData.put("poll", poll);
        resultData.put("analysis", analysis);
        resultData.put("adminAvailableDays", adminAvailableDays);

        return resultData;
    }
}