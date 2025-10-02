package de.technikteam.api.v1.public_api;

import de.technikteam.dao.AvailabilityDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.AvailabilityPoll;
import de.technikteam.model.AvailabilityResponse;
import de.technikteam.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public/availability")
@Tag(name = "Public Availability", description = "Endpoints for user availability.")
public class PublicAvailabilityResource {

    private final AvailabilityDAO availabilityDAO;

    @Autowired
    public PublicAvailabilityResource(AvailabilityDAO availabilityDAO) {
        this.availabilityDAO = availabilityDAO;
    }

    @GetMapping
    @Operation(summary = "Get all polls with the current user's response")
    public ResponseEntity<ApiResponse> getAllPollsWithUserStatus(@AuthenticationPrincipal SecurityUser securityUser) {
        List<AvailabilityPoll> polls = availabilityDAO.findAll();
        for (AvailabilityPoll poll : polls) {
            List<AvailabilityResponse> responses = availabilityDAO.findResponsesByPollId(poll.getId());
            poll.setResponses(responses);
        }
        return ResponseEntity.ok(new ApiResponse(true, "Polls retrieved.", polls));
    }

    @PostMapping("/{pollId}/respond")
    @Operation(summary = "Submit a response for an availability poll")
    public ResponseEntity<ApiResponse> submitResponse(@PathVariable int pollId, @RequestBody AvailabilityResponse response, @AuthenticationPrincipal SecurityUser securityUser) {
        response.setPollId(pollId);
        response.setUserId(securityUser.getUser().getId());
        availabilityDAO.saveResponse(response);
        return ResponseEntity.ok(new ApiResponse(true, "Response saved.", null));
    }
}