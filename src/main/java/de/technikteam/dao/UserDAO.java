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

/**
 * A core DAO responsible for all user account management, interacting with the
 * `users` table. Its functions include validating user credentials for login,
 * fetching single or all user records, creating, updating, and deleting users,
 * and handling password changes.
 */
public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);

	/**
	 * Helper method to check if a ResultSet contains a certain column,
	 * case-insensitive.
	 * 
	 * @param rs         The ResultSet to check.
	 * @param columnName The name of the column.
	 * @return true if the column exists.
	 * @throws SQLException if a database error occurs.
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
	 * Robustly maps a ResultSet row to a User object, checking for optional columns
	 * before attempting to read them.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated User object.
	 * @throws SQLException if a database error occurs.
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

	/**
	 * Validates user credentials against the database. IMPORTANT: This
	 * implementation uses plaintext passwords for validation, which is insecure and
	 * should be replaced with a password hashing mechanism (e.g., BCrypt) in a
	 * production environment.
	 * 
	 * @param username The user's username.
	 * @param password The user's plaintext password.
	 * @return A User object if validation is successful, null otherwise.
	 */
	public User validateUser(String username, String password) {
		String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
		logger.debug("Validating user credentials for username: {}", username);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, username);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("User validation successful for username: {}", username);
					return mapResultSetToUser(rs);
				} else {
					logger.warn("User validation failed for username: {}", username);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error during user validation for username: {}", username, e);
		}
		return null;
	}

	/**
	 * Fetches all users from the database, sorted by username.
	 * 
	 * @return A list of all User objects.
	 */
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		String sql = "SELECT * FROM users ORDER BY username";
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

	/**
	 * Fetches a single user by their unique ID.
	 * 
	 * @param userId The ID of the user to fetch.
	 * @return A User object, or null if not found.
	 */
	public User getUserById(int userId) {
		String sql = "SELECT * FROM users WHERE id = ?";
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

	/**
	 * Creates a new user in the database.
	 * 
	 * @param user     The User object containing the data to be inserted.
	 * @param password The plain text password (should be hashed in production).
	 * @return The ID of the newly created user, or 0 if creation failed.
	 */
	public int createUser(User user, String password) {
		String sql = "INSERT INTO users (username, password_hash, role, class_year, class_name) VALUES (?, ?, ?, ?, ?)";
		logger.debug("Attempting to create user: {}", user.getUsername());
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, user.getUsername());
			pstmt.setString(2, password); // In a real app, this should be a hash.
			pstmt.setString(3, user.getRole());
			pstmt.setInt(4, user.getClassYear());
			pstmt.setString(5, user.getClassName());

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
			logger.error("SQL error creating user '{}'. Username might already exist.", user.getUsername(), e);
		}
		return 0;
	}

	/**
	 * Updates an existing user's profile information in the database.
	 * 
	 * @param user The User object containing the updated data.
	 * @return true if the update was successful.
	 */
	public boolean updateUser(User user) {
		logger.debug("Updating user with ID: {}", user.getId());
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

	/**
	 * Deletes a user from the database.
	 * 
	 * @param userId The ID of the user to delete.
	 * @return true if the deletion was successful.
	 */
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

	/**
	 * Changes a user's password.
	 * 
	 * @param userId      The ID of the user whose password is to be changed.
	 * @param newPassword The new plaintext password.
	 * @return true if the password was changed successfully.
	 */
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