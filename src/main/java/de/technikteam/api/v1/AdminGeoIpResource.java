package de.technikteam.api.v1;

import de.technikteam.dao.GeoIpRuleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.GeoIpRule;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/geoip/rules")
@Tag(name = "Admin GeoIP", description = "Endpoints for managing GeoIP filtering rules.")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGeoIpResource {

    private final GeoIpRuleDAO geoIpRuleDAO;
    private final AdminLogService adminLogService;

    @Autowired
    public AdminGeoIpResource(GeoIpRuleDAO geoIpRuleDAO, AdminLogService adminLogService) {
        this.geoIpRuleDAO = geoIpRuleDAO;
        this.adminLogService = adminLogService;
    }

    @GetMapping
    @Operation(summary = "Get all GeoIP rules")
    public ResponseEntity<ApiResponse> getAllRules() {
        List<GeoIpRule> rules = geoIpRuleDAO.findAllRules();
        return ResponseEntity.ok(new ApiResponse(true, "GeoIP rules retrieved.", rules));
    }

    @PostMapping
    @Operation(summary = "Add or update a GeoIP rule")
    public ResponseEntity<ApiResponse> saveRule(@Valid @RequestBody GeoIpRule rule,
                                                @AuthenticationPrincipal SecurityUser securityUser) {
        if (rule.getCountryCode() == null || rule.getCountryCode().length() != 2) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid country code.", null));
        }
        if (!"ALLOW".equals(rule.getRuleType()) && !"BLOCK".equals(rule.getRuleType())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid rule type. Must be ALLOW or BLOCK.", null));
        }

        geoIpRuleDAO.saveRule(rule);
        adminLogService.log(securityUser.getUsername(), "GEOIP_RULE_SET",
                String.format("Set GeoIP rule for country %s to %s", rule.getCountryCode(), rule.getRuleType()));
        return ResponseEntity.ok(new ApiResponse(true, "GeoIP rule saved.", rule));
    }

    @DeleteMapping("/{countryCode}")
    @Operation(summary = "Delete a GeoIP rule")
    public ResponseEntity<ApiResponse> deleteRule(@PathVariable String countryCode,
                                                  @AuthenticationPrincipal SecurityUser securityUser) {
        if (geoIpRuleDAO.deleteRule(countryCode)) {
            adminLogService.log(securityUser.getUsername(), "GEOIP_RULE_DELETE",
                    String.format("Deleted GeoIP rule for country %s", countryCode));
            return ResponseEntity.ok(new ApiResponse(true, "GeoIP rule deleted.", null));
        }
        return ResponseEntity.notFound().build();
    }
}