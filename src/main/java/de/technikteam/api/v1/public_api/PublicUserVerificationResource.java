package de.technikteam.api.v1.public_api;

import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/verify")
@Tag(name = "Public Verification", description = "Endpoints for public user verification.")
public class PublicUserVerificationResource {

    private final UserDAO userDAO;

    @Autowired
    public PublicUserVerificationResource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Verify a user by their verification token")
    public ResponseEntity<ApiResponse> verifyUserByToken(@PathVariable String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Token is required.", null));
        }

        User user = userDAO.findPublicDataByVerificationToken(token);

        if (user != null) {
            // Return only a subset of public-safe data
            Map<String, String> publicUserData = Map.of(
                "username", user.getUsername(),
                "status", user.getStatus(),
                "roleName", user.getRoleName()
            );
            return ResponseEntity.ok(new ApiResponse(true, "User data retrieved.", publicUserData));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "No user found for the provided token.", null));
        }
    }
}