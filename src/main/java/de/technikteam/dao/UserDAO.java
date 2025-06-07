package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;

public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);

	private User mapResultSetToUser(ResultSet rs) throws SQLException {
		return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"),
				rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
	}

	public User validateUser(String username, String password) {
		String sql = "SELECT id, username, role, created_at FROM users WHERE username = ? AND password_hash = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("User validation successful for username: {}", username);
					return mapResultSetToUser(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error during user validation", e);
		}
		return null;
	}

	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		String sql = "SELECT id, username, role, created_at FROM users ORDER BY username";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				users.add(mapResultSetToUser(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all users", e);
		}
		return users;
	}

	public User getUserById(int userId) {
		String sql = "SELECT id, username, role, created_at FROM users WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToUser(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching user by ID", e);
		}
		return null;
	}

	public boolean createUser(User user, String password) {
		String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, password); // HASH In production!
			pstmt.setString(3, user.getRole());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error creating user '{}'.", user.getUsername(), e);
			return false;
		}
	}

	public boolean updateUser(User user) {
		String sql = "UPDATE users SET username = ?, role = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, user.getRole());
			pstmt.setInt(3, user.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating user with ID: {}", user.getId(), e);
			return false;
		}
	}

	public boolean deleteUser(int userId) {
		String sql = "DELETE FROM users WHERE id = ?";
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
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newPassword); // HASH in production!
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error changing password for user ID: {}", userId, e);
			return false;
		}
	}
}