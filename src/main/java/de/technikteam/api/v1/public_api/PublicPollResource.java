package de.technikteam.api.v1.public_api;

import de.technikteam.api.v1.dto.PollResponseRequest;
import de.technikteam.dao.PollDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Poll;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.PollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public/polls")
@Tag(name = "Public Polls", description = "Endpoints for participating in polls.")
public class PublicPollResource {

    private static final Logger logger = LogManager.getLogger(PublicPollResource.class);
    private final PollService pollService;
    private final PollDAO pollDAO;

    @Autowired
    public PublicPollResource(PollService pollService, PollDAO pollDAO) {
        this.pollService = pollService;
        this.pollDAO = pollDAO;
    }

    @GetMapping
    @Operation(summary = "Get all polls for the current user")
    public ResponseEntity<ApiResponse> getAllPolls(@AuthenticationPrincipal SecurityUser securityUser) {
        List<Poll> polls = pollDAO.findAll(securityUser.getUser().getId());
        return ResponseEntity.ok(new ApiResponse(true, "Polls retrieved.", polls));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single poll by ID")
    public ResponseEntity<ApiResponse> getPollById(@PathVariable int id, @AuthenticationPrincipal SecurityUser securityUser) {
        Optional<Poll> pollOpt = pollDAO.findById(id, securityUser.getUser().getId());
        if (pollOpt.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Poll not found.", null), HttpStatus.NOT_FOUND);
        }
        Poll poll = pollOpt.get();
        return ResponseEntity.ok(new ApiResponse(true, "Poll retrieved.", poll));
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Cast or change a vote in a poll")
    public ResponseEntity<ApiResponse> castVote(@PathVariable int id, @RequestBody Map<String, Object> payload, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            Poll poll = pollDAO.findById(id, securityUser.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Poll not found or you have no access."));
            pollService.submitResponse(poll, securityUser.getUser(), null, payload);
            return ResponseEntity.ok(new ApiResponse(true, "Vote cast successfully.", null));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @GetMapping("/by-uuid/{uuid}")
    @Operation(summary = "Get a poll by its public UUID")
    public ResponseEntity<ApiResponse> getPollByUuid(@PathVariable String uuid, @RequestParam(required = false) String guestName, @AuthenticationPrincipal SecurityUser securityUser) {
        Integer userId = (securityUser != null) ? securityUser.getUser().getId() : null;
        Optional<Poll> pollOpt = pollDAO.findByUuid(uuid, userId, guestName);
        if (pollOpt.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Poll not found.", null), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(new ApiResponse(true, "Poll retrieved.", pollOpt.get()));
    }

    @PostMapping("/by-uuid/{uuid}/vote")
    @Operation(summary = "Cast a guest vote in a poll or a logged-in user vote via public link")
    public ResponseEntity<ApiResponse> submitPublicResponse(@PathVariable String uuid, @Valid @RequestBody PollResponseRequest request, @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            User user = (securityUser != null) ? securityUser.getUser() : null;
            pollService.submitResponse(uuid, user, request);
            return ResponseEntity.ok(new ApiResponse(true, "Vote cast successfully.", null));
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Invalid poll submission for UUID {}: {}", uuid, e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        } catch (AccessDeniedException e) {
            logger.warn("Access denied for poll submission for UUID {}: {}", uuid, e.getMessage());
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.FORBIDDEN);
        }
    }
}