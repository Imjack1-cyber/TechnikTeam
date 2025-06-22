package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.Event;
import de.technikteam.model.EventAttendance;
import de.technikteam.model.SkillRequirement;
import de.technikteam.model.User;

/**
 * A comprehensive DAO for all database operations related to the `events`
 * table. It handles creating, reading, updating, and deleting events.
 * Additionally, it manages user sign-ups/sign-offs, event skill requirements,
 * and the final assignment of users to an event. It contains methods to supply
 * data for both regular user views (like upcoming events) and administrative
 * back-end pages.
 */
public class EventDAO {
	private static final Logger logger = LogManager.getLogger(EventDAO.class);

	// --- Private Helper Methods ---

	/**
	 * Maps a row from a ResultSet to an Event object.
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated Event object.
	 * @throws SQLException If a database error occurs.
	 */
	private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
		Event event = new Event();
		event.setId(rs.getInt("id"));
		event.setName(rs.getString("name"));
		event.setEventDateTime(rs.getTimestamp("event_datetime").toLocalDateTime());
		if (rs.getTimestamp("end_datetime") != null) {
			event.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
		}
		event.setDescription(rs.getString("description"));
		event.setStatus(rs.getString("status"));
		return event;
	}

	public void setAttendanceCommitment(int eventId, int userId, String commitment) {
		String sql = "UPDATE event_attendance SET commitment_status = ? WHERE event_id = ? AND user_id = ?";
		logger.debug("Setting attendance commitment for user {} event {} to '{}'", userId, eventId, commitment);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, commitment);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL error setting attendance commitment for user {} event {}", userId, eventId, e);
		}
	}

	/**
	 * Maps a row from a ResultSet to a simplified User object (ID, username, role).
	 * 
	 * @param rs The ResultSet to map.
	 * @return A populated User object.
	 * @throws SQLException If a database error occurs.
	 */
	private User mapResultSetToSimpleUser(ResultSet rs) throws SQLException {
		return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
	}

	/**
	 * Checks if a ResultSet contains a column with the given name
	 * (case-insensitive).
	 * 
	 * @param rs         The ResultSet to check.
	 * @param columnName The name of the column.
	 * @return true if the column exists, false otherwise.
	 * @throws SQLException If a database error occurs.
	 */
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if (columnName.equalsIgnoreCase(rsmd.getColumnName(i))) {
				return true;
			}
		}
		return false;
	}

	// --- Methods for Public and User-Specific Views ---

	/**
	 * Fetches the event participation history for a specific user.
	 * 
	 * @param userId The ID of the user.
	 * @return A list of all past and present events the user has interacted with.
	 */
	public List<Event> getEventHistoryForUser(int userId) {
		List<Event> history = new ArrayList<>();
		String sql = "SELECT e.*, ea.signup_status FROM events e "
				+ "JOIN event_attendance ea ON e.id = ea.event_id WHERE ea.user_id = ? ORDER BY e.event_datetime DESC";
		logger.debug("Fetching event history for user ID: {}", userId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Event event = mapResultSetToEvent(rs);
				event.setUserAttendanceStatus(rs.getString("signup_status"));
				history.add(event);
			}
			logger.info("Found {} events in history for user ID: {}", history.size(), userId);
		} catch (SQLException e) {
			logger.error("SQL error fetching event history for user {}", userId, e);
		}
		return history;
	}

	// --- Methods for Admin Views & CRUD ---

	/**
	 * Fetches a single event by its ID.
	 * 
	 * @param eventId The ID of the event.
	 * @return An Event object, or null if not found.
	 */
	public Event getEventById(int eventId) {
		String sql = "SELECT * FROM events WHERE id = ?";
		logger.debug("Fetching event by ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found event '{}' with ID: {}", rs.getString("name"), eventId);
					return mapResultSetToEvent(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching event by ID: {}", eventId, e);
		}
		logger.warn("No event found with ID: {}", eventId);
		return null;
	}

	/**
	 * Fetches all events from the database, newest first.
	 * 
	 * @return A list of all Event objects.
	 */
	public List<Event> getAllEvents() {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events ORDER BY event_datetime DESC";
		logger.debug("Fetching all events.");
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				events.add(mapResultSetToEvent(rs));
			}
			logger.info("Fetched a total of {} events.", events.size());
		} catch (SQLException e) {
			logger.error("SQL error fetching all events.", e);
		}
		return events;
	}

	/**
	 * Creates a new event in the database.
	 * 
	 * @param event The Event object to persist.
	 * @return The ID of the newly created event, or 0 on failure.
	 */
	public int createEvent(Event event) {
		String sql = "INSERT INTO events (name, event_datetime, end_datetime, description, status) VALUES (?, ?, ?, ?, ?)";
		logger.debug("Attempting to create new event: {}", event.getName());
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			if (event.getEndDateTime() != null) {
				pstmt.setTimestamp(3, Timestamp.valueOf(event.getEndDateTime()));
			} else {
				pstmt.setNull(3, Types.TIMESTAMP);
			}
			pstmt.setString(4, event.getDescription());
			pstmt.setString(5, "GEPLANT");
			if (pstmt.executeUpdate() > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int newId = generatedKeys.getInt(1);
						logger.info("Successfully created event '{}' with ID {}", event.getName(), newId);
						return newId;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating event '{}'.", event.getName(), e);
		}
		return 0;
	}

	/**
	 * Updates an existing event in the database.
	 * 
	 * @param event The Event object with updated data.
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateEvent(Event event) {
		String sql = "UPDATE events SET name = ?, event_datetime = ?, end_datetime = ?, description = ?, status = ? WHERE id = ?";
		logger.debug("Attempting to update event with ID: {}", event.getId());
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			if (event.getEndDateTime() != null) {
				pstmt.setTimestamp(3, Timestamp.valueOf(event.getEndDateTime()));
			} else {
				pstmt.setNull(3, Types.TIMESTAMP);
			}
			pstmt.setString(4, event.getDescription());
			pstmt.setString(5, event.getStatus());
			pstmt.setInt(6, event.getId());
			boolean success = pstmt.executeUpdate() > 0;
			if (success)
				logger.info("Successfully updated event with ID: {}", event.getId());
			return success;
		} catch (SQLException e) {
			logger.error("SQL error updating event with ID: {}", event.getId(), e);
		}
		return false;
	}

	/**
	 * Deletes an event from the database.
	 * 
	 * @param eventId The ID of the event to delete.
	 * @return true if deletion was successful, false otherwise.
	 */
	public boolean deleteEvent(int eventId) {
		String sql = "DELETE FROM events WHERE id = ?";
		logger.debug("Attempting to delete event with ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			boolean success = pstmt.executeUpdate() > 0;
			if (success)
				logger.warn("Successfully deleted event with ID: {}", eventId);
			return success;
		} catch (SQLException e) {
			logger.error("SQL error deleting event with ID: {}", eventId, e);
		}
		return false;
	}

	/**
	 * Updates only the status of a specific event.
	 * 
	 * @param eventId   The ID of the event to update.
	 * @param newStatus The new status string (e.g., 'LAUFEND', 'ABGESCHLOSSEN').
	 * @return true if the update was successful, false otherwise.
	 */
	public boolean updateEventStatus(int eventId, String newStatus) {
		String sql = "UPDATE events SET status = ? WHERE id = ?";
		logger.debug("Attempting to update status for event {} to '{}'", eventId, newStatus);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, newStatus);
			pstmt.setInt(2, eventId);
			boolean success = pstmt.executeUpdate() > 0;
			if (success)
				logger.info("Updating status for event {} to '{}' was successful.", eventId, newStatus);
			else
				logger.warn("Updating status for event {} to '{}' failed (0 rows affected).", eventId, newStatus);
			return success;
		} catch (SQLException e) {
			logger.error("SQL error updating status for event ID: {}", eventId, e);
			return false;
		}
	}

	// --- Methods for User Actions & Admin Management ---

	/**
	 * Signs a user up for an event, or updates their status if they previously
	 * signed off.
	 * 
	 * @param userId  The ID of the user.
	 * @param eventId The ID of the event.
	 */
	public void signUpForEvent(int userId, int eventId) {
		String sql = "INSERT INTO event_attendance (user_id, event_id, signup_status, commitment_status) VALUES (?, ?, 'ANGEMELDET', 'OFFEN') ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		logger.debug("Signing up user {} for event {}", userId, eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
			logger.info("User {} successfully signed up for event {}", userId, eventId);
		} catch (SQLException e) {
			logger.error("SQL error during event sign-up for user {} and event {}", userId, eventId, e);
		}
	}

	/**
	 * Signs a user off from an event.
	 * 
	 * @param userId  The ID of the user.
	 * @param eventId The ID of the event.
	 */
	public void signOffFromEvent(int userId, int eventId) {
		String sql = "UPDATE event_attendance SET signup_status = 'ABGEMELDET', commitment_status = 'OFFEN' WHERE user_id = ? AND event_id = ?";
		logger.debug("Signing off user {} from event {}", userId, eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
			logger.info("User {} successfully signed off from event {}", userId, eventId);
		} catch (SQLException e) {
			logger.error("SQL error during event sign-off for user {} and event {}", userId, eventId, e);
		}
	}

	/**
	 * Fetches a list of all users who have signed up for a specific event.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of User objects.
	 */
	public List<User> getSignedUpUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u JOIN event_attendance ea ON u.id = ea.user_id WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
		logger.debug("Fetching signed up users for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next())
					users.add(mapResultSetToSimpleUser(rs));
			}
			logger.info("Found {} signed-up users for event ID: {}", users.size(), eventId);
		} catch (SQLException e) {
			logger.error("SQL error fetching signed-up users for event ID: {}", eventId, e);
		}
		return users;
	}

	/**
	 * Fetches detailed attendance information for an event, including signup and
	 * commitment status.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of EventAttendance objects.
	 */
	public List<EventAttendance> getAttendanceDetailsForEvent(int eventId) {
		List<EventAttendance> attendances = new ArrayList<>();
		String sql = "SELECT u.id, u.username, ea.signup_status, ea.commitment_status FROM event_attendance ea JOIN users u ON ea.user_id = u.id WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
		logger.debug("Fetching attendance details for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventAttendance attendance = new EventAttendance();
					attendance.setUserId(rs.getInt("id"));
					attendance.setUsername(rs.getString("username"));
					attendance.setSignupStatus(rs.getString("signup_status"));
					attendance.setCommitmentStatus(rs.getString("commitment_status"));
					attendances.add(attendance);
				}
			}
			logger.info("Found {} attendance detail records for event ID: {}", attendances.size(), eventId);
		} catch (SQLException e) {
			logger.error("SQL error fetching attendance details for event ID: {}", eventId, e);
		}
		return attendances;
	}

	/**
	 * Updates the commitment status (e.g., 'BESTÄTIGT') for a user's attendance at
	 * an event.
	 * 
	 * @param eventId The ID of the event.
	 * @param userId  The ID of the user.
	 * @param status  The new commitment status.
	 * @return true if the update was successful.
	 */
	public boolean updateCommitmentStatus(int eventId, int userId, String status) {
		String sql = "UPDATE event_attendance SET commitment_status = ? WHERE event_id = ? AND user_id = ?";
		logger.debug("Updating commitment status for event {}, user {} to '{}'", eventId, userId, status);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error while updating commitment status for event {}, user {}", eventId, userId, e);
		}
		return false;
	}

	/**
	 * Fetches all skill requirements (required courses and number of people) for an
	 * event.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of SkillRequirement objects.
	 */
	public List<SkillRequirement> getSkillRequirementsForEvent(int eventId) {
		List<SkillRequirement> requirements = new ArrayList<>();
		String sql = "SELECT esr.required_course_id, c.name as course_name, esr.required_persons FROM event_skill_requirements esr JOIN courses c ON esr.required_course_id = c.id WHERE esr.event_id = ?";
		logger.debug("Fetching skill requirements for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					SkillRequirement req = new SkillRequirement();
					req.setRequiredCourseId(rs.getInt("required_course_id"));
					req.setCourseName(rs.getString("course_name"));
					req.setRequiredPersons(rs.getInt("required_persons"));
					requirements.add(req);
				}
			}
			logger.info("Found {} skill requirements for event ID: {}", requirements.size(), eventId);
		} catch (SQLException e) {
			logger.error("SQL error fetching skill requirements for event ID: {}", eventId, e);
		}
		return requirements;
	}

	/**
	 * Saves the skill requirements for an event in a single transaction. It first
	 * deletes all existing requirements for the event, then inserts the new ones.
	 * 
	 * @param eventId           The ID of the event.
	 * @param requiredCourseIds An array of course IDs.
	 * @param requiredPersons   An array of the number of people required for each
	 *                          course.
	 */
	public void saveSkillRequirements(int eventId, String[] requiredCourseIds, String[] requiredPersons) {
		String deleteSql = "DELETE FROM event_skill_requirements WHERE event_id = ?";
		String insertSql = "INSERT INTO event_skill_requirements (event_id, required_course_id, required_persons, skill_name) VALUES (?, ?, ?, ?)";
		logger.debug("Saving skill requirements for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false); // Start transaction
			try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
				deletePstmt.setInt(1, eventId);
				deletePstmt.executeUpdate();
			}
			if (requiredCourseIds != null && requiredPersons != null
					&& requiredCourseIds.length == requiredPersons.length) {
				try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
					for (int i = 0; i < requiredCourseIds.length; i++) {
						if (requiredCourseIds[i] == null || requiredCourseIds[i].isEmpty()
								|| "0".equals(requiredPersons[i]))
							continue;
						insertPstmt.setInt(1, eventId);
						insertPstmt.setInt(2, Integer.parseInt(requiredCourseIds[i]));
						insertPstmt.setInt(3, Integer.parseInt(requiredPersons[i]));
						insertPstmt.setString(4, "Default");
						insertPstmt.addBatch();
					}
					insertPstmt.executeBatch();
				}
			}
			conn.commit(); // Commit transaction
			logger.info("Successfully saved skill requirements for event ID: {}", eventId);
		} catch (SQLException | NumberFormatException e) {
			logger.error("Transaction error during saving skill requirements for event ID: {}.", eventId, e);
			// In a real app, you would handle transaction rollback here.
		}
	}

	/**
	 * Fetches a list of users who have been definitively assigned to an event's
	 * final team.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of assigned User objects.
	 */
	public List<User> getAssignedUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u "
				+ "JOIN event_assignments ea ON u.id = ea.user_id WHERE ea.event_id = ?";
		logger.debug("Fetching assigned users for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(mapResultSetToSimpleUser(rs));
				}
			}
			logger.info("Found {} assigned users for event ID: {}", users.size(), eventId);
		} catch (SQLException e) {
			logger.error("SQL error fetching assigned users for event ID: {}", eventId, e);
		}
		return users;
	}

	/**
	 * Saves the final assignment of users to an event. This is a transactional
	 * operation: it first clears all existing assignments for the event and then
	 * inserts the new ones.
	 * 
	 * @param eventId The ID of the event.
	 * @param userIds An array of user IDs to be assigned.
	 */
	public void assignUsersToEvent(int eventId, String[] userIds) {
		String deleteSql = "DELETE FROM event_assignments WHERE event_id = ?";
		String insertSql = "INSERT INTO event_assignments (event_id, user_id) VALUES (?, ?)";
		logger.debug("Assigning users to event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false); // Start transaction

			// 1. Delete all previous assignments for this event
			try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
				deletePstmt.setInt(1, eventId);
				deletePstmt.executeUpdate();
			}

			// 2. Insert the new assignments if any users were selected
			if (userIds != null && userIds.length > 0) {
				try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
					for (String userId : userIds) {
						insertPstmt.setInt(1, eventId);
						insertPstmt.setInt(2, Integer.parseInt(userId));
						insertPstmt.addBatch();
					}
					insertPstmt.executeBatch();
				}
			}

			conn.commit(); // Commit transaction
			logger.info("Successfully assigned {} users to event ID {}", (userIds != null ? userIds.length : 0),
					eventId);

		} catch (SQLException | NumberFormatException e) {
			logger.error("SQL transaction error during user assignment for event ID: {}", eventId, e);
			// In a real app, you would handle transaction rollback here.
		}
	}

	/**
	 * Fetches all upcoming events for a user, with a calculated status that
	 * prioritizes assignments over simple sign-ups. Status can be: ZUGEWIESEN,
	 * ANGEMELDET, ABGEMELDET, or OFFEN.
	 *
	 * @param user  The currently logged-in user.
	 * @param limit The maximum number of events to return (0 for no limit).
	 * @return A list of upcoming Event objects with the correctly calculated user
	 *         status.
	 */
	public List<Event> getUpcomingEventsForUser(User user, int limit) {
		List<Event> events = new ArrayList<>();

		// This intelligent SQL query calculates the most relevant status for the user.
		// It prioritizes "ZUGEWIESEN" (assigned) over "ANGEMELDET" (signed up).
		String sql = "SELECT e.*, " + "CASE " + "    WHEN eas.user_id IS NOT NULL THEN 'ZUGEWIESEN' " + // 1. Check for
																										// assignment
																										// first
				"    WHEN ea.signup_status IS NOT NULL THEN ea.signup_status " + // 2. Fall back to signup status
				"    ELSE 'OFFEN' " + // 3. Default to open
				"END AS calculated_user_status " + "FROM events e "
				+ "LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = ? "
				+ "LEFT JOIN event_assignments eas ON e.id = eas.event_id AND eas.user_id = ? "
				+ "WHERE e.event_datetime >= NOW() " + "AND (" + // Qualification check remains the same
				"  NOT EXISTS (SELECT 1 FROM event_skill_requirements esr WHERE esr.event_id = e.id) OR "
				+ "  EXISTS (SELECT 1 FROM event_skill_requirements esr JOIN user_qualifications uq ON esr.required_course_id = uq.course_id WHERE esr.event_id = e.id AND uq.user_id = ?)"
				+ ") " + "ORDER BY e.event_datetime ASC" + (limit > 0 ? " LIMIT ?" : "");

		logger.debug("Fetching upcoming events for user ID: {} with limit: {}", user.getId(), limit);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// Set the user ID for all three placeholders in the query
			pstmt.setInt(1, user.getId());
			pstmt.setInt(2, user.getId());
			pstmt.setInt(3, user.getId());
			if (limit > 0) {
				pstmt.setInt(4, limit);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Event event = mapResultSetToEvent(rs); // Use your existing helper

					// Get the final calculated status from our new CASE statement
					String finalStatus = rs.getString("calculated_user_status");
					event.setUserAttendanceStatus(finalStatus);

					events.add(event);
				}
				logger.info("Found {} qualified upcoming events for user ID {}", events.size(), user.getId());
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching qualified upcoming events for user {}", user.getId(), e);
		}
		return events;
	}
}