package de.technikteam.dao;

import de.technikteam.model.EventAttachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing file attachments for events, interacting with the
 * `event_attachments` table.
 */
public class EventAttachmentDAO {
	private static final Logger logger = LogManager.getLogger(EventAttachmentDAO.class);

	public boolean addAttachment(EventAttachment attachment) {
		String sql = "INSERT INTO event_attachments (event_id, filename, filepath, required_role) VALUES (?, ?, ?, ?)";
		logger.debug("Adding attachment '{}' to event ID {}", attachment.getFilename(), attachment.getEventId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachment.getEventId());
			pstmt.setString(2, attachment.getFilename());
			pstmt.setString(3, attachment.getFilepath());
			pstmt.setString(4, attachment.getRequiredRole());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error adding attachment to event {}", attachment.getEventId(), e);
			return false;
		}
	}

	public List<EventAttachment> getAttachmentsForEvent(int eventId, String userRole) {
		List<EventAttachment> attachments = new ArrayList<>();
		String sql = "SELECT * FROM event_attachments WHERE event_id = ?";
		if (!"ADMIN".equalsIgnoreCase(userRole)) {
			sql += " AND required_role = 'NUTZER'";
		}
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventAttachment att = new EventAttachment();
					att.setId(rs.getInt("id"));
					att.setEventId(rs.getInt("event_id"));
					att.setFilename(rs.getString("filename"));
					att.setFilepath(rs.getString("filepath"));
					att.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
					att.setRequiredRole(rs.getString("required_role"));
					attachments.add(att);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachments for event {}", eventId, e);
		}
		return attachments;
	}

	public EventAttachment getAttachmentById(int attachmentId) {
		String sql = "SELECT * FROM event_attachments WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					EventAttachment att = new EventAttachment();
					att.setId(rs.getInt("id"));
					att.setFilename(rs.getString("filename"));
					att.setFilepath(rs.getString("filepath"));
					att.setEventId(rs.getInt("event_id"));
					return att;
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachment by ID {}", attachmentId, e);
		}
		return null;
	}

	public boolean deleteAttachment(int attachmentId) {
		String sql = "DELETE FROM event_attachments WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting attachment ID {}", attachmentId, e);
			return false;
		}
	}
}