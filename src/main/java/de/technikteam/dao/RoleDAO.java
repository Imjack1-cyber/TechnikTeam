package de.technikteam.dao;

import de.technikteam.model.Role; // Assuming a simple Role model
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
	private static final Logger logger = LogManager.getLogger(RoleDAO.class);

	public List<Role> getAllRoles() {
		List<Role> roles = new ArrayList<>();
		String sql = "SELECT id, role_name FROM roles ORDER BY role_name";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				Role role = new Role();
				role.setId(rs.getInt("id"));
				role.setRoleName(rs.getString("role_name"));
				roles.add(role);
			}
		} catch (SQLException e) {
			logger.error("Error fetching all roles", e);
		}
		return roles;
	}
}