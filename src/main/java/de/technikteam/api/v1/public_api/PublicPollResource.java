package de.technikteam.api.v1.public_api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.technikteam.api.v1.dto.PollResponseRequest;
import de.technikteam.dao.AvailabilityDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.AvailabilityPoll;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/polls")
@Tag(name = "Public Polls", description = "Public endpoints for responding to polls.")
public class PublicPollResource {

    private static final Logger logger = LogManager.getLogger(PublicPollResource.class);
    private final AvailabilityDAO availabilityDAO;
    private final AvailabilityService availabilityService;
    private final Gson gson = new Gson();

    @Autowired
    public PublicPollResource(AvailabilityDAO availabilityDAO, AvailabilityService availabilityService) {
        this.availabilityDAO = availabilityDAO;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Get poll details by public UUID")
    public ResponseEntity<ApiResponse> getPollByUuid(@PathVariable String uuid) {
        Optional<AvailabilityPoll> pollOpt = availabilityDAO.findByUuid(uuid);
        if (pollOpt.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "Poll not found.", null), HttpStatus.NOT_FOUND);
        }
        
        AvailabilityPoll poll = pollOpt.get();
        // The options are stored as a JSON string, let's parse them for the frontend
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> optionsMap = gson.fromJson(poll.getOptions(), type);

        // Get responders to check if user has already voted
        List<String> responders = availabilityDAO.findResponsesByPollId(poll.getId()).stream()
                .map(r -> r.getUserId() != null ? r.getUsername() : r.getGuestName())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Map<String, Object> data = Map.of(
            "poll", poll,
            "options", optionsMap,
            "responders", responders
        );

        return ResponseEntity.ok(new ApiResponse(true, "Poll details retrieved.", data));
    }

    @PostMapping("/{uuid}/respond")
    @Operation(summary = "Submit a response to a poll")
    public ResponseEntity<ApiResponse> submitResponse(
            @PathVariable String uuid,
            @RequestBody PollResponseRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        try {
            User user = (securityUser != null) ? securityUser.getUser() : null;
            availabilityService.submitPollResponse(uuid, request, user);
            return ResponseEntity.ok(new ApiResponse(true, "Response submitted successfully.", null));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            logger.error("Error submitting poll response for UUID {}", uuid, e);
            return new ResponseEntity<>(new ApiResponse(false, "An internal error occurred.", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}