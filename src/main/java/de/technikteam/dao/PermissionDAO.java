package de.technikteam.dao;

import de.technikteam.model.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class PermissionDAO {
	private static final Logger logger = LogManager.getLogger(PermissionDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public PermissionDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Permission> getAllPermissions() {
		String sql = "SELECT * FROM permissions ORDER BY description";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Permission p = new Permission();
				p.setId(rs.getInt("id"));
				p.setPermissionKey(rs.getString("permission_key"));
				p.setDescription(rs.getString("description"));
				return p;
			});
		} catch (Exception e) {
			logger.error("Error fetching all permissions", e);
			return List.of();
		}
	}

	public List<Integer> getDirectPermissionIdsForUser(int userId) {
		String sql = "SELECT permission_id FROM user_permissions WHERE user_id = ?";
		try {
			return jdbcTemplate.queryForList(sql, Integer.class, userId);
		} catch (Exception e) {
			logger.error("Error fetching direct permission IDs for user {}", userId, e);
			return List.of();
		}
	}

	public List<Integer> getPermissionIdsForRole(int roleId) {
		String sql = "SELECT permission_id FROM role_permissions WHERE role_id = ?";
		try {
			return jdbcTemplate.queryForList(sql, Integer.class, roleId);
		} catch (Exception e) {
			logger.error("Error fetching permission IDs for role {}", roleId, e);
			return List.of();
		}
	}

	@Transactional
	public void updateRolePermissions(int roleId, List<Integer> permissionIds) {
		jdbcTemplate.update("DELETE FROM role_permissions WHERE role_id = ?", roleId);
		if (permissionIds != null && !permissionIds.isEmpty()) {
			String sql = "INSERT INTO role_permissions (role_id, permission_id) VALUES (?, ?)";
			jdbcTemplate.batchUpdate(sql, permissionIds, 100, (ps, permissionId) -> {
				ps.setInt(1, roleId);
				ps.setInt(2, permissionId);
			});
		}
	}

	public void grantPermissionToUsers(int permissionId, List<Integer> userIds) {
		if (userIds == null || userIds.isEmpty()) return;
		String sql = "INSERT IGNORE INTO user_permissions (user_id, permission_id) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
			ps.setInt(1, userId);
			ps.setInt(2, permissionId);
		});
	}

	public void revokePermissionFromUsers(int permissionId, List<Integer> userIds) {
		if (userIds == null || userIds.isEmpty()) return;
		String sql = "DELETE FROM user_permissions WHERE user_id = ? AND permission_id = ?";
		jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
			ps.setInt(1, userId);
			ps.setInt(2, permissionId);
		});
	}

	public Integer getPermissionIdByKey(String key) {
		String sql = "SELECT id FROM permissions WHERE permission_key = ?";
		try {
			return jdbcTemplate.queryForObject(sql, Integer.class, key);
		} catch (EmptyResultDataAccessException e) {
			logger.warn("Could not find permission with key: {}", key);
			return null;
		} catch (Exception e) {
			logger.error("Error fetching permission ID for key {}", key, e);
			return null;
		}
	}
}