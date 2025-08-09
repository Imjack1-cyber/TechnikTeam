package de.technikteam.dao;

import de.technikteam.model.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttachmentDAO {
	private static final Logger logger = LogManager.getLogger(AttachmentDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public AttachmentDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Attachment> attachmentRowMapper = (rs, rowNum) -> {
		Attachment att = new Attachment();
		att.setId(rs.getInt("id"));
		att.setParentType(rs.getString("parent_type"));
		att.setParentId(rs.getInt("parent_id"));
		att.setFilename(rs.getString("filename"));
		att.setFilepath(rs.getString("filepath"));
		att.setRequiredRole(rs.getString("required_role"));
		att.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
		return att;
	};

	public boolean addAttachment(Attachment attachment) {
		String sql = "INSERT INTO attachments (parent_type, parent_id, filename, filepath, required_role) VALUES (?, ?, ?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, attachment.getParentType(), attachment.getParentId(),
					attachment.getFilename(), attachment.getFilepath(), attachment.getRequiredRole()) > 0;
		} catch (Exception e) {
			logger.error("Error adding attachment to {} ID {}", attachment.getParentType(), attachment.getParentId(),
					e);
			return false;
		}
	}

	public List<Attachment> getAttachmentsForParent(String parentType, int parentId, String userRole) {
		String sql = "SELECT * FROM attachments WHERE parent_type = ? AND parent_id = ?";
		if (!"ADMIN".equalsIgnoreCase(userRole)) {
			sql += " AND required_role = 'NUTZER'";
		}
		try {
			return jdbcTemplate.query(sql, attachmentRowMapper, parentType, parentId);
		} catch (Exception e) {
			logger.error("Error fetching attachments for {} ID {}", parentType, parentId, e);
			return List.of();
		}
	}

	public Attachment getAttachmentById(int attachmentId) {
		String sql = "SELECT * FROM attachments WHERE id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, attachmentRowMapper, attachmentId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching attachment by ID {}", attachmentId, e);
			return null;
		}
	}

	public boolean deleteAttachment(int attachmentId) {
		String sql = "DELETE FROM attachments WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, attachmentId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting attachment ID {}", attachmentId, e);
			return false;
		}
	}
}