package de.technikteam.dao;

import de.technikteam.model.EventCustomField;
import de.technikteam.model.EventCustomFieldResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventCustomFieldDAO {
	private static final Logger logger = LogManager.getLogger(EventCustomFieldDAO.class);

	public void saveCustomFieldsForEvent(int eventId, List<EventCustomField> fields) {
		String deleteSql = "DELETE FROM event_custom_fields WHERE event_id = ?";
		String insertSql = "INSERT INTO event_custom_fields (event_id, field_name, field_type, is_required) VALUES (?, ?, ?, ?)";
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);

			// Clear old fields first
			try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
				deleteStmt.setInt(1, eventId);
				deleteStmt.executeUpdate();
			}

			// Insert new fields
			if (fields != null && !fields.isEmpty()) {
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					for (EventCustomField field : fields) {
						insertStmt.setInt(1, eventId);
						insertStmt.setString(2, field.getFieldName());
						insertStmt.setString(3, field.getFieldType());
						insertStmt.setBoolean(4, field.isRequired());
						insertStmt.addBatch();
					}
					insertStmt.executeBatch();
				}
			}
			conn.commit();
			logger.info("Successfully saved {} custom fields for event ID {}", fields != null ? fields.size() : 0,
					eventId);
		} catch (SQLException e) {
			logger.error("Error saving custom fields for event ID {}. Rolling back.", eventId, e);
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.error("Rollback failed.", ex);
				}
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException ex) {
					logger.error("Failed to close connection.", ex);
				}
		}
	}

	public List<EventCustomField> getCustomFieldsForEvent(int eventId) {
		List<EventCustomField> fields = new ArrayList<>();
		String sql = "SELECT * FROM event_custom_fields WHERE event_id = ? ORDER BY id";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventCustomField field = new EventCustomField();
					field.setId(rs.getInt("id"));
					field.setEventId(rs.getInt("event_id"));
					field.setFieldName(rs.getString("field_name"));
					field.setFieldType(rs.getString("field_type"));
					field.setRequired(rs.getBoolean("is_required"));
					fields.add(field);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching custom fields for event ID {}", eventId, e);
		}
		return fields;
	}

	public void saveResponse(EventCustomFieldResponse response) {
		String sql = "INSERT INTO event_custom_field_responses (field_id, user_id, response_value) VALUES (?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE response_value = VALUES(response_value)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, response.getFieldId());
			pstmt.setInt(2, response.getUserId());
			pstmt.setString(3, response.getResponseValue());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error saving custom field response for field {} and user {}", response.getFieldId(),
					response.getUserId(), e);
		}
	}
}