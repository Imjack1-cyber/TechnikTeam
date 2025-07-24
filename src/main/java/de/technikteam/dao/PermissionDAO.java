package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class PermissionDAO {
	private static final Logger logger = LogManager.getLogger(PermissionDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public PermissionDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public List<Permission> getAllPermissions() {
		List<Permission> permissions = new ArrayList<>();
		String sql = "SELECT * FROM permissions ORDER BY description";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				Permission p = new Permission();
				p.setId(rs.getInt("id"));
				p.setPermissionKey(rs.getString("permission_key"));
				p.setDescription(rs.getString("description"));
				permissions.add(p);
			}
		} catch (SQLException e) {
			logger.error("Error fetching all permissions", e);
		}
		return permissions;
	}

	public Set<Integer> getPermissionIdsForUser(int userId) {
		Set<Integer> permissionIds = new HashSet<>();
		String sql = "SELECT permission_id FROM user_permissions WHERE user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					permissionIds.add(rs.getInt("permission_id"));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching permission IDs for user {}", userId, e);
		}
		return permissionIds;
	}
}