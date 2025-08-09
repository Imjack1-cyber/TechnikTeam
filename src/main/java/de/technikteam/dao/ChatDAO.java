package de.technikteam.dao;

import de.technikteam.model.ChatMessage;
import de.technikteam.model.ChatConversation;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ChatDAO {

	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	public ChatDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
	}

	private final RowMapper<ChatMessage> chatMessageRowMapper = (rs, rowNum) -> {
		ChatMessage m = new ChatMessage();
		m.setId(rs.getLong("id"));
		m.setConversationId(rs.getInt("conversation_id"));
		m.setSenderId(rs.getInt("sender_id"));
		m.setSenderUsername(rs.getString("sender_username"));
		m.setMessageText(rs.getString("message_text"));
		m.setStatus(rs.getString("status"));
		m.setChatColor(rs.getString("chat_color"));
		m.setEdited(rs.getBoolean("edited"));
		if (rs.getTimestamp("edited_at") != null) {
			m.setEditedAt(rs.getTimestamp("edited_at").toLocalDateTime());
		}
		m.setDeleted(rs.getBoolean("is_deleted"));
		if (rs.getTimestamp("deleted_at") != null) {
			m.setDeletedAt(rs.getTimestamp("deleted_at").toLocalDateTime());
		}
		m.setDeletedByUserId(rs.getObject("deleted_by_user_id", Integer.class));
		m.setDeletedByUsername(rs.getString("deleted_by_username"));
		m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
		return m;
	};

	/**
	 * Soft-delete a message. This method executes in its own transaction so that
	 * the deletion is committed immediately and not rolled back by outer
	 * transactions.
	 *
	 * For non-admin deletions, the method first verifies that the deleter is the
	 * original sender.
	 *
	 * @param messageId      id of the message to delete
	 * @param deletersUserId id of the user requesting the delete
	 * @param isAdmin        whether the deleter has admin privileges for this
	 *                       conversation
	 * @return true if a row was updated (message marked deleted), false otherwise
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean deleteMessage(long messageId, int deletersUserId, boolean isAdmin) {
		User deleter = jdbcTemplate.queryForObject("SELECT id, username FROM users WHERE id = ?", (rs, rowNum) -> {
			User u = new User();
			u.setId(rs.getInt("id"));
			u.setUsername(rs.getString("username"));
			return u;
		}, deletersUserId);
		if (deleter == null)
			return false;

		String condition = " WHERE id = ? AND is_deleted = FALSE";
		if (!isAdmin) {
			condition += " AND sender_id = ?";
		}

		String deletedMessageText = "Diese Nachricht wurde von " + deleter.getUsername() + " gelöscht!";
		String sql = "UPDATE chat_messages SET is_deleted = TRUE, message_text = ?, deleted_by_user_id = ?, deleted_at = NOW()"
				+ condition;

		int updated;
		if (isAdmin) {
			updated = jdbcTemplate.update(sql, deletedMessageText, deletersUserId, messageId);
		} else {
			updated = jdbcTemplate.update(sql, deletedMessageText, deletersUserId, messageId, deletersUserId);
		}
		return updated > 0;
	}

	public List<ChatMessage> getMessagesForConversation(int conversationId, int limit, int offset) {
		String sql = """
				    SELECT cm.*, u.username as sender_username, u.chat_color, u_del.username as deleted_by_username
				    FROM chat_messages cm
				    JOIN users u ON cm.sender_id = u.id
				    LEFT JOIN users u_del ON cm.deleted_by_user_id = u_del.id
				    WHERE cm.conversation_id = ?
				    ORDER BY cm.sent_at DESC
				    LIMIT ? OFFSET ?
				""";
		return jdbcTemplate.query(sql, chatMessageRowMapper, conversationId, limit, offset);
	}

	public ChatMessage getMessageById(long messageId) {
		try {
			String sql = """
					    SELECT cm.*, u.username as sender_username, u.chat_color, u_del.username as deleted_by_username
					    FROM chat_messages cm
					    JOIN users u ON cm.sender_id = u.id
					    LEFT JOIN users u_del ON cm.deleted_by_user_id = u_del.id
					    WHERE cm.id = ?
					""";
			return jdbcTemplate.queryForObject(sql, chatMessageRowMapper, messageId);
		} catch (Exception e) {
			return null;
		}
	}

	public ChatConversation getConversationById(int conversationId) {
		String sql = "SELECT * FROM chat_conversations WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
				ChatConversation conv = new ChatConversation();
				conv.setId(rs.getInt("id"));
				conv.setName(rs.getString("name"));
				conv.setGroupChat(rs.getBoolean("is_group_chat"));
				conv.setCreatorId(rs.getInt("creator_id"));
				conv.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
				conv.setParticipants(getParticipantsForConversation(conversationId));
				return conv;
			}, conversationId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public List<ChatConversation> getConversationsForUser(int userId) {
		String sql = """
				    SELECT
				        c.id, c.name, c.is_group_chat, c.creator_id,
				        other_p.user_id as other_participant_id,
				        other_u.username as other_participant_username,
				        (SELECT cm.message_text FROM chat_messages cm WHERE cm.conversation_id = c.id AND cm.is_deleted = FALSE ORDER BY cm.sent_at DESC LIMIT 1) as last_message,
				        (SELECT cm.sent_at FROM chat_messages cm WHERE cm.conversation_id = c.id ORDER BY cm.sent_at DESC LIMIT 1) as last_message_timestamp
				    FROM chat_conversations c
				    JOIN chat_participants p ON c.id = p.conversation_id
				    LEFT JOIN chat_participants other_p ON c.id = other_p.conversation_id AND other_p.user_id != ? AND c.is_group_chat = false
				    LEFT JOIN users other_u ON other_p.user_id = other_u.id
				    WHERE p.user_id = ?
				    ORDER BY last_message_timestamp DESC;
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			ChatConversation conv = new ChatConversation();
			conv.setId(rs.getInt("id"));
			conv.setName(rs.getString("name"));
			conv.setGroupChat(rs.getBoolean("is_group_chat"));
			conv.setCreatorId(rs.getInt("creator_id"));
			conv.setOtherParticipantId(rs.getInt("other_participant_id"));
			conv.setOtherParticipantUsername(rs.getString("other_participant_username"));
			conv.setLastMessage(rs.getString("last_message"));
			if (rs.getTimestamp("last_message_timestamp") != null) {
				conv.setLastMessageTimestamp(rs.getTimestamp("last_message_timestamp").toLocalDateTime());
			}
			return conv;
		}, userId, userId);
	}

	public List<User> getParticipantsForConversation(int conversationId) {
		String sql = "SELECT u.id, u.username FROM users u JOIN chat_participants cp ON u.id = cp.user_id WHERE cp.conversation_id = ?";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			User user = new User();
			user.setId(rs.getInt("id"));
			user.setUsername(rs.getString("username"));
			return user;
		}, conversationId);
	}

	@Transactional
	public boolean updateMessagesStatusToRead(List<Long> messageIds, int conversationId, int readerId) {
		if (messageIds == null || messageIds.isEmpty()) {
			return false;
		}
		// FIX: Use NamedParameterJdbcTemplate to avoid SQL injection
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", messageIds);
		parameters.addValue("conversationId", conversationId);
		parameters.addValue("readerId", readerId);

		String sql = "UPDATE chat_messages SET status = 'READ' WHERE id IN (:ids) AND conversation_id = :conversationId AND sender_id != :readerId AND status != 'READ'";

		return namedParameterJdbcTemplate.update(sql, parameters) > 0;
	}

	public boolean updateMessage(long messageId, int userId, String newText) {
		String sql = "UPDATE chat_messages SET message_text = ?, edited = TRUE, edited_at = NOW() WHERE id = ? AND sender_id = ? AND is_deleted = FALSE AND sent_at >= NOW() - INTERVAL 24 HOUR";
		return jdbcTemplate.update(sql, newText, messageId, userId) > 0;
	}

	@Transactional
	public ChatMessage createMessage(ChatMessage message) {
		String sql = "INSERT INTO chat_messages (conversation_id, sender_id, message_text, status) VALUES (?, ?, ?, 'SENT')";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, message.getConversationId());
			ps.setInt(2, message.getSenderId());
			ps.setString(3, message.getMessageText());
			return ps;
		}, keyHolder);
		message.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
		message.setStatus("SENT");
		return message;
	}

	@Transactional
	public int findOrCreateConversation(int userId1, int userId2) {
		String findSql = """
				    SELECT cp1.conversation_id FROM chat_participants cp1
				    JOIN chat_participants cp2 ON cp1.conversation_id = cp2.conversation_id
				    JOIN chat_conversations c ON cp1.conversation_id = c.id
				    WHERE cp1.user_id = ? AND cp2.user_id = ? AND c.is_group_chat = false;
				""";
		try {
			return jdbcTemplate.queryForObject(findSql, Integer.class, userId1, userId2);
		} catch (EmptyResultDataAccessException e) {
			String createConvSql = "INSERT INTO chat_conversations (is_group_chat, creator_id) VALUES (false, ?)";
			GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(createConvSql, Statement.RETURN_GENERATED_KEYS);
				ps.setInt(1, userId1);
				return ps;
			}, keyHolder);
			int newConversationId = Objects.requireNonNull(keyHolder.getKey()).intValue();

			String addParticipantsSql = "INSERT INTO chat_participants (conversation_id, user_id) VALUES (?, ?)";
			jdbcTemplate.update(addParticipantsSql, newConversationId, userId1);
			jdbcTemplate.update(addParticipantsSql, newConversationId, userId2);

			return newConversationId;
		}
	}

	@Transactional
	public int createGroupConversation(String name, int creatorId, List<Integer> participantIds) {
		String createConvSql = "INSERT INTO chat_conversations (is_group_chat, name, creator_id) VALUES (true, ?, ?)";
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(createConvSql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, name);
			ps.setInt(2, creatorId);
			return ps;
		}, keyHolder);
		int newConversationId = Objects.requireNonNull(keyHolder.getKey()).intValue();

		String addParticipantsSql = "INSERT INTO chat_participants (conversation_id, user_id) VALUES (?, ?)";
		// Add the creator as well
		if (!participantIds.contains(creatorId)) {
			participantIds.add(creatorId);
		}
		for (Integer participantId : participantIds) {
			jdbcTemplate.update(addParticipantsSql, newConversationId, participantId);
		}

		return newConversationId;
	}

	public boolean isUserInConversation(int conversationId, int userId) {
		String sql = "SELECT COUNT(*) FROM chat_participants WHERE conversation_id = ? AND user_id = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, conversationId, userId);
		return count != null && count > 0;
	}

	@Transactional
	public void addParticipantsToGroup(int conversationId, List<Integer> userIds) {
		String sql = "INSERT IGNORE INTO chat_participants (conversation_id, user_id) VALUES (?, ?)";
		jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
			ps.setInt(1, conversationId);
			ps.setInt(2, userId);
		});
	}

	@Transactional
	public boolean removeParticipant(int conversationId, int userId) {
		return jdbcTemplate.update("DELETE FROM chat_participants WHERE conversation_id = ? AND user_id = ?",
				conversationId, userId) > 0;
	}

	@Transactional
	public boolean leaveGroup(int conversationId, int userId) {
		return jdbcTemplate.update("DELETE FROM chat_participants WHERE conversation_id = ? AND user_id = ?",
				conversationId, userId) > 0;
	}

	@Transactional
	public boolean deleteGroup(int conversationId) {
		return jdbcTemplate.update("DELETE FROM chat_conversations WHERE id = ?", conversationId) > 0;
	}
}