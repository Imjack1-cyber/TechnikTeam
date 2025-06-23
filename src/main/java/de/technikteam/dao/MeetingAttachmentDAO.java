package de.technikteam.dao;

import de.technikteam.dao.DatabaseManager;
import de.technikteam.model.MeetingAttachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for managing file attachments for meetings, interacting with the
 * `meeting_attachments` table. It handles adding, retrieving (with role-based
 * filtering), and deleting file attachments associated with a specific meeting.
 */
public class MeetingAttachmentDAO {
	private static final Logger logger = LogManager.getLogger(MeetingAttachmentDAO.class);

	/**
	 * Attaches a file to a meeting by creating a record in the database.
	 * 
	 * @param attachment The MeetingAttachment object to persist.
	 * @return true if the record was successfully created.
	 */
	public boolean addAttachment(MeetingAttachment attachment) {
		String sql = "INSERT INTO meeting_attachments (meeting_id, filename, filepath, required_role) VALUES (?, ?, ?, ?)";
		logger.debug("Adding attachment '{}' to meeting ID {}", attachment.getFilename(), attachment.getMeetingId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachment.getMeetingId());
			pstmt.setString(2, attachment.getFilename());
			pstmt.setString(3, attachment.getFilepath());
			pstmt.setString(4, attachment.getRequiredRole());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error adding attachment to meeting {}", attachment.getMeetingId(), e);
			return false;
		}
	}

	/**
	 * Gets all attachments for a specific meeting, optionally filtering by user
	 * role. Admins see all files, while regular users only see files marked as
	 * 'NUTZER'.
	 * 
	 * @param meetingId The ID of the meeting.
	 * @param userRole  The role of the current user ("ADMIN" or "NUTZER").
	 * @return A list of MeetingAttachment objects.
	 */
	public List<MeetingAttachment> getAttachmentsForMeeting(int meetingId, String userRole) {
		List<MeetingAttachment> attachments = new ArrayList<>();

		String sql = "SELECT * FROM meeting_attachments WHERE meeting_id = ?";
		if (!"ADMIN".equalsIgnoreCase(userRole)) {
			sql += " AND required_role = 'NUTZER'";
			logger.debug("Fetching attachments for meeting {} with NUTZER role filter.", meetingId);
		} else {
			logger.debug("Fetching attachments for meeting {} with ADMIN role (no filter).", meetingId);
		}

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, meetingId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					MeetingAttachment att = new MeetingAttachment();
					att.setId(rs.getInt("id"));
					att.setMeetingId(rs.getInt("meeting_id"));
					att.setFilename(rs.getString("filename"));
					att.setFilepath(rs.getString("filepath"));
					att.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
					att.setRequiredRole(rs.getString("required_role"));
					attachments.add(att);
				}
				logger.info("Found {} attachments for meeting ID {}.", attachments.size(), meetingId);
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachments for meeting {}", meetingId, e);
		}
		return attachments;
	}

	/**
	 * Gets a single attachment by its ID, without any role check. This is typically
	 * used internally by admin functions like deletion.
	 * 
	 * @param attachmentId The ID of the attachment.
	 * @return A MeetingAttachment object or null if not found.
	 */
	public MeetingAttachment getAttachmentById(int attachmentId) {
		String sql = "SELECT * FROM meeting_attachments WHERE id = ?";
		logger.debug("Fetching attachment by ID: {}", attachmentId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					MeetingAttachment att = new MeetingAttachment();
					att.setId(rs.getInt("id"));
					att.setFilename(rs.getString("filename"));
					att.setFilepath(rs.getString("filepath"));
					att.setMeetingId(rs.getInt("meeting_id"));
					return att;
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachment by ID {}", attachmentId, e);
		}
		logger.warn("No attachment found for ID: {}", attachmentId);
		return null;
	}

	/**
	 * Deletes an attachment record from the database. The physical file must be
	 * deleted separately.
	 * 
	 * @param attachmentId The ID of the attachment to delete.
	 * @return true if successful.
	 */
	public boolean deleteAttachment(int attachmentId) {
		String sql = "DELETE FROM meeting_attachments WHERE id = ?";
		logger.warn("Attempting to delete attachment with ID: {}", attachmentId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting attachment ID {}", attachmentId, e);
			return false;
		}
	}
}