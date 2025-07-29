package de.technikteam.dao;

import de.technikteam.model.Permission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

	public Set<Integer> getPermissionIdsForUser(int userId) {
		String sql = "SELECT permission_id FROM user_permissions WHERE user_id = ?";
		try {
			List<Integer> ids = jdbcTemplate.queryForList(sql, Integer.class, userId);
			return new HashSet<>(ids);
		} catch (Exception e) {
			logger.error("Error fetching permission IDs for user {}", userId, e);
			return Set.of();
		}
	}
}