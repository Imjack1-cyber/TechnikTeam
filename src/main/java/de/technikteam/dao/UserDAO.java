package de.technikteam.dao;

import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);

	private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
		User user = new User();
		user.setId(resultSet.getInt("id"));
		user.setUsername(resultSet.getString("username"));
		user.setRoleId(resultSet.getInt("role_id"));
		user.setChatColor(resultSet.getString("chat_color"));
		if (DaoUtils.hasColumn(resultSet, "role_name")) {
			user.setRoleName(resultSet.getString("role_name"));
		}
		if (DaoUtils.hasColumn(resultSet, "created_at") && resultSet.getTimestamp("created_at") != null) {
			user.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
		}
		if (DaoUtils.hasColumn(resultSet, "class_year")) {
			user.setClassYear(resultSet.getInt("class_year"));
		}
		if (DaoUtils.hasColumn(resultSet, "class_name")) {
			user.setClassName(resultSet.getString("class_name"));
		}
		if (DaoUtils.hasColumn(resultSet, "email")) {
			user.setEmail(resultSet.getString("email"));
		}
		return user;
	}

	public User validateUser(String username, String password) {
		String sql = "SELECT u.*, r.role_name FROM users u " + "LEFT JOIN roles r ON u.role_id = r.id "
				+ "WHERE u.username = ? AND u.password_hash = ?";
		logger.debug("Validating user credentials for username: {}", username);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			preparedStatement.setString(2, password);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					logger.info("User validation successful for username: {}", username);
					User user = mapResultSetToUser(resultSet);
					user.setPermissions(getPermissionsForRole(user.getRoleId()));
					return user;
				} else {
					logger.warn("User validation failed for username: {}", username);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error during user validation for username: {}", username, exception);
		}
		return null;
	}

	public Set<String> getPermissionsForRole(int roleId) {
		Set<String> permissions = new HashSet<>();
		String sql = "SELECT p.permission_key FROM permissions p "
				+ "JOIN role_permissions rp ON p.id = rp.permission_id " + "WHERE rp.role_id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, roleId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					permissions.add(resultSet.getString("permission_key"));
				}
			}
		} catch (SQLException exception) {
			logger.error("Could not fetch permissions for role ID: {}", roleId, exception);
		}
		logger.debug("Fetched {} permissions for role ID {}", permissions.size(), roleId);
		return permissions;
	}

	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.username";
		logger.debug("Fetching all users.");
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				users.add(mapResultSetToUser(resultSet));
			}
			logger.info("Fetched {} total users.", users.size());
		} catch (SQLException exception) {
			logger.error("SQL error fetching all users", exception);
		}
		return users;
	}

	public User getUserById(int userId) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
		logger.debug("Fetching user by ID: {}", userId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					logger.info("Found user '{}' with ID: {}", resultSet.getString("username"), userId);
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching user by ID: {}", userId, exception);
		}
		logger.warn("No user found with ID: {}", userId);
		return null;
	}

	public User getUserByUsername(String username) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		logger.debug("Fetching user by username: {}", username);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					logger.info("Found user with username: {}", username);
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching user by username: {}", username, exception);
		}
		logger.warn("No user found with username: {}", username);
		return null;
	}

	public int createUser(User user, String password) {
		String sql = "INSERT INTO users (username, password_hash, role_id, class_year, class_name, email) VALUES (?, ?, ?, ?, ?, ?)";
		logger.debug("Attempting to create user: {}", user.getUsername());
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS)) {

			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setString(2, password);
			preparedStatement.setInt(3, user.getRoleId());
			preparedStatement.setInt(4, user.getClassYear());
			preparedStatement.setString(5, user.getClassName());
			preparedStatement.setString(6, user.getEmail());

			int affectedRows = preparedStatement.executeUpdate();

			if (affectedRows > 0) {
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newUserId = generatedKeys.getInt(1);
						logger.info("Successfully created user '{}' with ID: {}", user.getUsername(), newUserId);
						return newUserId;
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error creating user '{}'. Username or email might already exist.", user.getUsername(),
					exception);
		}
		return 0;
	}

	public boolean updateUser(User user) {
		logger.debug("Updating user with ID: {}", user.getId());
		String sql = "UPDATE users SET username = ?, role_id = ?, class_year = ?, class_name = ?, email = ? WHERE id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setInt(2, user.getRoleId());
			preparedStatement.setInt(3, user.getClassYear());
			preparedStatement.setString(4, user.getClassName());
			preparedStatement.setString(5, user.getEmail());
			preparedStatement.setInt(6, user.getId());
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error updating user with ID: {}", user.getId(), exception);
			return false;
		}
	}

	public boolean updateUserChatColor(int userId, String chatColor) {
		String sql = "UPDATE users SET chat_color = ? WHERE id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, chatColor);
			preparedStatement.setInt(2, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error updating chat color for user ID {}", userId, exception);
			return false;
		}
	}

	public boolean deleteUser(int userId) {
		String sql = "DELETE FROM users WHERE id = ?";
		logger.warn("Attempting to delete user with ID: {}", userId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error deleting user with ID: {}", userId, exception);
			return false;
		}
	}

	public boolean changePassword(int userId, String newPassword) {
		String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
		logger.debug("Changing password for user ID: {}", userId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, newPassword);
			preparedStatement.setInt(2, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error changing password for user ID: {}", userId, exception);
			return false;
		}
	}
}