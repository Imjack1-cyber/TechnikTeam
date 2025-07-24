package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.EventChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EventChatDAO {
	private static final Logger logger = LogManager.getLogger(EventChatDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public EventChatDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public EventChatMessage postMessage(EventChatMessage message) {
		String sql = "INSERT INTO event_chat_messages (event_id, user_id, username, message_text) VALUES (?, ?, ?, ?)";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setInt(1, message.getEventId());
			preparedStatement.setInt(2, message.getUserId());
			preparedStatement.setString(3, message.getUsername());
			preparedStatement.setString(4, message.getMessageText());
			if (preparedStatement.executeUpdate() > 0) {
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newId = generatedKeys.getInt(1);
						return getMessageById(newId, connection);
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("Error posting chat message for event {}", message.getEventId(), exception);
		}
		return null;
	}

	public List<EventChatMessage> getMessagesForEvent(int eventId) {
		List<EventChatMessage> messages = new ArrayList<>();
		String sql = "SELECT m.*, u_del.username as deleted_by_username, u_orig.chat_color FROM event_chat_messages m LEFT JOIN users u_del ON m.deleted_by_user_id = u_del.id JOIN users u_orig ON m.user_id = u_orig.id WHERE m.event_id = ? ORDER BY m.sent_at ASC";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					messages.add(mapRowToMessage(resultSet));
				}
			}
		} catch (SQLException exception) {
			logger.error("Error fetching chat messages for event {}", eventId, exception);
		}
		return messages;
	}

	private EventChatMessage getMessageById(int messageId, Connection connection) throws SQLException {
		String sql = "SELECT m.*, u_del.username as deleted_by_username, u_orig.chat_color FROM event_chat_messages m LEFT JOIN users u_del ON m.deleted_by_user_id = u_del.id JOIN users u_orig ON m.user_id = u_orig.id WHERE m.id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, messageId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return mapRowToMessage(resultSet);
				}
			}
		}
		return null;
	}

	private EventChatMessage mapRowToMessage(ResultSet resultSet) throws SQLException {
		EventChatMessage message = new EventChatMessage();
		message.setId(resultSet.getInt("id"));
		message.setEventId(resultSet.getInt("event_id"));
		message.setUserId(resultSet.getInt("user_id"));
		message.setUsername(resultSet.getString("username"));
		message.setMessageText(resultSet.getString("message_text"));
		message.setEdited(resultSet.getBoolean("edited"));
		message.setDeleted(resultSet.getBoolean("is_deleted"));
		message.setDeletedByUserId(resultSet.getInt("deleted_by_user_id"));
		message.setDeletedByUsername(resultSet.getString("deleted_by_username"));
		message.setChatColor(resultSet.getString("chat_color"));
		if (resultSet.getTimestamp("deleted_at") != null) {
			message.setDeletedAt(resultSet.getTimestamp("deleted_at").toLocalDateTime());
		}
		message.setSentAt(resultSet.getTimestamp("sent_at").toLocalDateTime());
		return message;
	}

	public boolean updateMessage(int messageId, int userId, String newText) {
		String sql = "UPDATE event_chat_messages SET message_text = ?, edited = TRUE WHERE id = ? AND user_id = ? AND is_deleted = FALSE";
		try (Connection connection = dbManager.getConnection();
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

	public boolean deleteMessage(int messageId, int deletersUserId, boolean isAdmin) {
		String sql = isAdmin ? "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_by_user_id = ? WHERE id = ?"
				: "UPDATE event_chat_messages SET is_deleted = TRUE, deleted_by_user_id = ? WHERE id = ? AND user_id = ?";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, deletersUserId);
			preparedStatement.setInt(2, messageId);
			if (!isAdmin) {
				preparedStatement.setInt(3, deletersUserId);
			}
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("Error soft-deleting message ID {}:", messageId, exception);
			return false;
		}
	}
}