package de.technikteam.dao;

import de.technikteam.model.UserNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserNotificationDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public UserNotificationDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<UserNotification> rowMapper = (rs, rowNum) -> {
		UserNotification n = new UserNotification();
		n.setId(rs.getLong("id"));
		n.setUserId(rs.getInt("user_id"));
		n.setTitle(rs.getString("title"));
		n.setDescription(rs.getString("description"));
		n.setLevel(rs.getString("level"));
		n.setUrl(rs.getString("url"));
		n.setSeen(rs.getBoolean("is_seen"));
		n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
		return n;
	};

	public void create(UserNotification notification) {
		String sql = "INSERT INTO user_notifications (user_id, title, description, level, url) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql, notification.getUserId(), notification.getTitle(), notification.getDescription(),
				notification.getLevel(), notification.getUrl());
	}

	public List<UserNotification> findByUser(int userId) {
		String sql = "SELECT * FROM user_notifications WHERE user_id = ? ORDER BY created_at DESC";
		return jdbcTemplate.query(sql, rowMapper, userId);
	}

	public int getUnseenCount(int userId) {
		String sql = "SELECT COUNT(*) FROM user_notifications WHERE user_id = ? AND is_seen = FALSE";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
		return count != null ? count : 0;
	}

	public boolean markAllAsSeen(int userId) {
		String sql = "UPDATE user_notifications SET is_seen = TRUE WHERE user_id = ? AND is_seen = FALSE";
		return jdbcTemplate.update(sql, userId) > 0;
	}
}