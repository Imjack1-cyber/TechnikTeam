package de.technikteam.dao;

import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MeetingWaitlistDAO {
	private static final Logger logger = LogManager.getLogger(MeetingWaitlistDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public MeetingWaitlistDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
		User user = new User();
		user.setId(rs.getInt("user_id"));
		user.setUsername(rs.getString("username"));
		return user;
	};

	/**
	 * Add a user to a meeting's waitlist. Uses INSERT IGNORE-like semantics by
	 * handling duplicates.
	 */
	public boolean addToWaitlist(int meetingId, int userId, Integer requestedBy) {
		String sql = "INSERT INTO meeting_waitlist (meeting_id, user_id, requested_by) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE created_at = created_at";
		try {
			jdbcTemplate.update(sql, meetingId, userId, requestedBy);
			return true;
		} catch (Exception e) {
			logger.error("Error adding user {} to waitlist for meeting {}", userId, meetingId, e);
			return false;
		}
	}

	/**
	 * Remove a user from a meeting's waitlist.
	 */
	public boolean removeFromWaitlist(int meetingId, int userId) {
		String sql = "DELETE FROM meeting_waitlist WHERE meeting_id = ? AND user_id = ?";
		try {
			jdbcTemplate.update(sql, meetingId, userId);
			return true;
		} catch (Exception e) {
			logger.error("Error removing user {} from waitlist for meeting {}", userId, meetingId, e);
			return false;
		}
	}

	/**
	 * Get the waitlist users for a meeting ordered by created_at (FIFO).
	 */
	public List<User> getWaitlistForMeeting(int meetingId) {
		String sql = "SELECT u.id AS user_id, u.username, mw.created_at FROM meeting_waitlist mw JOIN users u ON mw.user_id = u.id WHERE mw.meeting_id = ? ORDER BY mw.created_at ASC";
		try {
			return jdbcTemplate.query(sql, userRowMapper, meetingId);
		} catch (Exception e) {
			logger.error("Error fetching waitlist for meeting {}", meetingId, e);
			return List.of();
		}
	}

	/**
	 * Mark promoted_by and promoted_at for a waitlist entry (for auditing).
	 */
	public boolean markPromoted(int meetingId, int userId, int adminUserId) {
		String sql = "UPDATE meeting_waitlist SET promoted_by = ?, promoted_at = ? WHERE meeting_id = ? AND user_id = ?";
		try {
			jdbcTemplate.update(sql, adminUserId, Timestamp.valueOf(java.time.LocalDateTime.now()), meetingId, userId);
			return removeFromWaitlist(meetingId, userId);
		} catch (Exception e) {
			logger.error("Error marking promoted for user {} meeting {}", userId, meetingId, e);
			return false;
		}
	}

	/**
	 * Check if user is already on waitlist for meeting
	 */
	public boolean isUserOnWaitlist(int meetingId, int userId) {
		String sql = "SELECT COUNT(*) FROM meeting_waitlist WHERE meeting_id = ? AND user_id = ?";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, meetingId, userId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking waitlist existence for user {} meeting {}", userId, meetingId, e);
			return false;
		}
	}
}
