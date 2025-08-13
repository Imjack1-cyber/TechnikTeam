package de.technikteam.dao;

import de.technikteam.model.EventCustomField;
import de.technikteam.model.EventCustomFieldResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EventCustomFieldDAO {
	private static final Logger logger = LogManager.getLogger(EventCustomFieldDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventCustomFieldDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public void saveCustomFieldsForEvent(int eventId, List<EventCustomField> fields) {
		try {
			jdbcTemplate.update("DELETE FROM event_custom_fields WHERE event_id = ?", eventId);

			if (fields != null && !fields.isEmpty()) {
				String insertSql = "INSERT INTO event_custom_fields (event_id, field_name, field_type, is_required, field_options) VALUES (?, ?, ?, ?, ?)";
				jdbcTemplate.batchUpdate(insertSql, fields, 100, (ps, field) -> {
					ps.setInt(1, eventId);
					ps.setString(2, field.getFieldName());
					ps.setString(3, field.getFieldType());
					ps.setBoolean(4, field.isRequired());
					ps.setString(5, field.getFieldOptions());
				});
			}
		} catch (Exception e) {
			logger.error("Error in transaction for saving custom fields for event {}", eventId, e);
			throw new RuntimeException(e);
		}
	}

	public List<EventCustomField> getCustomFieldsForEvent(int eventId) {
		String sql = "SELECT * FROM event_custom_fields WHERE event_id = ? ORDER BY id";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				EventCustomField field = new EventCustomField();
				field.setId(rs.getInt("id"));
				field.setEventId(rs.getInt("event_id"));
				field.setFieldName(rs.getString("field_name"));
				field.setFieldType(rs.getString("field_type"));
				field.setRequired(rs.getBoolean("is_required"));
				field.setFieldOptions(rs.getString("field_options"));
				return field;
			}, eventId);
		} catch (Exception e) {
			logger.error("Error fetching custom fields for event ID {}", eventId, e);
			return List.of();
		}
	}

	public void saveResponse(EventCustomFieldResponse response) {
		String sql = "INSERT INTO event_custom_field_responses (field_id, user_id, response_value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE response_value = VALUES(response_value)";
		try {
			jdbcTemplate.update(sql, response.getFieldId(), response.getUserId(), response.getResponseValue());
		} catch (Exception e) {
			logger.error("Error saving custom field response for field {} and user {}", response.getFieldId(),
					response.getUserId(), e);
		}
	}
}