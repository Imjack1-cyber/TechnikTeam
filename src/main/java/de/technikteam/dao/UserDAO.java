package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;

public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);

	private User mapResultSetToUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setId(rs.getInt("id"));
		user.setUsername(rs.getString("username"));
		user.setRoleId(rs.getInt("role_id"));
		if (DaoUtils.hasColumn(rs, "role_name")) {
			user.setRoleName(rs.getString("role_name"));
		}
		if (DaoUtils.hasColumn(rs, "created_at") && rs.getTimestamp("created_at") != null) {
			user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		}
		if (DaoUtils.hasColumn(rs, "class_year")) {
			user.setClassYear(rs.getInt("class_year"));
		}
		if (DaoUtils.hasColumn(rs, "class_name")) {
			user.setClassName(rs.getString("class_name"));
		}
		if (DaoUtils.hasColumn(rs, "email")) {
			user.setEmail(rs.getString("email"));
		}
		return user;
	}

	public User validateUser(String username, String password) {
		String sql = "SELECT u.*, r.role_name FROM users u " + "LEFT JOIN roles r ON u.role_id = r.id "
				+ "WHERE u.username = ? AND u.password_hash = ?"; // In a real app, this should check a hash.
		logger.debug("Validating user credentials for username: {}", username);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("User validation successful for username: {}", username);
					User user = mapResultSetToUser(rs);
					user.setPermissions(getPermissionsForRole(user.getRoleId()));
					return user;
				} else {
					logger.warn("User validation failed for username: {}", username);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error during user validation for username: {}", username, e);
		}
		return null;
	}

	public Set<String> getPermissionsForRole(int roleId) {
		Set<String> permissions = new HashSet<>();
		String sql = "SELECT p.permission_key FROM permissions p "
				+ "JOIN role_permissions rp ON p.id = rp.permission_id " + "WHERE rp.role_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, roleId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					permissions.add(rs.getString("permission_key"));
				}
			}
		} catch (SQLException e) {
			logger.error("Could not fetch permissions for role ID: {}", roleId, e);
		}
		logger.debug("Fetched {} permissions for role ID {}", permissions.size(), roleId);
		return permissions;
	}

	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.username";
		logger.debug("Fetching all users.");
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				users.add(mapResultSetToUser(rs));
			}
			logger.info("Fetched {} total users.", users.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching all users", e);
		}
		return users;
	}

	public User getUserById(int userId) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
		logger.debug("Fetching user by ID: {}", userId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found user '{}' with ID: {}", rs.getString("username"), userId);
					return mapResultSetToUser(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching user by ID: {}", userId, e);
		}
		logger.warn("No user found with ID: {}", userId);
		return null;
	}

	public User getUserByUsername(String username) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		logger.debug("Fetching user by username: {}", username);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found user with username: {}", username);
					return mapResultSetToUser(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching user by username: {}", username, e);
		}
		logger.warn("No user found with username: {}", username);
		return null;
	}

	public int createUser(User user, String password) {
		String sql = "INSERT INTO users (username, password_hash, role_id, class_year, class_name, email) VALUES (?, ?, ?, ?, ?, ?)";
		logger.debug("Attempting to create user: {}", user.getUsername());
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, password);
			pstmt.setInt(3, user.getRoleId());
			pstmt.setInt(4, user.getClassYear());
			pstmt.setString(5, user.getClassName());
			pstmt.setString(6, user.getEmail());

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newUserId = generatedKeys.getInt(1);
						logger.info("Successfully created user '{}' with ID: {}", user.getUsername(), newUserId);
						return newUserId;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating user '{}'. Username or email might already exist.", user.getUsername(), e);
		}
		return 0;
	}

	public boolean updateUser(User user) {
		logger.debug("Updating user with ID: {}", user.getId());
		String sql = "UPDATE users SET username = ?, role_id = ?, class_year = ?, class_name = ?, email = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setInt(2, user.getRoleId());
			pstmt.setInt(3, user.getClassYear());
			pstmt.setString(4, user.getClassName());
			pstmt.setString(5, user.getEmail());
			pstmt.setInt(6, user.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating user with ID: {}", user.getId(), e);
			return false;
		}
	}

	public boolean deleteUser(int userId) {
		String sql = "DELETE FROM users WHERE id = ?";
		logger.warn("Attempting to delete user with ID: {}", userId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting user with ID: {}", userId, e);
			return false;
		}
	}

	public boolean changePassword(int userId, String newPassword) {
		String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
		logger.debug("Changing password for user ID: {}", userId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newPassword); // Should be a hash in production.
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error changing password for user ID: {}", userId, e);
			return false;
		}
	}
}