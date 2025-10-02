package de.technikteam.api.v1;

import de.technikteam.dao.AvailabilityDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.AvailabilityPoll;
import de.technikteam.model.AvailabilityResponse;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/availability")
@Tag(name = "Admin Availability", description = "Endpoints for managing availability polls.")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('AVAILABILITY_MANAGE')")
public class AdminAvailabilityResource {

    private final AvailabilityDAO availabilityDAO;
    private final UserDAO userDAO;
    private final AvailabilityService availabilityService;

    @Autowired
    public AdminAvailabilityResource(AvailabilityDAO availabilityDAO, UserDAO userDAO, AvailabilityService availabilityService) {
        this.availabilityDAO = availabilityDAO;
        this.userDAO = userDAO;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @Operation(summary = "Get all availability polls")
    public ResponseEntity<ApiResponse> getAllPolls() {
        List<AvailabilityPoll> polls = availabilityDAO.findAll();
        return ResponseEntity.ok(new ApiResponse(true, "Polls retrieved.", polls));
    }

    @PostMapping
    @Operation(summary = "Create a new availability poll")
    public ResponseEntity<ApiResponse> createPoll(@RequestBody AvailabilityPoll poll, @AuthenticationPrincipal SecurityUser securityUser) {
        poll.setCreatedByUserId(securityUser.getUser().getId());
        AvailabilityPoll createdPoll = availabilityDAO.create(poll);
        return new ResponseEntity<>(new ApiResponse(true, "Poll created.", createdPoll), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details and responses for a specific poll")
    public ResponseEntity<ApiResponse> getPollDetails(@PathVariable int id) {
        try {
            Map<String, Object> data = availabilityService.analyzePollResults(id);
            return ResponseEntity.ok(new ApiResponse(true, "Poll details retrieved.", data));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage(), null), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an availability poll")
    public ResponseEntity<ApiResponse> deletePoll(@PathVariable int id) {
        if (availabilityDAO.delete(id)) {
            return ResponseEntity.ok(new ApiResponse(true, "Poll deleted.", null));
        }
        return new ResponseEntity<>(new ApiResponse(false, "Poll not found.", null), HttpStatus.NOT_FOUND);
    }
}