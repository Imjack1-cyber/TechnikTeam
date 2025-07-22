package de.technikteam.dao;

import de.technikteam.model.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A unified DAO for managing file attachments for various parent entities
 * (e.g., Events, Meetings), interacting with the generic `attachments` table.
 */
public class AttachmentDAO {
	private static final Logger logger = LogManager.getLogger(AttachmentDAO.class);

	private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
		Attachment att = new Attachment();
		att.setId(rs.getInt("id"));
		att.setParentType(rs.getString("parent_type"));
		att.setParentId(rs.getInt("parent_id"));
		att.setFilename(rs.getString("filename"));
		att.setFilepath(rs.getString("filepath"));
		att.setRequiredRole(rs.getString("required_role"));
		att.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		return att;
	}

	public boolean addAttachment(Attachment attachment) {
		logger.debug("Adding attachment '{}' to {} ID {} (manages its own connection)", attachment.getFilename(),
				attachment.getParentType(), attachment.getParentId());
		try (Connection conn = DatabaseManager.getConnection()) {
			return addAttachment(attachment, conn);
		} catch (SQLException e) {
			logger.error("Error adding attachment to {} ID {}", attachment.getParentType(), attachment.getParentId(),
					e);
			return false;
		}
	}

	public boolean addAttachment(Attachment attachment, Connection conn) throws SQLException {
		String sql = "INSERT INTO attachments (parent_type, parent_id, filename, filepath, required_role) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, attachment.getParentType());
			pstmt.setInt(2, attachment.getParentId());
			pstmt.setString(3, attachment.getFilename());
			pstmt.setString(4, attachment.getFilepath());
			pstmt.setString(5, attachment.getRequiredRole());
			return pstmt.executeUpdate() > 0;
		}
	}

	public List<Attachment> getAttachmentsForParent(String parentType, int parentId, String userRole) {
		List<Attachment> attachments = new ArrayList<>();
		String sql = "SELECT * FROM attachments WHERE parent_type = ? AND parent_id = ?";
		if (!"ADMIN".equalsIgnoreCase(userRole)) {
			sql += " AND required_role = 'NUTZER'";
		}
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, parentType);
			pstmt.setInt(2, parentId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					attachments.add(mapResultSetToAttachment(rs));
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachments for {} ID {}", parentType, parentId, e);
		}
		return attachments;
	}

	public Attachment getAttachmentById(int attachmentId) {
		String sql = "SELECT * FROM attachments WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToAttachment(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching attachment by ID {}", attachmentId, e);
		}
		return null;
	}

	public boolean deleteAttachment(int attachmentId) {
		String sql = "DELETE FROM attachments WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attachmentId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting attachment ID {}", attachmentId, e);
			return false;
		}
	}
}