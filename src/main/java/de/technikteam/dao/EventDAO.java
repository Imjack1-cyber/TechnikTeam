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
import de.technikteam.model.EventAttendance;
import de.technikteam.model.SkillRequirement;
import de.technikteam.model.User;

/**
 * A comprehensive DAO for all database operations related to events. It handles
 * creating, reading, updating, and deleting events. Additionally, it manages
 * user sign-ups/sign-offs, event skill requirements, and the final assignment
 * of users to an event. It contains methods to supply data for both regular
 * user views (like upcoming events) and administrative back-end pages.
 */
public class EventDAO {
	private static final Logger logger = LogManager.getLogger(EventDAO.class);

	// --- Private Helper Methods ---

	private Event mapResultSetToEvent(ResultSet rs) throws SQLException {
		Event event = new Event();
		event.setId(rs.getInt("id"));
		event.setName(rs.getString("name"));
		event.setEventDateTime(rs.getTimestamp("event_datetime").toLocalDateTime());
		event.setDescription(rs.getString("description"));
		event.setStatus(rs.getString("status"));
		return event;
	}

	public void setAttendanceCommitment(int eventId, int userId, String commitment) {
		String sql = "UPDATE event_attendance SET commitment_status = ? WHERE event_id = ? AND user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, commitment);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, userId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			/* ... */ }
	}

	private User mapResultSetToSimpleUser(ResultSet rs) throws SQLException {
		return new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
	}

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

	public List<Event> getEventHistoryForUser(int userId) {
		List<Event> history = new ArrayList<>();
		String sql = "SELECT e.*, ea.signup_status FROM events e "
				+ "JOIN event_attendance ea ON e.id = ea.event_id WHERE ea.user_id = ? ORDER BY e.event_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				Event event = mapResultSetToEvent(rs);
				event.setUserAttendanceStatus(rs.getString("signup_status"));
				history.add(event);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching event history for user {}", userId, e);
		}
		return history;
	}

	// --- Methods for Admin Views & CRUD ---

	public Event getEventById(int eventId) {
		String sql = "SELECT * FROM events WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return mapResultSetToEvent(rs);
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching event by ID: {}", eventId, e);
		}
		return null;
	}

	public List<Event> getAllEvents() {
		List<Event> events = new ArrayList<>();
		String sql = "SELECT * FROM events ORDER BY event_datetime DESC";
		try (Connection conn = DatabaseManager.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				events.add(mapResultSetToEvent(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching all events.", e);
		}
		return events;
	}

	public int createEvent(Event event) {
		String sql = "INSERT INTO events (name, event_datetime, description, status) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			pstmt.setString(3, event.getDescription());
			pstmt.setString(4, "GEPLANT");
			if (pstmt.executeUpdate() > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next())
						return generatedKeys.getInt(1);
				}
			}
		} catch (SQLException e) {
			logger.error("SQL error creating event.", e);
		}
		return 0;
	}

	public boolean updateEvent(Event event) {
		String sql = "UPDATE events SET name = ?, event_datetime = ?, description = ?, status = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, event.getName());
			pstmt.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
			pstmt.setString(3, event.getDescription());
			pstmt.setString(4, event.getStatus());
			pstmt.setInt(5, event.getId());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error updating event with ID: {}", event.getId(), e);
		}
		return false;
	}

	public boolean deleteEvent(int eventId) {
		String sql = "DELETE FROM events WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error deleting event with ID: {}", eventId, e);
		}
		return false;
	}

	// --- Methods for User Actions & Admin Management ---

	public void signUpForEvent(int userId, int eventId) {
		String sql = "INSERT INTO event_attendance (user_id, event_id, signup_status, commitment_status) VALUES (?, ?, 'ANGEMELDET', 'OFFEN') ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL error during event sign-up", e);
		}
	}

	public void signOffFromEvent(int userId, int eventId) {
		String sql = "UPDATE event_attendance SET signup_status = 'ABGEMELDET', commitment_status = 'OFFEN' WHERE user_id = ? AND event_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, eventId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL error during event sign-off", e);
		}
	}

	public List<User> getSignedUpUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u JOIN event_attendance ea ON u.id = ea.user_id WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next())
					users.add(mapResultSetToSimpleUser(rs));
			}
		} catch (SQLException e) {
			logger.error("SQL error fetching signed-up users for event ID: {}", eventId, e);
		}
		return users;
	}

	public List<EventAttendance> getAttendanceDetailsForEvent(int eventId) {
		List<EventAttendance> attendances = new ArrayList<>();
		String sql = "SELECT u.id, u.username, ea.signup_status, ea.commitment_status FROM event_attendance ea JOIN users u ON ea.user_id = u.id WHERE ea.event_id = ? AND ea.signup_status = 'ANGEMELDET'";
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
		} catch (SQLException e) {
			logger.error("SQL error fetching attendance details for event ID: {}", eventId, e);
		}
		return attendances;
	}

	public boolean updateCommitmentStatus(int eventId, int userId, String status) {
		String sql = "UPDATE event_attendance SET commitment_status = ? WHERE event_id = ? AND user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, eventId);
			pstmt.setInt(3, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("SQL error while updating commitment status.", e);
		}
		return false;
	}

	public List<SkillRequirement> getSkillRequirementsForEvent(int eventId) {
		List<SkillRequirement> requirements = new ArrayList<>();
		String sql = "SELECT esr.required_course_id, c.name as course_name, esr.required_persons FROM event_skill_requirements esr JOIN courses c ON esr.required_course_id = c.id WHERE esr.event_id = ?";
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
		} catch (SQLException e) {
			logger.error("SQL error fetching skill requirements for event ID: {}", eventId, e);
		}
		return requirements;
	}

	public void saveSkillRequirements(int eventId, String[] requiredCourseIds, String[] requiredPersons) {
		String deleteSql = "DELETE FROM event_skill_requirements WHERE event_id = ?";
		String insertSql = "INSERT INTO event_skill_requirements (event_id, required_course_id, required_persons, skill_name) VALUES (?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false);
			try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
				deletePstmt.setInt(1, eventId);
				deletePstmt.executeUpdate();
			}
			if (requiredCourseIds != null && requiredPersons != null
					&& requiredCourseIds.length == requiredPersons.length) {
				try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
					for (int i = 0; i < requiredCourseIds.length; i++) {
						if (requiredCourseIds[i] == null || requiredCourseIds[i].isEmpty())
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
			conn.commit();
		} catch (SQLException | NumberFormatException e) {
			logger.error("Transaction error during saving skill requirements for event ID: {}.", eventId, e);
		}
	}

	/**
	 * Fetches a list of users who have been definitively assigned to an event.
	 * 
	 * @param eventId The ID of the event.
	 * @return A list of assigned User objects.
	 */
	public List<User> getAssignedUsersForEvent(int eventId) {
		List<User> users = new ArrayList<>();
		String sql = "SELECT u.id, u.username, u.role FROM users u "
				+ "JOIN event_assignments ea ON u.id = ea.user_id WHERE ea.event_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					users.add(mapResultSetToSimpleUser(rs)); // Assuming you have this helper method
				}
			}
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
	
	// In: src/main/java/de/technikteam/dao/EventDAO.java

	/**
	 * Fetches all upcoming events for a user, with a calculated status that
	 * prioritizes assignments over simple sign-ups.
	 * Status can be: ZUGEWIESEN, ANGEMELDET, ABGEMELDET, or OFFEN.
	 *
	 * @param user The currently logged-in user.
	 * @param limit The maximum number of events to return (0 for no limit).
	 * @return A list of upcoming Event objects with the correctly calculated user status.
	 */
	public List<Event> getUpcomingEventsForUser(User user, int limit) {
	    List<Event> events = new ArrayList<>();
	    
	    // THE NEW, MORE INTELLIGENT SQL QUERY
	    String sql = "SELECT e.*, " +
	                 "CASE " +
	                 "    WHEN eas.user_id IS NOT NULL THEN 'ZUGEWIESEN' " + // 1. Check for assignment first
	                 "    WHEN ea.signup_status IS NOT NULL THEN ea.signup_status " + // 2. Fall back to signup status
	                 "    ELSE 'OFFEN' " + // 3. Default to open
	                 "END AS calculated_user_status " +
	                 "FROM events e " +
	                 "LEFT JOIN event_attendance ea ON e.id = ea.event_id AND ea.user_id = ? " +
	                 "LEFT JOIN event_assignments eas ON e.id = eas.event_id AND eas.user_id = ? " +
	                 "WHERE e.event_datetime >= NOW() " +
	                 "AND (" + // Qualification check remains the same
	                 "  NOT EXISTS (SELECT 1 FROM event_skill_requirements esr WHERE esr.event_id = e.id) OR " +
	                 "  EXISTS (SELECT 1 FROM event_skill_requirements esr JOIN user_qualifications uq ON esr.required_course_id = uq.course_id WHERE esr.event_id = e.id AND uq.user_id = ?)" +
	                 ") " +
	                 "ORDER BY e.event_datetime ASC" +
	                 (limit > 0 ? " LIMIT ?" : "");

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
	        }
	    } catch (SQLException e) {
	        logger.error("SQL error fetching qualified upcoming events for user {}", user.getId(), e);
	    }
	    return events;
	}
}