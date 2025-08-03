package de.technikteam.dao;

import de.technikteam.config.Permissions;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public UserDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<User> userRowMapper = (resultSet, rowNum) -> {
		User user = new User();
		user.setId(resultSet.getInt("id"));
		user.setUsername(resultSet.getString("username"));
		user.setRoleId(resultSet.getInt("role_id"));
		user.setChatColor(resultSet.getString("chat_color"));
		user.setPasswordHash(resultSet.getString("password_hash"));
		if (DaoUtils.hasColumn(resultSet, "theme")) {
			user.setTheme(resultSet.getString("theme"));
		}
		if (DaoUtils.hasColumn(resultSet, "profile_picture_path")) {
			user.setProfilePicturePath(resultSet.getString("profile_picture_path"));
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
	};

	public User validateUser(String username, String password) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, this.userRowMapper, username);
			String storedHash = user.getPasswordHash();

			if (storedHash != null && passwordEncoder.matches(password, storedHash)) {
				user.setPermissions(getPermissionsForUser(user.getId()));
				return user;
			}
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("SQL error during user validation for username: {}", username, e);
		}
		return null;
	}

	public User getUserByUsername(String username) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.username = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
			if (user != null) {
				user.setPermissions(getPermissionsForUser(user.getId()));
			}
			return user;
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("SQL error fetching user by username: {}", username, e);
			return null;
		}
	}

	public Set<String> getPermissionsForUser(int userId) {
		// Check if user is an admin (role_id = 1)
		String checkAdminSql = "SELECT COUNT(*) FROM users WHERE id = ? AND role_id = 1";
		Integer adminCount = 0;
		try {
			adminCount = jdbcTemplate.queryForObject(checkAdminSql, Integer.class, userId);
		} catch (Exception e) {
			logger.error("Could not check admin status for user ID: {}", userId, e);
			return new HashSet<>(); // Return empty set on error
		}

		if (adminCount != null && adminCount > 0) {
			// User is an admin, grant all permissions
			logger.debug("User ID {} is an admin. Granting all permissions.", userId);
			String allPermissionsSql = "SELECT permission_key FROM permissions";
			try {
				return new HashSet<>(jdbcTemplate.queryForList(allPermissionsSql, String.class));
			} catch (Exception e) {
				logger.error("Could not fetch all permissions for admin user ID: {}", userId, e);
				return new HashSet<>();
			}
		} else {
			// User is not an admin, grant only their specific permissions
			String userPermissionsSql = "SELECT p.permission_key FROM permissions p JOIN user_permissions up ON p.id = up.permission_id WHERE up.user_id = ?";
			try {
				List<String> permissionsList = jdbcTemplate.queryForList(userPermissionsSql, String.class, userId);
				return new HashSet<>(permissionsList);
			} catch (Exception e) {
				logger.error("Could not fetch permissions for user ID: {}", userId, e);
				return new HashSet<>();
			}
		}
	}

	public boolean updateUserPermissions(int userId, String[] permissionIds) {
		try {
			jdbcTemplate.update("DELETE FROM user_permissions WHERE user_id = ?", userId);
			if (permissionIds != null && permissionIds.length > 0) {
				String insertSql = "INSERT INTO user_permissions (user_id, permission_id) VALUES (?, ?)";
				jdbcTemplate.batchUpdate(insertSql, List.of(permissionIds), 100, (ps, permId) -> {
					ps.setInt(1, userId);
					ps.setInt(2, Integer.parseInt(permId));
				});
			}
			return true;
		} catch (Exception e) {
			logger.error("Error updating user permissions for user {}", userId, e);
			return false;
		}
	}

	public int createUser(User user, String password) {
		String hashedPassword = passwordEncoder.encode(password);
		String sql = "INSERT INTO users (username, password_hash, role_id, class_year, class_name, email, theme) VALUES (?, ?, ?, ?, ?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, user.getUsername());
				ps.setString(2, hashedPassword);
				ps.setInt(3, user.getRoleId());
				ps.setInt(4, user.getClassYear());
				ps.setString(5, user.getClassName());
				ps.setString(6, user.getEmail());
				ps.setString(7, "light");
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating user {}", user.getUsername(), e);
			return 0;
		}
	}

	public boolean updateUser(User user) {
		String sql = "UPDATE users SET username = ?, role_id = ?, class_year = ?, class_name = ?, email = ?, profile_picture_path = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, user.getUsername(), user.getRoleId(), user.getClassYear(),
					user.getClassName(), user.getEmail(), user.getProfilePicturePath(), user.getId()) > 0;
		} catch (Exception e) {
			logger.error("SQL error updating user with ID: {}", user.getId(), e);
			return false;
		}
	}

	public boolean updateUserTheme(int userId, String theme) {
		String sql = "UPDATE users SET theme = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, theme, userId) > 0;
		} catch (Exception e) {
			logger.error("Error updating theme for user ID {}", userId, e);
			return false;
		}
	}

	public boolean updateUserChatColor(int userId, String chatColor) {
		String sql = "UPDATE users SET chat_color = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, chatColor, userId) > 0;
		} catch (Exception e) {
			logger.error("Error updating chat color for user ID {}", userId, e);
			return false;
		}
	}

	public boolean deleteUser(int userId) {
		String sql = "DELETE FROM users WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, userId) > 0;
		} catch (Exception e) {
			logger.error("SQL error deleting user with ID: {}", userId, e);
			return false;
		}
	}

	public boolean changePassword(int userId, String newPassword) {
		String hashedPassword = passwordEncoder.encode(newPassword);
		String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, hashedPassword, userId) > 0;
		} catch (Exception e) {
			logger.error("SQL error changing password for user ID: {}", userId, e);
			return false;
		}
	}

	public List<User> getAllUsers() {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.username";
		try {
			return jdbcTemplate.query(sql, userRowMapper);
		} catch (Exception e) {
			logger.error("SQL error fetching all users", e);
			return List.of();
		}
	}

	public User getUserById(int userId) {
		String sql = "SELECT u.*, r.role_name FROM users u LEFT JOIN roles r ON u.role_id = r.id WHERE u.id = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
			if (user != null) {
				user.setPermissions(getPermissionsForUser(userId));
			}
			return user;
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("SQL error fetching user by ID with permissions: {}", userId, e);
			return null;
		}
	}
}