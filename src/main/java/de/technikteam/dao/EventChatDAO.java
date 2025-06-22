package de.technikteam.dao;

import de.technikteam.model.EventChatMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for handling chat messages specific to a single event. It
 * manages records in the `event_chat_messages` table, allowing users to post
 * messages and retrieve the chat history for a particular event.
 */
public class EventChatDAO {
	private static final Logger logger = LogManager.getLogger(EventChatDAO.class);

	/**
	 * Posts a new message to an event's chat log in the database.
	 * 
	 * @param message The EventChatMessage object to persist.
	 * @return true if the message was successfully inserted, false otherwise.
	 */
	public boolean postMessage(EventChatMessage message) {
		String sql = "INSERT INTO event_chat_messages (event_id, user_id, username, message_text) VALUES (?, ?, ?, ?)";
		logger.debug("Posting chat message for event {}: '{}'", message.getEventId(), message.getMessageText());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, message.getEventId());
			pstmt.setInt(2, message.getUserId());
			pstmt.setString(3, message.getUsername());
			pstmt.setString(4, message.getMessageText());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error posting chat message for event {}", message.getEventId(), e);
			return false;
		}
	}

	/**
	 * Fetches all messages for a specific event, ordered by the time they were
	 * sent.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of EventChatMessage objects, or an empty list if none are
	 *         found.
	 */
	public List<EventChatMessage> getMessagesForEvent(int eventId) {
		List<EventChatMessage> messages = new ArrayList<>();
		String sql = "SELECT * FROM event_chat_messages WHERE event_id = ? ORDER BY sent_at ASC";
		logger.debug("Fetching chat messages for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventChatMessage msg = new EventChatMessage();
					msg.setId(rs.getInt("id"));
					msg.setEventId(rs.getInt("event_id"));
					msg.setUserId(rs.getInt("user_id"));
					msg.setUsername(rs.getString("username"));
					msg.setMessageText(rs.getString("message_text"));
					msg.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
					messages.add(msg);
				}
				logger.info("Found {} chat messages for event ID: {}", messages.size(), eventId);
			}
		} catch (SQLException e) {
			logger.error("Error fetching chat messages for event {}", eventId, e);
		}
		return messages;
	}
}