package de.technikteam.dao;

import de.technikteam.model.ChatConversation;
import de.technikteam.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class ChatDAO {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ChatDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<ChatConversation> getConversationsForUser(int userId) {
		String sql = """
				    SELECT
				        c.id,
				        other_p.user_id as other_participant_id,
				        other_u.username as other_participant_username,
				        (SELECT cm.message_text FROM chat_messages cm WHERE cm.conversation_id = c.id ORDER BY cm.sent_at DESC LIMIT 1) as last_message,
				        (SELECT cm.sent_at FROM chat_messages cm WHERE cm.conversation_id = c.id ORDER BY cm.sent_at DESC LIMIT 1) as last_message_timestamp
				    FROM chat_conversations c
				    JOIN chat_participants p ON c.id = p.conversation_id
				    JOIN chat_participants other_p ON c.id = other_p.conversation_id AND other_p.user_id != ?
				    JOIN users other_u ON other_p.user_id = other_u.id
				    WHERE p.user_id = ?
				    ORDER BY last_message_timestamp DESC;
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			ChatConversation conv = new ChatConversation();
			conv.setId(rs.getInt("id"));
			conv.setOtherParticipantId(rs.getInt("other_participant_id"));
			conv.setOtherParticipantUsername(rs.getString("other_participant_username"));
			conv.setLastMessage(rs.getString("last_message"));
			if (rs.getTimestamp("last_message_timestamp") != null) {
				conv.setLastMessageTimestamp(rs.getTimestamp("last_message_timestamp").toLocalDateTime());
			}
			return conv;
		}, userId, userId);
	}

	public boolean isUserInConversation(int conversationId, int userId) {
		String sql = "SELECT COUNT(*) FROM chat_participants WHERE conversation_id = ? AND user_id = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId, userId);
		return count != null && count > 0;
	}

	public List<ChatMessage> getMessagesForConversation(int conversationId, int limit, int offset) {
		String sql = """
				    SELECT cm.*, u.username as sender_username
				    FROM chat_messages cm
				    JOIN users u ON cm.sender_id = u.id
				    WHERE cm.conversation_id = ?
				    ORDER BY cm.sent_at DESC
				    LIMIT ? OFFSET ?
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			ChatMessage msg = new ChatMessage();
			msg.setId(rs.getLong("id"));
			msg.setConversationId(rs.getInt("conversation_id"));
			msg.setSenderId(rs.getInt("sender_id"));
			msg.setSenderUsername(rs.getString("sender_username"));
			msg.setMessageText(rs.getString("message_text"));
			msg.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
			return msg;
		}, conversationId, limit, offset);
	}

	@Transactional
	public ChatMessage createMessage(ChatMessage message) {
		String sql = "INSERT INTO chat_messages (conversation_id, sender_id, message_text) VALUES (?, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, message.getConversationId());
			ps.setInt(2, message.getSenderId());
			ps.setString(3, message.getMessageText());
			return ps;
		}, keyHolder);
		message.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
		return message;
	}

	@Transactional
	public int findOrCreateConversation(int userId1, int userId2) {
		String findSql = """
				    SELECT cp1.conversation_id FROM chat_participants cp1
				    JOIN chat_participants cp2 ON cp1.conversation_id = cp2.conversation_id
				    WHERE cp1.user_id = ? AND cp2.user_id = ?;
				""";
		try {
			return jdbcTemplate.queryForObject(findSql, Integer.class, userId1, userId2);
		} catch (EmptyResultDataAccessException e) {
			// No conversation found, create one
			String createConvSql = "INSERT INTO chat_conversations () VALUES ()";
			GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(
					connection -> connection.prepareStatement(createConvSql, Statement.RETURN_GENERATED_KEYS),
					keyHolder);
			int newConversationId = Objects.requireNonNull(keyHolder.getKey()).intValue();

			String addParticipantsSql = "INSERT INTO chat_participants (conversation_id, user_id) VALUES (?, ?)";
			jdbcTemplate.update(addParticipantsSql, newConversationId, userId1);
			jdbcTemplate.update(addParticipantsSql, newConversationId, userId2);

			return newConversationId;
		}
	}
}