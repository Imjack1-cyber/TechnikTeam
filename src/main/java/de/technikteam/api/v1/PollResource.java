package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Poll;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AvailabilityService;
import de.technikteam.service.PollService;
import de.technikteam.dao.PollDAO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/polls")
@Tag(name = "Admin Polls", description = "Endpoints for managing all types of polls.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('POLL_MANAGE')")
public class PollResource {

    private final PollService pollService;
    private final PollDAO pollDAO;
    private final AvailabilityService availabilityService;
    private final Gson gson = new Gson();

    @Autowired
    public PollResource(PollService pollService, PollDAO pollDAO, AvailabilityService availabilityService) {
        this.pollService = pollService;
        this.pollDAO = pollDAO;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @Operation(summary = "Get all polls")
    public ResponseEntity<ApiResponse> getAllPolls(@AuthenticationPrincipal SecurityUser securityUser) {
        List<Poll> polls = pollDAO.findAll(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "Polls retrieved.", polls));
    }


    @PostMapping
    @Operation(summary = "Create a new poll of any type")
    public ResponseEntity<ApiResponse> createPoll(@RequestBody Map<String, Object> payload, @AuthenticationPrincipal SecurityUser securityUser) {
        Poll poll = new Poll();
        poll.setQuestion((String) payload.get("title")); // Use 'title' for question
        poll.setDescription((String) payload.get("description"));
        poll.setType((String) payload.get("type"));

        // Handle date/time fields for scheduling polls
        if (payload.containsKey("startTime") && payload.get("startTime") != null) {
            poll.setStartTime(LocalDateTime.parse((String) payload.get("startTime")));
        }
        if (payload.containsKey("endTime") && payload.get("endTime") != null) {
            poll.setEndTime(LocalDateTime.parse((String) payload.get("endTime")));
        }
        if (payload.containsKey("closesAt") && payload.get("closesAt") != null) {
            poll.setClosesAt(LocalDateTime.parse((String) payload.get("closesAt")));
        }


        // Handle JSON options field for all poll types
        @SuppressWarnings("unchecked")
        Map<String, Object> pollOptions = (Map<String, Object>) payload.get("options");
        poll.setOptions(gson.toJson(pollOptions));

        @SuppressWarnings("unchecked")
        List<String> optionTexts = (List<String>) payload.get("optionTexts");
        
        poll.setVerificationCode((String) payload.get("verificationCode"));

        Poll createdPoll = pollService.createPoll(poll, optionTexts, securityUser.getUser());
        return new ResponseEntity<>(new ApiResponse(true, "Poll created.", createdPoll), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Get results for a specific poll")
    public ResponseEntity<ApiResponse> getPollResults(@PathVariable int id) {
        try {
            Map<String, Object> data = availabilityService.analyzePollResults(id);
            return ResponseEntity.ok(new ApiResponse(true, "Poll results retrieved.", data));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update a poll")
    public ResponseEntity<ApiResponse> updatePoll(@PathVariable int id, @RequestBody Map<String, Object> payload, @AuthenticationPrincipal SecurityUser securityUser) {
        Optional<Poll> existingPollOpt = pollDAO.findById(id, securityUser.getUser().getId());
        if (existingPollOpt.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Poll not found.", null), HttpStatus.NOT_FOUND);
        }
        Poll existingPoll = existingPollOpt.get();
        
        // Manually map fields from the payload to the existing entity
        existingPoll.setQuestion((String) payload.get("question"));
        existingPoll.setDescription((String) payload.get("description"));

        if (payload.containsKey("closesAt") && payload.get("closesAt") != null) {
            existingPoll.setClosesAt(LocalDateTime.parse((String) payload.get("closesAt")));
        } else {
            existingPoll.setClosesAt(null);
        }
        
        if (payload.containsKey("isClosed")) {
             existingPoll.setClosed((Boolean) payload.get("isClosed"));
        }

        // Correctly handle the 'options' object by serializing it to a string
        if (payload.containsKey("options")) {
            existingPoll.setOptions(gson.toJson(payload.get("options")));
        }
        
        Poll savedPoll = pollService.updatePoll(existingPoll);
        return ResponseEntity.ok(new ApiResponse(true, "Poll updated.", savedPoll));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a poll")
    @PreAuthorize("hasAuthority('POLL_MANAGE')")
    public ResponseEntity<ApiResponse> deletePoll(@PathVariable int id) {
        pollService.deletePoll(id);
        return ResponseEntity.ok(new ApiResponse(true, "Poll deleted.", null));
    }
}