package de.technikteam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.Event;
import de.technikteam.model.SkillRequirement;
import de.technikteam.model.User;

public class EventDAO {
	private static final Logger logger = LogManager.getLogger(EventDAO.class);

	// --- Private Helper Method to reduce code duplication ---

	/**
	 * Maps a row from a ResultSet to an Event object.
	 * 
	 * @param rs The ResultSet, positioned at the correct row.
	 * @return A populated Event object.
	 * @throws SQLException If a database access error occurs.
	 */
	private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
		Event event = new Event();
		event.setId(rs.getInt("id"));
		event.setName(rs.getString("name"));
		event.setEventDateTime(rs.getTimestamp("event_datetime").toLocalDateTime());
		event.setDescription(rs.getString("description"));
		event.setStatus(rs.getString("status"));

		// Check if the 'signup_status' column exists in the result set before accessing
		// it
		if (hasColumn(rs, "signup_status")) {
			event.setUserAttendanceStatus(rs.getString("signup_status"));
		}

		return event;
	}

	/**
	 * Helper to check if a ResultSet contains a certain column.
	 * 
	 * @param rs         The ResultSet.
	 * @param columnName The column name to check.
	 * @return true if the column exists, false otherwise.
	 */
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columns = rsmd.getColumnCount();
		for (int x = 1; x <= columns; x++) {
			if (columnName.equals(rsmd.getColumnName(x))) {
				return true;
			}
		}
		return false;
	}

	// --- CRUD Methods for Admin ---

	/**
	 * Retrieves a single event by its ID.
	 * 
	 * @param eventId The ID of the event.
	 * @return An Event object, or null if not found.
	 */
	public Event getEventById(int eventId) {
		logger.debug("Fetching event by ID: {}", eventId);
		String sql = "SELECT * FROM events WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					logger.info("Found event with ID: {}", eventId);
					return mapResultSetToEvent(rs);
				} else {
					logger.warn("No event found with ID: {}", eventId);
					return null;
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching event with ID: {}", eventId, e);
			return null;
		}
	}

	/**
	 * Retrieves a list of all events, typically for an admin view.
	 * 
	 * @return A list of all Event objects.
	 */
	public List<Event> getAllEvents() {
		logger.debug("Fetching all events from database.");
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events ORDER BY event_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				events.add(mapResultSetToEvent(rs));
			}
			logger.info("Fetched {} total events.", events.size());
		} catch (SQLException e) {
			logger.error("SQL error while fetching all events.", e);
		}
		return events;
	}

	/**
	 * Creates a new event in the database.
	 * 
	 * @param event The Event object to create.
	 * @return true if creation was successful, false otherwise.
	 */
	public boolean createEvent(Event event) {
		logger.info("Creating new event: {}", event.getName());
		String sql = "INSERT INTO events (name, event_datetime, description, status) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			pstmt.setString(3, event.getDescription());
			pstmt.setString(4, "GEPLANT"); // Default status on creation

			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			logger.error("SQL error while creating event '{}'.", event.getName(), e);
			return false;
		}
	}

	/**
	 * Updates an existing event in the database.
	 * 
	 * @param event The Event object with updated data.
	 * @return true if update was successful, false otherwise.
	 */
	public boolean updateEvent(Event event) {
		logger.info("Updating event with ID: {}", event.getId());
		String sql = "UPDATE events SET name = ?, event_datetime = ?, description = ?, status = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			pstmt.setString(3, event.getDescription());
			pstmt.setString(4, event.getStatus());
			pstmt.setInt(5, event.getId());

			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			logger.error("SQL error while updating event with ID: {}", event.getId(), e);
			return false;
		}
	}

	/**
	 * Deletes an event from the database.
	 * 
	 * @param eventId The ID of the event to delete.
	 * @return true if deletion was successful, false otherwise.
	 */
	public boolean deleteEvent(int eventId) {
		logger.warn("Attempting to delete event with ID: {}", eventId);
		String sql = "DELETE FROM events WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, eventId);

			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			logger.error("SQL error while deleting event with ID: {}", eventId, e);
			return false;
		}
	}

	// --- Existing Methods (getUpcomingEventsForUser, signUpForEvent,
	// signOffFromEvent) ---
	// These methods remain as they were in the previous step.
	// Make sure they also use the mapResultSetToEvent helper where applicable.

	public List<Event> getUpcomingEventsForUser(User user, int limit) {
		// ... (The complex query from the previous step)
		// Inside the while(rs.next()) loop, use this:
		// events.add(mapResultSetToEvent(rs));
		// This keeps the code consistent.
		logger.debug("Fetching qualified upcoming events for user ID: {} with limit: {}", user.getId(), limit);
		List<Event> events = new ArrayList<>();

		String sql = "SELECT e.*, ea.signup_status FROM events e "
				+ "LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = ? "
				+ "WHERE e.event_datetime >= NOW() " + "AND ("
				+ "  NOT EXISTS (SELECT 1 FROM event_skill_requirements esr WHERE esr.event_id = e.id)" + "  OR "
				+ "  EXISTS (" + "    SELECT 1 FROM event_skill_requirements esr "
				+ "    JOIN user_qualifications uq ON esr.required_course_id = uq.course_id "
				+ "    WHERE esr.event_id = e.id AND uq.user_id = ?" + "  )" + ") " + "ORDER BY e.event_datetime ASC"
				+ (limit > 0 ? " LIMIT ?" : "");

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, user.getId());
			pstmt.setInt(2, user.getId());
			if (limit > 0) {
				pstmt.setInt(3, limit);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					events.add(mapResultSetToEvent(rs));
				}
				logger.info("Found {} qualified upcoming events for user ID: {}", events.size(), user.getId());
			}
		} catch (SQLException e) {
			logger.error("SQL error while fetching qualified upcoming events for user ID: {}", user.getId(), e);
		}
		return events;
	}

	/**
	 * Signs a user up for an event or updates their status if they already exist.
	 * Uses ON DUPLICATE KEY UPDATE for an atomic operation.
	 * 
	 * @param userId  The user's ID.
	 * @param eventId The event's ID.
	 * @return true if successful.
	 */
	public boolean signUpForEvent(int userId, int eventId) {
		logger.info("User {} signing up for event {}", userId, eventId);
		String sql = "INSERT INTO event_attendance (user_id, event_id, signup_status) VALUES (?, ?, 'ANGEMELDET') "
				+ "ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			logger.error("SQL error during event sign-up for user {} and event {}", userId, eventId, e);
			return false;
		}
	}

	/**
	 * Signs a user off from an event.
	 * 
	 * @param userId  The user's ID.
	 * @param eventId The event's ID.
	 * @return true if successful.
	 */
	public boolean signOffFromEvent(int userId, int eventId) {
		logger.info("User {} signing off from event {}", userId, eventId);
		String sql = "UPDATE event_attendance SET signup_status = 'ABGEMELDET' WHERE user_id = ? AND event_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			int rowsAffected = pstmt.executeUpdate();
			return rowsAffected > 0;
		} catch (SQLException e) {
			logger.error("SQL error during event sign-off for user {} and event {}", userId, eventId, e);
			return false;
		}
	}

	public List<User> getSignedUpUsersForEvent(int eventId) {
		logger.debug("Fetching all signed-up users for event ID: {}", eventId);
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u " + "JOIN event_attendance ea ON u.id = ea.user_id "
				+ "WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					// Using the shorter constructor from the User model
					users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching signed-up users for event ID: {}", eventId, e);
		}
		return users;
	}

	public List<User> getAssignedUsersForEvent(int eventId) {
		logger.debug("Fetching assigned users for event ID: {}", eventId);
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u "
				+ "JOIN event_assignments ea ON u.id = ea.user_id WHERE ea.event_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(new User(rs.getInt("id"), rs.getString("username"), rs.getString("role")));
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching assigned users for event ID: {}", eventId, e);
		}
		return users;
	}

	public void assignUsersToEvent(int eventId, String[] userIds) {
		logger.info("Assigning users to event ID: {}", eventId);
		String deleteSql = "DELETE FROM event_assignments WHERE event_id = ?";
		String insertSql = "INSERT INTO event_assignments (event_id, user_id) VALUES (?, ?)";

		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false); // Start transaction

			// 1. Clear existing assignments
			try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
				deletePstmt.setInt(1, eventId);
				deletePstmt.executeUpdate();
			}

			// 2. Insert new assignments
			if (userIds != null) {
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
			logger.info("Successfully assigned {} users to event {}", (userIds != null ? userIds.length : 0), eventId);
		} catch (SQLException e) {
			logger.error("SQL transaction error during user assignment for event ID: {}", eventId, e);
			// In a real app, you would handle the rollback here.
		}
	}

	// Add to src/main/java/de/technikteam/dao/EventDAO.java
	public List<SkillRequirement> getSkillRequirementsForEvent(int eventId) {
		List<SkillRequirement> requirements = new ArrayList<>();
		String sql = "SELECT skill_name, required_persons FROM event_skill_requirements WHERE event_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					SkillRequirement req = new SkillRequirement();
					req.setSkillName(rs.getString("skill_name"));
					req.setRequiredPersons(rs.getInt("required_persons"));
					requirements.add(req);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching skill requirements for event ID: {}", eventId, e);
		}
		return requirements;
	}

	// Fügen Sie diese Methode zu Ihrer bestehenden EventDAO-Klasse hinzu.

	public List<Event> getEventHistoryForUser(int userId) {
		List<Event> history = new ArrayList<>();
		// Diese Abfrage holt alle Events, für die ein Nutzer einen Teilnahmestatus hat.
		String sql = "SELECT e.id, e.name, e.event_datetime, e.status, ea.signup_status "
				+ "FROM events e JOIN event_attendance ea ON e.id = ea.event_id "
				+ "WHERE ea.user_id = ? ORDER BY e.event_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Event event = new Event();
				event.setId(rs.getInt("id"));
				event.setName(rs.getString("name"));
				event.setEventDateTime(rs.getTimestamp("event_datetime").toLocalDateTime());
				event.setStatus(rs.getString("status"));
				event.setUserAttendanceStatus(rs.getString("signup_status")); // Hier wird der Status des Nutzers
																				// gespeichert
				history.add(event);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching event history for user {}", userId, e);
		}
		return history;
	}
}