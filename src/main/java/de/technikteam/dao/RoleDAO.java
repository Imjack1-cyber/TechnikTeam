package de.technikteam.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.technikteam.model.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class RoleDAO {
	private static final Logger logger = LogManager.getLogger(RoleDAO.class);
	private final JdbcTemplate jdbcTemplate;
	private final LoadingCache<String, List<Role>> roleCache;
	private static final String ALL_ROLES_KEY = "ALL_ROLES";

	@Autowired
	public RoleDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.roleCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(1)
				.build(key -> fetchAllRolesFromDb());
	}

	public List<Role> getAllRoles() {
		logger.debug("Fetching all roles from cache.");
		return roleCache.get(ALL_ROLES_KEY);
	}

	private List<Role> fetchAllRolesFromDb() {
		logger.info("Cache miss for roles. Fetching all roles from database.");
		String sql = "SELECT id, role_name FROM roles ORDER BY role_name";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Role role = new Role();
				role.setId(rs.getInt("id"));
				role.setRoleName(rs.getString("role_name"));
				return role;
			});
		} catch (Exception e) {
			logger.error("Error fetching all roles from DB", e);
			return List.of();
		}
	}
}