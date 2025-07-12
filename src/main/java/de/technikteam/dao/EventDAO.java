package de.technikteam.dao;

import de.technikteam.model.Event;
import de.technikteam.model.EventAttendance;
import de.technikteam.model.SkillRequirement;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDAO {
	private static final Logger logger = LogManager.getLogger(EventDAO.class);

	private Event mapResultSetToEvent(ResultSet resultSet) throws SQLException {
		Event event = new Event();
		event.setId(resultSet.getInt("id"));
		event.setName(resultSet.getString("name"));
		event.setDescription(resultSet.getString("description"));
		event.setLocation(resultSet.getString("location"));
		event.setStatus(resultSet.getString("status"));

		Timestamp eventTimestamp = resultSet.getTimestamp("event_datetime");
		if (eventTimestamp != null) {
			event.setEventDateTime(eventTimestamp.toLocalDateTime());
		}
		Timestamp endTimestamp = resultSet.getTimestamp("end_datetime");
		if (endTimestamp != null) {
			event.setEndDateTime(endTimestamp.toLocalDateTime());
		}

		if (DaoUtils.hasColumn(resultSet, "leader_user_id")) {
			event.setLeaderUserId(resultSet.getInt("leader_user_id"));
		}
		if (DaoUtils.hasColumn(resultSet, "leader_username")) {
			event.setLeaderUsername(resultSet.getString("leader_username"));
		}
		return event;
	}

	private User mapResultSetToSimpleUser(ResultSet resultSet) throws SQLException {
		return new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("role"));
	}

	public List<Event> getEventHistoryForUser(int userId) {
		List<Event> history = new ArrayList<>();
		String sql = "SELECT e.*, ea.signup_status FROM events e "
				+ "JOIN event_attendance ea ON e.id = ea.event_id WHERE ea.user_id = ? ORDER BY e.event_datetime DESC";
		logger.debug("Fetching event history for user ID: {}", userId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					Event event = mapResultSetToEvent(resultSet);
					event.setUserAttendanceStatus(resultSet.getString("signup_status"));
					history.add(event);
				}
			}
			logger.info("Found {} events in history for user ID: {}", history.size(), userId);
		} catch (SQLException exception) {
			logger.error("SQL error fetching event history for user {}", userId, exception);
		}
		return history;
	}

	public Event getEventById(int eventId) {
		String sql = "SELECT e.*, u.username as leader_username " + "FROM events e "
				+ "LEFT JOIN users u ON e.leader_user_id = u.id " + "WHERE e.id = ?";
		logger.debug("Fetching event by ID: {}", eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					logger.info("Found event '{}' with ID: {}", resultSet.getString("name"), eventId);
					return mapResultSetToEvent(resultSet);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching event by ID: {}", eventId, exception);
		}
		logger.warn("No event found with ID: {}", eventId);
		return null;
	}

	public List<Event> getAllEvents() {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT e.*, u.username as leader_username " + "FROM events e "
				+ "LEFT JOIN users u ON e.leader_user_id = u.id " + "ORDER BY e.event_datetime DESC";
		logger.debug("Fetching all events.");
		try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(sql)) {
			while (resultSet.next()) {
				events.add(mapResultSetToEvent(resultSet));
			}
			logger.info("Fetched a total of {} events.", events.size());
		} catch (SQLException exception) {
			logger.error("SQL error fetching all events.", exception);
		}
		return events;
	}

	public List<Event> getActiveEvents() {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events WHERE status IN ('GEPLANT', 'KOMPLETT', 'LAUFEND') ORDER BY event_datetime ASC";
		logger.debug("Fetching active events.");
		try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(sql)) {
			while (resultSet.next()) {
				events.add(mapResultSetToEvent(resultSet));
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching active events.", exception);
		}
		return events;
	}

	public int createEvent(Event event) {
		String sql = "INSERT INTO events (name, event_datetime, end_datetime, description, location, status, leader_user_id) VALUES (?, ?, ?, ?, ?, 'GEPLANT', ?)";
		logger.debug("Attempting to create new event: {}", event.getName());
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql,
						Statement.RETURN_GENERATED_KEYS)) {
			preparedStatement.setString(1, event.getName());
			preparedStatement.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			if (event.getEndDateTime() != null) {
				preparedStatement.setTimestamp(3, Timestamp.valueOf(event.getEndDateTime()));
			} else {
				preparedStatement.setNull(3, Types.TIMESTAMP);
			}
			preparedStatement.setString(4, event.getDescription());
			preparedStatement.setString(5, event.getLocation());
			if (event.getLeaderUserId() > 0) {
				preparedStatement.setInt(6, event.getLeaderUserId());
			} else {
				preparedStatement.setNull(6, Types.INTEGER);
			}

			if (preparedStatement.executeUpdate() > 0) {
				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newId = generatedKeys.getInt(1);
						logger.info("Successfully created event '{}' with ID {}", event.getName(), newId);
						return newId;
					}
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error creating event '{}'.", event.getName(), exception);
		}
		return 0;
	}

	public boolean updateEvent(Event event) {
		String sql = "UPDATE events SET name = ?, event_datetime = ?, end_datetime = ?, description = ?, location = ?, status = ?, leader_user_id = ? WHERE id = ?";
		logger.debug("Attempting to update event with ID: {}", event.getId());
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, event.getName());
			preparedStatement.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			if (event.getEndDateTime() != null) {
				preparedStatement.setTimestamp(3, Timestamp.valueOf(event.getEndDateTime()));
			} else {
				preparedStatement.setNull(3, Types.TIMESTAMP);
			}
			preparedStatement.setString(4, event.getDescription());
			preparedStatement.setString(5, event.getLocation());
			preparedStatement.setString(6, event.getStatus());
			if (event.getLeaderUserId() > 0) {
				preparedStatement.setInt(7, event.getLeaderUserId());
			} else {
				preparedStatement.setNull(7, Types.INTEGER);
			}
			preparedStatement.setInt(8, event.getId());

			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error updating event with ID: {}", event.getId(), exception);
		}
		return false;
	}

	public boolean deleteEvent(int eventId) {
		String sql = "DELETE FROM events WHERE id = ?";
		logger.debug("Attempting to delete event with ID: {}", eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error deleting event with ID: {}", eventId, exception);
		}
		return false;
	}

	public boolean updateEventStatus(int eventId, String newStatus) {
		String sql = "UPDATE events SET status = ? WHERE id = ?";
		logger.debug("Attempting to update status for event {} to '{}'", eventId, newStatus);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, newStatus);
			preparedStatement.setInt(2, eventId);
			return preparedStatement.executeUpdate() > 0;
		} catch (SQLException exception) {
			logger.error("SQL error updating status for event ID: {}", eventId, exception);
			return false;
		}
	}

	public void signUpForEvent(int userId, int eventId) {
		String sql = "INSERT INTO event_attendance (user_id, event_id, signup_status, commitment_status) VALUES (?, ?, 'ANGEMELDET', 'OFFEN') ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		logger.debug("Signing up user {} for event {}", userId, eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, eventId);
			preparedStatement.executeUpdate();
			logger.info("User {} successfully signed up for event {}", userId, eventId);
		} catch (SQLException exception) {
			logger.error("SQL error during event sign-up for user {} and event {}", userId, eventId, exception);
		}
	}

	public void signOffFromEvent(int userId, int eventId) {
		String sql = "UPDATE event_attendance SET signup_status = 'ABGEMELDET', commitment_status = 'OFFEN' WHERE user_id = ? AND event_id = ?";
		logger.debug("Signing off user {} from event {}", userId, eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			preparedStatement.setInt(2, eventId);
			preparedStatement.executeUpdate();
			logger.info("User {} successfully signed off from event {}", userId, eventId);
		} catch (SQLException exception) {
			logger.error("SQL error during event sign-off for user {} and event {}", userId, eventId, exception);
		}
	}

	public List<User> getSignedUpUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, r.role_name as role FROM users u "
				+ "JOIN event_attendance ea ON u.id = ea.user_id " + "LEFT JOIN roles r on u.role_id = r.id "
				+ "WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
		logger.debug("Fetching signed up users for event ID: {}", eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next())
					users.add(mapResultSetToSimpleUser(resultSet));
			}
			logger.info("Found {} signed-up users for event ID: {}", users.size(), eventId);
		} catch (SQLException exception) {
			logger.error("SQL error fetching signed-up users for event ID: {}", eventId, exception);
		}
		return users;
	}

	public void assignUsersToEvent(int eventId, String[] userIds) {
		String deleteSql = "DELETE FROM event_assignments WHERE event_id = ?";
		String insertSql = "INSERT INTO event_assignments (event_id, user_id) VALUES (?, ?)";
		logger.debug("Assigning users to event ID: {}", eventId);

		try (Connection connection = DatabaseManager.getConnection()) {
			connection.setAutoCommit(false);

			try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
				deleteStatement.setInt(1, eventId);
				deleteStatement.executeUpdate();
			}

			if (userIds != null && userIds.length > 0) {
				try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
					for (String userId : userIds) {
						insertStatement.setInt(1, eventId);
						insertStatement.setInt(2, Integer.parseInt(userId));
						insertStatement.addBatch();
					}
					insertStatement.executeBatch();
				}
			}

			connection.commit();
			logger.info("Successfully assigned {} users to event ID {}", (userIds != null ? userIds.length : 0),
					eventId);

		} catch (SQLException | NumberFormatException exception) {
			logger.error(
					"SQL transaction error during user assignment for event ID: {}. Transaction will be rolled back.",
					eventId, exception);
			// The try-with-resources on the connection will handle rollback if autoCommit
			// is false.
		}
	}

	public List<SkillRequirement> getSkillRequirementsForEvent(int eventId) {
		List<SkillRequirement> requirements = new ArrayList<>();
		String sql = "SELECT esr.required_course_id, c.name as course_name, esr.required_persons "
				+ "FROM event_skill_requirements esr " + "JOIN courses c ON esr.required_course_id = c.id "
				+ "WHERE esr.event_id = ?";
		logger.debug("Fetching skill requirements for event ID: {}", eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					SkillRequirement requirement = new SkillRequirement();
					requirement.setRequiredCourseId(resultSet.getInt("required_course_id"));
					requirement.setCourseName(resultSet.getString("course_name"));
					requirement.setRequiredPersons(resultSet.getInt("required_persons"));
					requirements.add(requirement);
				}
			}
			logger.info("Found {} skill requirements for event ID: {}", requirements.size(), eventId);
		} catch (SQLException exception) {
			logger.error("SQL error fetching skill requirements for event ID: {}", eventId, exception);
		}
		return requirements;
	}

	public void saveSkillRequirements(int eventId, String[] requiredCourseIds, String[] requiredPersons) {
		String deleteSql = "DELETE FROM event_skill_requirements WHERE event_id = ?";
		String insertSql = "INSERT INTO event_skill_requirements (event_id, required_course_id, required_persons) VALUES (?, ?, ?)";
		logger.debug("Saving skill requirements for event ID: {}", eventId);

		try (Connection connection = DatabaseManager.getConnection()) {
			connection.setAutoCommit(false);

			try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
				deleteStatement.setInt(1, eventId);
				deleteStatement.executeUpdate();
			}

			if (requiredCourseIds != null && requiredPersons != null
					&& requiredCourseIds.length == requiredPersons.length) {
				try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
					for (int i = 0; i < requiredCourseIds.length; i++) {
						if (requiredCourseIds[i] == null || requiredCourseIds[i].isEmpty())
							continue;
						insertStatement.setInt(1, eventId);
						insertStatement.setInt(2, Integer.parseInt(requiredCourseIds[i]));
						insertStatement.setInt(3, Integer.parseInt(requiredPersons[i]));
						insertStatement.addBatch();
					}
					insertStatement.executeBatch();
				}
			}

			connection.commit();
			logger.info("Successfully saved skill requirements for event ID: {}", eventId);
		} catch (SQLException | NumberFormatException exception) {
			logger.error(
					"Transaction error during saving skill requirements for event ID: {}. Transaction will be rolled back.",
					eventId, exception);
		}
	}

	public void saveReservations(int eventId, String[] itemIds, String[] quantities) {
		String deleteSql = "DELETE FROM event_storage_reservations WHERE event_id = ?";
		String insertSql = "INSERT INTO event_storage_reservations (event_id, item_id, reserved_quantity) VALUES (?, ?, ?)";

		try (Connection connection = DatabaseManager.getConnection()) {
			connection.setAutoCommit(false);

			try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
				deleteStatement.setInt(1, eventId);
				deleteStatement.executeUpdate();
			}

			if (itemIds != null && quantities != null && itemIds.length == quantities.length) {
				try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
					for (int i = 0; i < itemIds.length; i++) {
						if (itemIds[i] == null || itemIds[i].isEmpty())
							continue;
						insertStatement.setInt(1, eventId);
						insertStatement.setInt(2, Integer.parseInt(itemIds[i]));
						insertStatement.setInt(3, Integer.parseInt(quantities[i]));
						insertStatement.addBatch();
					}
					insertStatement.executeBatch();
				}
			}
			connection.commit();
			logger.info("Successfully saved storage reservations for event ID: {}", eventId);
		} catch (SQLException | NumberFormatException exception) {
			logger.error("Error saving storage reservations for event {}. Transaction will be rolled back.", eventId,
					exception);
		}
	}

	public List<StorageItem> getReservedItemsForEvent(int eventId) {
		List<StorageItem> items = new ArrayList<>();
		String sql = "SELECT si.id, si.name, esr.reserved_quantity FROM event_storage_reservations esr "
				+ "JOIN storage_items si ON esr.item_id = si.id WHERE esr.event_id = ?";
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					StorageItem item = new StorageItem();
					item.setId(resultSet.getInt("id"));
					item.setName(resultSet.getString("name"));
					item.setQuantity(resultSet.getInt("reserved_quantity"));
					items.add(item);
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching reserved items for event ID: {}", eventId, exception);
		}
		return items;
	}

	public List<Event> getAllActiveAndUpcomingEvents() {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events WHERE status NOT IN ('ABGESCHLOSSEN', 'ABGESAGT') AND event_datetime >= NOW() - INTERVAL 1 DAY ORDER BY event_datetime ASC";
		logger.debug("Fetching all active and upcoming events for calendar feed.");
		try (Connection connection = DatabaseManager.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(sql)) {
			while (resultSet.next()) {
				events.add(mapResultSetToEvent(resultSet));
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching active/upcoming events for calendar.", exception);
		}
		return events;
	}

	public List<Event> getUpcomingEventsForUser(User user, int limit) {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT e.*, " + "CASE " + "    WHEN eas.user_id IS NOT NULL THEN 'ZUGEWIESEN' "
				+ "    WHEN ea.signup_status IS NOT NULL THEN ea.signup_status " + "    ELSE 'OFFEN' "
				+ "END AS calculated_user_status " + "FROM events e "
				+ "LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = ? "
				+ "LEFT JOIN event_assignments eas ON e.id = eas.event_id AND eas.user_id = ? "
				+ "WHERE e.event_datetime >= NOW() " + "AND ("
				+ "  NOT EXISTS (SELECT 1 FROM event_skill_requirements esr WHERE esr.event_id = e.id) OR "
				+ "  EXISTS (SELECT 1 FROM event_skill_requirements esr JOIN user_qualifications uq ON esr.required_course_id = uq.course_id WHERE esr.event_id = e.id AND uq.user_id = ? AND uq.status = 'ABSOLVIERT')"
				+ ") " + "ORDER BY e.event_datetime ASC" + (limit > 0 ? " LIMIT ?" : "");

		logger.debug("Fetching upcoming events for user ID: {} with limit: {}", user.getId(), limit);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setInt(1, user.getId());
			preparedStatement.setInt(2, user.getId());
			preparedStatement.setInt(3, user.getId());
			if (limit > 0) {
				preparedStatement.setInt(4, limit);
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					Event event = mapResultSetToEvent(resultSet);
					String finalStatus = resultSet.getString("calculated_user_status");
					event.setUserAttendanceStatus(finalStatus);
					events.add(event);
				}
				logger.info("Found {} qualified upcoming events for user ID {}", events.size(), user.getId());
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching qualified upcoming events for user {}", user.getId(), exception);
		}
		return events;
	}

	public boolean isUserAssociatedWithEvent(int eventId, int userId) {
		String sql = "SELECT 1 FROM event_attendance WHERE event_id = ? AND user_id = ? AND signup_status = 'ANGEMELDET' "
				+ "UNION " + "SELECT 1 FROM event_assignments WHERE event_id = ? AND user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			pstmt.setInt(2, userId);
			pstmt.setInt(3, eventId);
			pstmt.setInt(4, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking user association for event {} and user {}", eventId, userId, e);
			return false;
		}
	}

	public List<User> getAssignedUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, r.role_name AS role FROM users u "
				+ "JOIN event_assignments ea ON u.id = ea.user_id " + "LEFT JOIN roles r ON u.role_id = r.id "
				+ "WHERE ea.event_id = ?";
		logger.debug("Fetching assigned users for event ID: {}", eventId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, eventId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					users.add(mapResultSetToSimpleUser(resultSet));
				}
			}
			logger.info("Found {} assigned users for event ID: {}", users.size(), eventId);
		} catch (SQLException exception) {
			logger.error("SQL error fetching assigned users for event ID: {}", eventId, exception);
		}
		return users;
	}

	public List<Event> getCompletedEventsForUser(int userId) {
		List<Event> history = new ArrayList<>();
		String sql = "SELECT e.* FROM events e " + "JOIN event_assignments ea ON e.id = ea.event_id "
				+ "WHERE ea.user_id = ? AND e.status = 'ABGESCHLOSSEN' " + "ORDER BY e.event_datetime DESC";
		logger.debug("Fetching completed event history for user ID: {}", userId);
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					history.add(mapResultSetToEvent(resultSet));
				}
			}
		} catch (SQLException exception) {
			logger.error("SQL error fetching completed event history for user {}", userId, exception);
		}
		return history;
	}

	public List<Event> getAssignedEventsForUser(int userId, int limit) {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT e.* FROM events e " + "JOIN event_assignments ea ON e.id = ea.event_id "
				+ "WHERE ea.user_id = ? AND e.event_datetime >= NOW() " + "ORDER BY e.event_datetime ASC";
		if (limit > 0) {
			sql += " LIMIT ?";
		}
		try (Connection connection = DatabaseManager.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setInt(1, userId);
			if (limit > 0) {
				preparedStatement.setInt(2, limit);
			}
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					events.add(mapResultSetToEvent(resultSet));
				}
			}
		} catch (SQLException exception) {
			logger.error("Error fetching assigned events for user {}", userId, exception);
		}
		return events;
	}

	public List<User> getQualifiedAndAvailableUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		// This complex query first identifies all users who meet ALL skill requirements
		// for the event.
		// It then excludes users who are already assigned to an overlapping event.
		String sql = "SELECT u.id, u.username, r.role_name AS role " + "FROM users u "
				+ "LEFT JOIN roles r ON u.role_id = r.id " +
				// Condition 1: User has ALL required skills for the event.
				// This subquery counts the required skills for the event and ensures the user
				// has all of them.
				"WHERE (SELECT COUNT(*) FROM event_skill_requirements WHERE event_id = ?) = "
				+ "      (SELECT COUNT(*) FROM user_qualifications uq "
				+ "       JOIN event_skill_requirements esr ON uq.course_id = esr.required_course_id "
				+ "       WHERE esr.event_id = ? AND uq.user_id = u.id AND uq.status = 'ABSOLVIERT') " + "AND " +
				// Condition 2: User is not already assigned to an overlapping event.
				"u.id NOT IN ( " + "  SELECT ea.user_id FROM event_assignments ea "
				+ "  JOIN events conflicting_event ON ea.event_id = conflicting_event.id "
				+ "  JOIN events target_event ON target_event.id = ? "
				+ "  WHERE conflicting_event.id != target_event.id "
				+ "  AND conflicting_event.event_datetime < COALESCE(target_event.end_datetime, target_event.event_datetime + INTERVAL 2 HOUR) "
				+ "  AND COALESCE(conflicting_event.end_datetime, conflicting_event.event_datetime + INTERVAL 2 HOUR) > target_event.event_datetime "
				+ ") " + "ORDER BY u.username ASC";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(mapResultSetToSimpleUser(rs));
				}
			}
			logger.info("Found {} qualified and available users for event ID {}", users.size(), eventId);
		} catch (SQLException e) {
			logger.error("Error finding qualified and available users for event ID {}", eventId, e);
		}
		return users;
	}
}