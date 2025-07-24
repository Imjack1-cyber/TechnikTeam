package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.EventCustomField;
import de.technikteam.model.EventCustomFieldResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class EventCustomFieldDAO {
	private static final Logger logger = LogManager.getLogger(EventCustomFieldDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public EventCustomFieldDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public void saveCustomFieldsForEvent(int eventId, List<EventCustomField> fields, Connection conn)
			throws SQLException {
		String deleteSql = "DELETE FROM event_custom_fields WHERE event_id = ?";
		String insertSql = "INSERT INTO event_custom_fields (event_id, field_name, field_type, is_required, field_options) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement deleteStatement = conn.prepareStatement(deleteSql)) {
			deleteStatement.setInt(1, eventId);
			deleteStatement.executeUpdate();
		}
		if (fields != null && !fields.isEmpty()) {
			try (PreparedStatement insertStatement = conn.prepareStatement(insertSql)) {
				for (EventCustomField field : fields) {
					insertStatement.setInt(1, eventId);
					insertStatement.setString(2, field.getFieldName());
					insertStatement.setString(3, field.getFieldType());
					insertStatement.setBoolean(4, field.isRequired());
					insertStatement.setString(5, field.getFieldOptions());
					insertStatement.addBatch();
				}
				insertStatement.executeBatch();
			}
		}
	}

	public List<EventCustomField> getCustomFieldsForEvent(int eventId) {
		List<EventCustomField> fields = new ArrayList<>();
		String sql = "SELECT * FROM event_custom_fields WHERE event_id = ? ORDER BY id";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					EventCustomField field = new EventCustomField();
					field.setId(resultSet.getInt("id"));
					field.setEventId(resultSet.getInt("event_id"));
					field.setFieldName(resultSet.getString("field_name"));
					field.setFieldType(resultSet.getString("field_type"));
					field.setRequired(resultSet.getBoolean("is_required"));
					field.setFieldOptions(resultSet.getString("field_options"));
					fields.add(field);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching custom fields for event ID {}", eventId, e);
		}
		return fields;
	}

	public void saveResponse(EventCustomFieldResponse response) {
		String sql = "INSERT INTO event_custom_field_responses (field_id, user_id, response_value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE response_value = VALUES(response_value)";
		try (Connection connection = dbManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, response.getFieldId());
			preparedStatement.setInt(2, response.getUserId());
			preparedStatement.setString(3, response.getResponseValue());
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			logger.error("Error saving custom field response for field {} and user {}", response.getFieldId(),
					response.getUserId(), e);
		}
	}
}