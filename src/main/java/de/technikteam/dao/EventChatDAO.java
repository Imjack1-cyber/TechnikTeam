package de.technikteam.dao;

import de.technikteam.model.EventChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventChatDAO {
	private static final Logger logger = LogManager.getLogger(EventChatDAO.class);

	public boolean postMessage(EventChatMessage message) {
		String sql = "INSERT INTO event_chat_messages (event_id, user_id, username, message_text) VALUES (?, ?, ?, ?)";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, message.getEventId());
			preparedStatement.setInt(2, message.getUserId());
			preparedStatement.setString(3, message.getUsername());
			preparedStatement.setString(4, message.getMessageText());
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error posting chat message for event {}", message.getEventId(), exception);
			return false;
		}
	}

	public List<EventChatMessage> getMessagesForEvent(int eventId) {
		List<EventChatMessage> messages = new ArrayList<>();
		String sql = "SELECT * FROM event_chat_messages WHERE event_id = ? ORDER BY sent_at ASC";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					EventChatMessage message = new EventChatMessage();
					message.setId(resultSet.getInt("id"));
					message.setEventId(resultSet.getInt("event_id"));
					message.setUserId(resultSet.getInt("user_id"));
					message.setUsername(resultSet.getString("username"));
					message.setMessageText(resultSet.getString("message_text"));
					message.setEdited(resultSet.getBoolean("edited"));
					message.setDeleted(resultSet.getBoolean("is_deleted"));
					message.setDeletedAt(resultSet.getObject("deleted_at", LocalDateTime.class));
					message.setDeletedByUsername(resultSet.getString("deleted_by_username"));
					message.setSentAt(resultSet.getTimestamp("sent_at").toLocalDateTime());
					messages.add(message);
				}
			}
		} catch (SQLException exception) {
			logger.error("Error fetching chat messages for event {}", eventId, exception);
		}
		return messages;
	}

	public boolean updateMessage(int messageId, int userId, String newText) {
		String sql = "UPDATE event_chat_messages SET message_text = ?, edited = TRUE WHERE id = ? AND user_id = ? AND is_deleted = FALSE";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, newText);
			preparedStatement.setInt(2, messageId);
			preparedStatement.setInt(3, userId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error updating message ID {}", messageId, exception);
			return false;
		}
	}

	public boolean deleteMessage(int messageId, int userId, String deleterUsername, boolean isAdmin) {
		String sql;
		if (isAdmin) {
			sql = "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_at = NOW(), deleted_by_username = ? WHERE id = ?";
		} else {
			sql = "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_at = NOW(), deleted_by_username = ? WHERE id = ? AND user_id = ?";
		}

		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, deleterUsername);
			preparedStatement.setInt(2, messageId);
			if (!isAdmin) {
				preparedStatement.setInt(3, userId);
			}
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error soft-deleting message ID {}:", messageId, exception);
			return false;
		}
	}
}