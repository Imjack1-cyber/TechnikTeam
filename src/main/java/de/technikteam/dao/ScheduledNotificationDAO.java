package de.technikteam.dao;

import de.technikteam.model.ScheduledNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ScheduledNotificationDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ScheduledNotificationDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void createOrUpdateReminder(String type, int entityId, List<Integer> userIds, LocalDateTime sendAt,
			String title, String description, String url) {
		deleteReminders(type, entityId);

		if (sendAt != null && userIds != null && !userIds.isEmpty()) {
			String sql = "INSERT INTO scheduled_notifications (target_user_id, notification_type, related_entity_id, send_at, title, description, url) VALUES (?, ?, ?, ?, ?, ?, ?)";
			jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
				ps.setInt(1, userId);
				ps.setString(2, type);
				ps.setInt(3, entityId);
				ps.setTimestamp(4, Timestamp.valueOf(sendAt));
				ps.setString(5, title);
				ps.setString(6, description);
				ps.setString(7, url);
			});
		}
	}

	public void deleteReminders(String type, int entityId) {
		String sql = "DELETE FROM scheduled_notifications WHERE notification_type = ? AND related_entity_id = ?";
		jdbcTemplate.update(sql, type, entityId);
	}

	public List<ScheduledNotification> findPendingNotifications() {
		String sql = "SELECT * FROM scheduled_notifications WHERE status = 'PENDING' AND send_at <= NOW()";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			ScheduledNotification n = new ScheduledNotification();
			n.setId(rs.getInt("id"));
			n.setTargetUserId(rs.getInt("target_user_id"));
			n.setTitle(rs.getString("title"));
			n.setDescription(rs.getString("description"));
			n.setUrl(rs.getString("url"));
			return n;
		});
	}

	public void markAsSent(List<Integer> ids) {
		if (ids.isEmpty())
			return;
		String sql = "UPDATE scheduled_notifications SET status = 'SENT' WHERE id IN ("
				+ ids.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")) + ")";
		jdbcTemplate.update(sql);
	}
}