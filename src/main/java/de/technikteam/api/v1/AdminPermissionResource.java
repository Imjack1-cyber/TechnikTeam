package de.technikteam.api.v1;

import de.technikteam.dao.PermissionDAO;
import de.technikteam.dao.RoleDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Permission;
import de.technikteam.model.Role;
import de.technikteam.model.User;
import de.technikteam.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@Tag(name = "Admin Permissions", description = "Endpoints for managing roles and permissions.")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('PERMISSION_MANAGE')")
public class AdminPermissionResource {

    private final PermissionDAO permissionDAO;
    private final RoleDAO roleDAO;
    private final UserDAO userDAO;
    private final PermissionService permissionService;

    @Autowired
    public AdminPermissionResource(PermissionDAO permissionDAO, RoleDAO roleDAO, UserDAO userDAO, PermissionService permissionService) {
        this.permissionDAO = permissionDAO;
        this.roleDAO = roleDAO;
        this.userDAO = userDAO;
        this.permissionService = permissionService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get all data for the permissions management page")
    public ResponseEntity<ApiResponse> getPermissionsOverview() {
        List<User> users = userDAO.getAllUsers();
        List<Role> roles = roleDAO.getAllRoles();
        List<Permission> allPermissions = permissionDAO.getAllPermissions();

        Map<String, List<Permission>> groupedPermissions = allPermissions.stream().collect(Collectors.groupingBy(p -> {
            String key = p.getPermissionKey();
            if (key.contains("_")) {
                return key.substring(0, key.indexOf("_"));
            }
            return "SYSTEM";
        }));
        
        Map<Integer, List<Integer>> directUserPermissions = users.stream()
            .collect(Collectors.toMap(User::getId, user -> permissionDAO.getDirectPermissionIdsForUser(user.getId())));

        Map<Integer, List<Integer>> rolePermissions = roles.stream()
            .collect(Collectors.toMap(Role::getId, role -> permissionDAO.getPermissionIdsForRole(role.getId())));


        Map<String, Object> responseData = Map.of(
            "users", users,
            "roles", roles,
            "groupedPermissions", groupedPermissions,
            "directUserPermissions", directUserPermissions,
            "rolePermissions", rolePermissions
        );

        return ResponseEntity.ok(new ApiResponse(true, "Permissions overview retrieved.", responseData));
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "Update permissions for a role")
    public ResponseEntity<ApiResponse> updateRolePermissions(@PathVariable int roleId, @RequestBody List<Integer> permissionIds) {
        permissionService.updateRolePermissions(roleId, permissionIds);
        return ResponseEntity.ok(new ApiResponse(true, "Role permissions updated successfully.", null));
    }

    @PostMapping("/grant")
    @Operation(summary = "Grant a permission to multiple users")
    public ResponseEntity<ApiResponse> grantPermission(@RequestBody Map<String, Object> payload) {
        Integer permissionId = (Integer) payload.get("permissionId");
        @SuppressWarnings("unchecked")
        List<Integer> userIds = (List<Integer>) payload.get("userIds");
        if (permissionId == null || userIds == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "permissionId and userIds are required.", null));
        }
        permissionService.grantPermissionToUsers(permissionId, userIds);
        return ResponseEntity.ok(new ApiResponse(true, "Permission granted.", null));
    }
    
    @PostMapping("/revoke")
    @Operation(summary = "Revoke a permission from multiple users")
    public ResponseEntity<ApiResponse> revokePermission(@RequestBody Map<String, Object> payload) {
        Integer permissionId = (Integer) payload.get("permissionId");
        @SuppressWarnings("unchecked")
        List<Integer> userIds = (List<Integer>) payload.get("userIds");
        if (permissionId == null || userIds == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "permissionId and userIds are required.", null));
        }
        permissionService.revokePermissionFromUsers(permissionId, userIds);
        return ResponseEntity.ok(new ApiResponse(true, "Permission revoked.", null));
    }
}