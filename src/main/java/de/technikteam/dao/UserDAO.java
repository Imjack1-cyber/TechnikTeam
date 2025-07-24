package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final DatabaseManager dbManager;

	@Inject
	public UserDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
		User user = new User();
		user.setId(resultSet.getInt("id"));
		user.setUsername(resultSet.getString("username"));
		user.setRoleId(resultSet.getInt("role_id"));
		user.setChatColor(resultSet.getString("chat_color"));
		if (DaoUtils.hasColumn(resultSet, "theme")) {
			user.setTheme(resultSet.getString("theme"));
		}
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
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					String storedHash = resultSet.getString("password_hash");
					if (passwordEncoder.matches(password, storedHash)) {
						User user = mapResultSetToUser(resultSet);
						user.setPermissions(getPermissionsForUser(user.getId()));
						return user;
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error during user validation for username: {}", username, exception);
		}
		return null;
	}

	public User getUserByUsername(String username) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, username);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapResultSetToUser(resultSet);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching user by username: {}", username, exception);
		}
		return null;
	}

	public Set<String> getPermissionsForUser(int userId) {
		Set<String> permissions = new HashSet<>();
		String sql = "SELECT p.permission_key FROM permissions p JOIN user_permissions up ON p.id = up.permission_id WHERE up.user_id = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					permissions.add(resultSet.getString("permission_key"));
				}
			}
		} catch (SQLException exception) {
			logger.error("Could not fetch permissions for user ID: {}", userId, exception);
		}
		return permissions;
	}

	public boolean updateUserPermissions(int userId, String[] permissionIds, Connection conn) throws SQLException {
		String deleteSql = "DELETE FROM user_permissions WHERE user_id = ?";
		try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
			deleteStmt.setInt(1, userId);
			deleteStmt.executeUpdate();
		}
		if (permissionIds != null && permissionIds.length > 0) {
			String insertSql = "INSERT INTO user_permissions (user_id, permission_id) VALUES (?, ?)";
			try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
				for (String permId : permissionIds) {
					insertStmt.setInt(1, userId);
					insertStmt.setInt(2, Integer.parseInt(permId));
					insertStmt.addBatch();
				}
				insertStmt.executeBatch();
			}
		}
		return true;
	}

	public int createUser(User user, String password, Connection connection) throws SQLException {
		String hashedPassword = passwordEncoder.encode(password);
		String sql = "INSERT INTO users (username, password_hash, role_id, class_year, class_name, email, theme) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setString(2, hashedPassword);
			preparedStatement.setInt(3, user.getRoleId());
			preparedStatement.setInt(4, user.getClassYear());
			preparedStatement.setString(5, user.getClassName());
			preparedStatement.setString(6, user.getEmail());
			preparedStatement.setString(7, "light");
			if (preparedStatement.executeUpdate() > 0) {
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						return generatedKeys.getInt(1);
					}
				}
			}
		}
		return 0;
	}

	public boolean updateUser(User user, Connection connection) throws SQLException {
		String sql = "UPDATE users SET username = ?, role_id = ?, class_year = ?, class_name = ?, email = ? WHERE id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, user.getUsername());
			preparedStatement.setInt(2, user.getRoleId());
			preparedStatement.setInt(3, user.getClassYear());
			preparedStatement.setString(4, user.getClassName());
			preparedStatement.setString(5, user.getEmail());
			preparedStatement.setInt(6, user.getId());
			return preparedStatement.executeUpdate() > 0;
		}
	}

	public boolean updateUser(User user) {
		try (Connection connection = dbManager.getConnection()) {
			return updateUser(user, connection);
		} catch (SQLException e) {
			logger.error("SQL error updating user with ID: {}", user.getId(), e);
			return false;
		}
	}

	public boolean updateUserTheme(int userId, String theme) {
		String sql = "UPDATE users SET theme = ? WHERE id = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, theme);
			preparedStatement.setInt(2, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error updating theme for user ID {}", userId, exception);
			return false;
		}
	}

	public boolean updateUserChatColor(int userId, String chatColor) {
		String sql = "UPDATE users SET chat_color = ? WHERE id = ?";
		try (Connection connection = dbManager.getConnection();
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
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error deleting user with ID: {}", userId, exception);
			return false;
		}
	}

	public boolean changePassword(int userId, String newPassword) {
		String hashedPassword = passwordEncoder.encode(newPassword);
		String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, hashedPassword);
			preparedStatement.setInt(2, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error changing password for user ID: {}", userId, exception);
			return false;
		}
	}

	public List<User> getAllUsers() {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.username";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				users.add(mapResultSetToUser(resultSet));
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching all users", exception);
		}
		return users;
	}

	public User getUserById(int userId) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					User user = mapResultSetToUser(resultSet);
					user.setPermissions(getPermissionsForUser(userId));
					return user;
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching user by ID: {}", userId, exception);
		}
		return null;
	}
}