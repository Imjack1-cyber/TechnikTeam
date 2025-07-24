package de.technikteam.dao;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import de.technikteam.model.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoleDAO {
	private static final Logger logger = LogManager.getLogger(RoleDAO.class);
	private final DatabaseManager dbManager;
	private final LoadingCache<String, List<Role>> roleCache;
	private static final String ALL_ROLES_KEY = "ALL_ROLES";

	@Inject
	public RoleDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
		this.roleCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).maximumSize(1) // Only one entry: the
																									// list of all roles
				.build(key -> fetchAllRolesFromDb());
	}

	public List<Role> getAllRoles() {
		logger.debug("Fetching all roles from cache.");
		return roleCache.get(ALL_ROLES_KEY);
	}

	private List<Role> fetchAllRolesFromDb() {
		logger.info("Cache miss for roles. Fetching all roles from database.");
		List<Role> roles = new ArrayList<>();
		String sql = "SELECT id, role_name FROM roles ORDER BY role_name";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Role role = new Role();
				role.setId(rs.getInt("id"));
				role.setRoleName(rs.getString("role_name"));
				roles.add(role);
			}
		} catch (SQLException e) {
			logger.error("Error fetching all roles from DB", e);
		}
		return roles;
	}
}