package de.technikteam.service;

import de.technikteam.dao.PermissionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {

    private final PermissionDAO permissionDAO;
    private final NotificationService notificationService;

    @Autowired
    public PermissionService(PermissionDAO permissionDAO, NotificationService notificationService) {
        this.permissionDAO = permissionDAO;
        this.notificationService = notificationService;
    }

    @Transactional
    public void updateRolePermissions(int roleId, List<Integer> permissionIds) {
        permissionDAO.updateRolePermissions(roleId, permissionIds);
        // We could broadcast an update, but a user's permissions only refresh on next login/session fetch.
    }

    @Transactional
    public void grantPermissionToUsers(int permissionId, List<Integer> userIds) {
        permissionDAO.grantPermissionToUsers(permissionId, userIds);
        userIds.forEach(id -> notificationService.broadcastUIUpdate("USER", "UPDATED", id));
    }

    @Transactional
    public void revokePermissionFromUsers(int permissionId, List<Integer> userIds) {
        permissionDAO.revokePermissionFromUsers(permissionId, userIds);
        userIds.forEach(id -> notificationService.broadcastUIUpdate("USER", "UPDATED", id));
    }
}