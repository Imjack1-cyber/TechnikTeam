package de.technikteam.dao;

import de.technikteam.model.EventChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class EventChatDAO {
	private static final Logger logger = LogManager.getLogger(EventChatDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventChatDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<EventChatMessage> chatMessageRowMapper = (rs, rowNum) -> {
		EventChatMessage message = new EventChatMessage();
		message.setId(rs.getInt("id"));
		message.setEventId(rs.getInt("event_id"));
		message.setUserId(rs.getInt("user_id"));
		message.setUsername(rs.getString("username"));
		message.setMessageText(rs.getString("message_text"));
		message.setEdited(rs.getBoolean("edited"));
		message.setDeleted(rs.getBoolean("is_deleted"));
		message.setDeletedByUserId(rs.getInt("deleted_by_user_id"));
		message.setDeletedByUsername(rs.getString("deleted_by_username"));
		message.setChatColor(rs.getString("chat_color"));
		if (rs.getTimestamp("deleted_at") != null) {
			message.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
		}
		message.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
		return message;
	};

	public EventChatMessage postMessage(EventChatMessage message) {
		String sql = "INSERT INTO event_chat_messages (event_id, user_id, username, message_text) VALUES (?, ?, ?, ?)";
		try {
			KeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, message.getEventId());
				ps.setInt(2, message.getUserId());
				ps.setString(3, message.getUsername());
				ps.setString(4, message.getMessageText());
				return ps;
			}, keyHolder);

			int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
			return getMessageById(newId);
		} catch (Exception e) {
			logger.error("Error posting chat message for event {}", message.getEventId(), e);
			return null;
		}
	}

	public List<EventChatMessage> getMessagesForEvent(int eventId) {
		String sql = "SELECT m.*, u_del.username as deleted_by_username, u_orig.chat_color FROM event_chat_messages m LEFT JOIN users u_del ON m.deleted_by_user_id = u_del.id JOIN users u_orig ON m.user_id = u_orig.id WHERE m.event_id = ? ORDER BY m.sent_at ASC";
		try {
			return jdbcTemplate.query(sql, chatMessageRowMapper, eventId);
		} catch (Exception e) {
			logger.error("Error fetching chat messages for event {}", eventId, e);
			return List.of();
		}
	}

	public EventChatMessage getMessageById(int messageId) {
		String sql = "SELECT m.*, u_del.username as deleted_by_username, u_orig.chat_color FROM event_chat_messages m LEFT JOIN users u_del ON m.deleted_by_user_id = u_del.id JOIN users u_orig ON m.user_id = u_orig.id WHERE m.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, chatMessageRowMapper, messageId);
		} catch (Exception e) {
			logger.error("Error fetching message by ID {}", messageId, e);
			return null;
		}
	}

	public boolean updateMessage(int messageId, int userId, String newText) {
		String sql = "UPDATE event_chat_messages SET message_text = ?, edited = TRUE WHERE id = ? AND user_id = ? AND is_deleted = FALSE";
		try {
			return jdbcTemplate.update(sql, newText, messageId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error updating message ID {}", messageId, e);
			return false;
		}
	}

	public boolean deleteMessage(int messageId, int deletersUserId, boolean isAdmin) {
		String sql = isAdmin
				? "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_by_user_id = ?, deleted_at = NOW() WHERE id = ?"
				: "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_by_user_id = ?, deleted_at = NOW() WHERE id = ? AND user_id = ?";
		try {
			if (isAdmin) {
				return jdbcTemplate.update(sql, deletersUserId, messageId) > 0;
			} else {
				return jdbcTemplate.update(sql, deletersUserId, messageId, deletersUserId) > 0;
			}
		} catch (Exception e) {
			logger.error("Error soft-deleting message ID {}:", messageId, e);
			return false;
		}
	}
}