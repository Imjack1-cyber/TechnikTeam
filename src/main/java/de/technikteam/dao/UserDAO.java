package de.technikteam.dao;

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
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAO.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final JdbcTemplate jdbcTemplate;
	private final UserNotificationDAO userNotificationDAO;

	@Autowired
	public UserDAO(JdbcTemplate jdbcTemplate, UserNotificationDAO userNotificationDAO) {
		this.jdbcTemplate = jdbcTemplate;
		this.userNotificationDAO = userNotificationDAO;
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
		if (DaoUtils.hasColumn(resultSet, "profile_icon_class")) {
			user.setProfileIconClass(resultSet.getString("profile_icon_class"));
		}
		if (DaoUtils.hasColumn(resultSet, "admin_notes")) {
			user.setAdminNotes(resultSet.getString("admin_notes"));
		}
		if (DaoUtils.hasColumn(resultSet, "dashboard_layout")) {
			user.setDashboardLayout(resultSet.getString("dashboard_layout"));
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
		if (DaoUtils.hasColumn(resultSet, "status")) {
			user.setStatus(resultSet.getString("status"));
		}
		if (DaoUtils.hasColumn(resultSet, "suspended_until") && resultSet.getTimestamp("suspended_until") != null) {
			user.setSuspendedUntil(resultSet.getTimestamp("suspended_until").toLocalDateTime());
		}
		if (DaoUtils.hasColumn(resultSet, "suspended_reason")) {
			user.setSuspendedReason(resultSet.getString("suspended_reason"));
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
				user.setUnseenNotificationsCount(userNotificationDAO.getUnseenCount(user.getId()));
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
				user.setUnseenNotificationsCount(userNotificationDAO.getUnseenCount(user.getId()));
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
		String sql = "SELECT p.permission_key FROM permissions p "
				+ "JOIN user_permissions up ON p.id = up.permission_id " + "WHERE up.user_id = ?";
		try {
			List<String> permissionKeys = jdbcTemplate.queryForList(sql, String.class, userId);
			return new HashSet<>(permissionKeys);
		} catch (Exception e) {
			logger.error("Error fetching permissions for user {}", userId, e);
			return Set.of();
		}
	}

	@Transactional
	public boolean updateUserPermissions(int userId, String[] permissionIds) {
		jdbcTemplate.update("DELETE FROM user_permissions WHERE user_id = ?", userId);
		if (permissionIds != null && permissionIds.length > 0) {
			List<Object[]> batchArgs = Arrays.stream(permissionIds)
					.map(idStr -> new Object[] { userId, Integer.parseInt(idStr) }).collect(Collectors.toList());
			jdbcTemplate.batchUpdate("INSERT INTO user_permissions (user_id, permission_id) VALUES (?, ?)", batchArgs);
		}
		return true;
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
				// Treat empty string as NULL to avoid unique constraint violation
				if (user.getEmail() != null && !user.getEmail().isEmpty()) {
					ps.setString(6, user.getEmail());
				} else {
					ps.setNull(6, Types.VARCHAR);
				}
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
		String sql = "UPDATE users SET username = ?, role_id = ?, class_year = ?, class_name = ?, email = ?, profile_icon_class = ?, admin_notes = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, user.getUsername(), user.getRoleId(), user.getClassYear(),
					user.getClassName(), user.getEmail(), user.getProfileIconClass(), user.getAdminNotes(),
					user.getId()) > 0;
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

	public boolean updateDashboardLayout(int userId, String layoutJson) {
		String sql = "UPDATE users SET dashboard_layout = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, layoutJson, userId) > 0;
		} catch (Exception e) {
			logger.error("Error updating dashboard layout for user ID {}", userId, e);
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
				user.setUnseenNotificationsCount(userNotificationDAO.getUnseenCount(user.getId()));
			}
			return user;
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("SQL error fetching user by ID with permissions: {}", userId, e);
			return null;
		}
	}

	public List<Integer> findUserIdsByPermission(String permissionKey) {
		String sql = "SELECT up.user_id FROM user_permissions up JOIN permissions p ON up.permission_id = p.id WHERE p.permission_key = ?";
		try {
			return jdbcTemplate.queryForList(sql, Integer.class, permissionKey);
		} catch (Exception e) {
			logger.error("Error fetching user IDs by permission key '{}'", permissionKey, e);
			return List.of();
		}
	}

	public boolean suspendUser(int userId, LocalDateTime suspendedUntil, String reason) {
		String sql = "UPDATE users SET status = 'SUSPENDED', suspended_until = ?, suspended_reason = ? WHERE id = ?";
		try {
			Timestamp suspendedUntilTimestamp = (suspendedUntil != null) ? Timestamp.valueOf(suspendedUntil) : null;
			int updated = jdbcTemplate.update(sql, suspendedUntilTimestamp, reason, userId);
			return updated > 0;
		} catch (Exception e) {
			logger.error("Error suspending user id {}", userId, e);
			return false;
		}
	}

	public boolean unsuspendUser(int userId) {
		String sql = "UPDATE users SET status = 'ACTIVE', suspended_until = NULL, suspended_reason = NULL WHERE id = ?";
		try {
			int updated = jdbcTemplate.update(sql, userId);
			return updated > 0;
		} catch (Exception e) {
			logger.error("Error unsuspending user id {}", userId, e);
			return false;
		}
	}
}