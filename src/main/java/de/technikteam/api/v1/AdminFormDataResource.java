package de.technikteam.api.v1;

import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Permission;
import de.technikteam.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/form-data")
@Tag(name = "Admin Users", description = "Endpoints for managing users.")
@SecurityRequirement(name = "bearerAuth")
public class AdminFormDataResource {

	private final RoleDAO roleDAO;
	private final PermissionDAO permissionDAO;

	@Autowired
	public AdminFormDataResource(RoleDAO roleDAO, PermissionDAO permissionDAO) {
		this.roleDAO = roleDAO;
		this.permissionDAO = permissionDAO;
	}

	@GetMapping
	@Operation(summary = "Get data for user forms", description = "Retrieves all roles and grouped permissions needed to populate admin forms for creating or editing users.")
	public ResponseEntity<ApiResponse> getFormDataForUserForms() {
		List<Role> roles = roleDAO.getAllRoles();
		List<Permission> allPermissions = permissionDAO.getAllPermissions();

		Map<String, List<Permission>> groupedPermissions = allPermissions.stream().collect(Collectors.groupingBy(p -> {
			String key = p.getPermissionKey();
			if (key.contains("_")) {
				return key.substring(0, key.indexOf("_"));
			}
			return "SYSTEM";
		}));

		Map<String, Object> formData = Map.of("roles", roles, "groupedPermissions", groupedPermissions);

		return ResponseEntity.ok(new ApiResponse(true, "Formulardaten erfolgreich abgerufen.", formData));
	}
}