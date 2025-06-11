package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;

public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);

	/**
	 * Helper method to check if a ResultSet contains a certain column.
	 */
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Robust mapping method that checks for the existence of each optional column.
	 */
	private User mapResultSetToUser(ResultSet rs) throws SQLException {
		User user = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
		if (hasColumn(rs, "created_at") && rs.getTimestamp("created_at") != null) {
			user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		}
		if (hasColumn(rs, "class_year")) {
			user.setClassYear(rs.getInt("class_year"));
		}
		if (hasColumn(rs, "class_name")) {
			user.setClassName(rs.getString("class_name"));
		}
		return user;
	}

	public User validateUser(String username, String password) {
		String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
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
		String sql = "SELECT * FROM users ORDER BY username";
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
		String sql = "SELECT * FROM users WHERE id = ?";
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

	// Ersetzen Sie die bestehende createUser-Methode

	/**
	 * Creates a new user in the database.
	 * 
	 * @param user     The User object containing the data.
	 * @param password The plain text password.
	 * @return The ID of the newly created user, or 0 if creation failed.
	 */
	public int createUser(User user, String password) {
		String sql = "INSERT INTO users (username, password_hash, role, class_year, class_name) VALUES (?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, password);
			pstmt.setString(3, user.getRole());
			pstmt.setInt(4, user.getClassYear());
			pstmt.setString(5, user.getClassName());

			int affectedRows = pstmt.executeUpdate();

			if (affectedRows > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newUserId = generatedKeys.getInt(1);
						logger.info("Successfully created user '{}' with ID: {}", user.getUsername(), newUserId);
						return newUserId; // Gibt die neue ID zurück
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating user '{}'.", user.getUsername(), e);
		}
		return 0; // Fehlerfall
	}

	// Ersetzen Sie die bestehende updateUser-Methode
	public boolean updateUser(User user) {
		logger.info("Updating user with ID: {}", user.getId());
		// KORREKTUR: SQL-Befehl um die neuen Spalten erweitert
		String sql = "UPDATE users SET username = ?, role = ?, class_year = ?, class_name = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, user.getRole());
			pstmt.setInt(3, user.getClassYear());
			pstmt.setString(4, user.getClassName());
			pstmt.setInt(5, user.getId());
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
			pstmt.setString(1, newPassword);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error changing password for user ID: {}", userId, e);
			return false;
		}
	}
}