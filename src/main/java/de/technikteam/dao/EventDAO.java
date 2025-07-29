package de.technikteam.dao;

import de.technikteam.model.Event;
import de.technikteam.model.SkillRequirement;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.util.DaoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Objects;

@Repository
public class EventDAO {
	private static final Logger logger = LogManager.getLogger(EventDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private final RowMapper<Event> eventRowMapper = (rs, rowNum) -> {
		Event event = new Event();
		event.setId(rs.getInt("id"));
		event.setName(rs.getString("name"));
		event.setDescription(rs.getString("description"));
		event.setLocation(rs.getString("location"));
		event.setStatus(rs.getString("status"));
		event.setEventDateTime(rs.getTimestamp("event_datetime").toLocalDateTime());
		if (rs.getTimestamp("end_datetime") != null) {
			event.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
		}
		if (DaoUtils.hasColumn(rs, "leader_user_id")) {
			event.setLeaderUserId(rs.getInt("leader_user_id"));
		}
		if (DaoUtils.hasColumn(rs, "leader_username")) {
			event.setLeaderUsername(rs.getString("leader_username"));
		}
		return event;
	};

	private final RowMapper<User> simpleUserRowMapper = (rs, rowNum) -> new User(rs.getInt("id"),
			rs.getString("username"), rs.getString("role"));

	public List<Event> getEventHistoryForUser(int userId) {
		String sql = "SELECT e.*, COALESCE( (SELECT 'ZUGEWIESEN' FROM event_assignments WHERE event_id = e.id AND user_id = ?), (SELECT signup_status FROM event_attendance WHERE event_id = e.id AND user_id = ?), 'OFFEN' ) AS user_status FROM events e WHERE EXISTS ( SELECT 1 FROM event_attendance WHERE event_id = e.id AND user_id = ? UNION SELECT 1 FROM event_assignments WHERE event_id = e.id AND user_id = ? ) ORDER BY e.event_datetime DESC";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				Event event = eventRowMapper.mapRow(rs, rowNum);
				event.setUserAttendanceStatus(rs.getString("user_status"));
				return event;
			}, userId, userId, userId, userId);
		} catch (Exception e) {
			logger.error("Error fetching event history for user {}", userId, e);
			return List.of();
		}
	}

	public Event getEventById(int eventId) {
		String sql = "SELECT e.*, u.username as leader_username FROM events e LEFT JOIN users u ON e.leader_user_id = u.id WHERE e.id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, eventRowMapper, eventId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (Exception e) {
			logger.error("Error fetching event by ID: {}", eventId, e);
			return null;
		}
	}

	public List<Event> getAllEvents() {
		String sql = "SELECT e.*, u.username as leader_username FROM events e LEFT JOIN users u ON e.leader_user_id = u.id ORDER BY e.event_datetime DESC";
		try {
			return jdbcTemplate.query(sql, eventRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching all events.", e);
			return List.of();
		}
	}

	public List<Event> getActiveEvents() {
		String sql = "SELECT * FROM events WHERE status IN ('GEPLANT', 'KOMPLETT', 'LAUFEND') ORDER BY event_datetime ASC";
		try {
			return jdbcTemplate.query(sql, eventRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching active events.", e);
			return List.of();
		}
	}

	public int createEvent(Event event) {
		String sql = "INSERT INTO events (name, event_datetime, end_datetime, description, location, status, leader_user_id) VALUES (?, ?, ?, ?, ?, 'GEPLANT', ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		try {
			jdbcTemplate.update(connection -> {
				PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				ps.setString(1, event.getName());
				ps.setTimestamp(2, Timestamp.valueOf(event.getEventDateTime()));
				if (event.getEndDateTime() != null)
					ps.setTimestamp(3, Timestamp.valueOf(event.getEndDateTime()));
				else
					ps.setNull(3, Types.TIMESTAMP);
				ps.setString(4, event.getDescription());
				ps.setString(5, event.getLocation());
				if (event.getLeaderUserId() > 0)
					ps.setInt(6, event.getLeaderUserId());
				else
					ps.setNull(6, Types.INTEGER);
				return ps;
			}, keyHolder);
			return Objects.requireNonNull(keyHolder.getKey()).intValue();
		} catch (Exception e) {
			logger.error("Error creating event {}", event.getName(), e);
			return 0;
		}
	}

	public boolean updateEvent(Event event) {
		String sql = "UPDATE events SET name = ?, event_datetime = ?, end_datetime = ?, description = ?, location = ?, status = ?, leader_user_id = ? WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, event.getName(), Timestamp.valueOf(event.getEventDateTime()),
					event.getEndDateTime() != null ? Timestamp.valueOf(event.getEndDateTime()) : null,
					event.getDescription(), event.getLocation(), event.getStatus(),
					event.getLeaderUserId() > 0 ? event.getLeaderUserId() : null, event.getId()) > 0;
		} catch (Exception e) {
			logger.error("Error updating event {}", event.getName(), e);
			return false;
		}
	}

	public boolean deleteEvent(int eventId) {
		String sql = "DELETE FROM events WHERE id = ?";
		try {
			return jdbcTemplate.update(sql, eventId) > 0;
		} catch (Exception e) {
			logger.error("Error deleting event with ID: {}", eventId, e);
			return false;
		}
	}

	public void signUpForEvent(int userId, int eventId) {
		String sql = "INSERT INTO event_attendance (user_id, event_id, signup_status, commitment_status) VALUES (?, ?, 'ANGEMELDET', 'OFFEN') ON DUPLICATE KEY UPDATE signup_status = 'ANGEMELDET'";
		try {
			jdbcTemplate.update(sql, userId, eventId);
		} catch (Exception e) {
			logger.error("Error during event sign-up for user {} and event {}", userId, eventId, e);
		}
	}

	public void signOffFromEvent(int userId, int eventId) {
		String sql = "UPDATE event_attendance SET signup_status = 'ABGEMELDET', commitment_status = 'OFFEN' WHERE user_id = ? AND event_id = ?";
		try {
			jdbcTemplate.update(sql, userId, eventId);
		} catch (Exception e) {
			logger.error("Error during event sign-off for user {} and event {}", userId, eventId, e);
		}
	}

	public void assignUsersToEvent(int eventId, String[] userIds) {
		try {
			jdbcTemplate.update("DELETE FROM event_assignments WHERE event_id = ?", eventId);
			if (userIds != null && userIds.length > 0) {
				String insertSql = "INSERT INTO event_assignments (event_id, user_id) VALUES (?, ?)";
				jdbcTemplate.batchUpdate(insertSql, List.of(userIds), 100, (ps, userId) -> {
					ps.setInt(1, eventId);
					ps.setInt(2, Integer.parseInt(userId));
				});
			}
		} catch (Exception e) {
			logger.error("Error during user assignment for event ID: {}.", eventId, e);
		}
	}

	public List<SkillRequirement> getSkillRequirementsForEvent(int eventId) {
		String sql = "SELECT esr.required_course_id, c.name as course_name, esr.required_persons FROM event_skill_requirements esr JOIN courses c ON esr.required_course_id = c.id WHERE esr.event_id = ?";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				SkillRequirement req = new SkillRequirement();
				req.setRequiredCourseId(rs.getInt("required_course_id"));
				req.setCourseName(rs.getString("course_name"));
				req.setRequiredPersons(rs.getInt("required_persons"));
				return req;
			}, eventId);
		} catch (Exception e) {
			logger.error("Error fetching skill requirements for event ID: {}", eventId, e);
			return List.of();
		}
	}

	public void saveSkillRequirements(int eventId, String[] requiredCourseIds, String[] requiredPersons) {
		try {
			jdbcTemplate.update("DELETE FROM event_skill_requirements WHERE event_id = ?", eventId);
			if (requiredCourseIds != null && requiredPersons != null
					&& requiredCourseIds.length == requiredPersons.length) {
				String sql = "INSERT INTO event_skill_requirements (event_id, required_course_id, required_persons) VALUES (?, ?, ?)";
				jdbcTemplate.batchUpdate(sql, List.of(requiredCourseIds), 100, (ps, courseId) -> {
					// This is a bit clumsy but necessary to align arrays
					int index = List.of(requiredCourseIds).indexOf(courseId);
					if (!courseId.isEmpty()) {
						ps.setInt(1, eventId);
						ps.setInt(2, Integer.parseInt(courseId));
						ps.setInt(3, Integer.parseInt(requiredPersons[index]));
					}
				});
			}
		} catch (Exception e) {
			logger.error("Error saving skill requirements for event ID: {}.", eventId, e);
		}
	}

	public void saveReservations(int eventId, String[] itemIds, String[] quantities) {
		try {
			jdbcTemplate.update("DELETE FROM event_storage_reservations WHERE event_id = ?", eventId);
			if (itemIds != null && quantities != null && itemIds.length == quantities.length) {
				String sql = "INSERT INTO event_storage_reservations (event_id, item_id, reserved_quantity) VALUES (?, ?, ?)";
				jdbcTemplate.batchUpdate(sql, List.of(itemIds), 100, (ps, itemId) -> {
					int index = List.of(itemIds).indexOf(itemId);
					if (!itemId.isEmpty()) {
						ps.setInt(1, eventId);
						ps.setInt(2, Integer.parseInt(itemId));
						ps.setInt(3, Integer.parseInt(quantities[index]));
					}
				});
			}
		} catch (Exception e) {
			logger.error("Error saving reservations for event ID: {}.", eventId, e);
		}
	}

	public List<StorageItem> getReservedItemsForEvent(int eventId) {
		String sql = "SELECT si.id, si.name, esr.reserved_quantity FROM event_storage_reservations esr JOIN storage_items si ON esr.item_id = si.id WHERE esr.event_id = ?";
		try {
			return jdbcTemplate.query(sql, (rs, rowNum) -> {
				StorageItem item = new StorageItem();
				item.setId(rs.getInt("id"));
				item.setName(rs.getString("name"));
				item.setQuantity(rs.getInt("reserved_quantity"));
				return item;
			}, eventId);
		} catch (Exception e) {
			logger.error("Error fetching reserved items for event ID: {}", eventId, e);
			return List.of();
		}
	}

	public List<Event> getAllActiveAndUpcomingEvents() {
		String sql = "SELECT * FROM events WHERE status NOT IN ('ABGESCHLOSSEN', 'ABGESAGT') AND event_datetime >= NOW() - INTERVAL 1 DAY ORDER BY event_datetime ASC";
		try {
			return jdbcTemplate.query(sql, eventRowMapper);
		} catch (Exception e) {
			logger.error("Error fetching active/upcoming events for calendar.", e);
			return List.of();
		}
	}

	public boolean isUserAssociatedWithEvent(int eventId, int userId) {
		String sql = "SELECT COUNT(*) FROM (SELECT 1 FROM event_attendance WHERE event_id = ? AND user_id = ? AND signup_status = 'ANGEMELDET' UNION ALL SELECT 1 FROM event_assignments WHERE event_id = ? AND user_id = ?) AS combined";
		try {
			Integer count = jdbcTemplate.queryForObject(sql, Integer.class, eventId, userId, eventId, userId);
			return count != null && count > 0;
		} catch (Exception e) {
			logger.error("Error checking user association for event {} and user {}", eventId, userId, e);
			return false;
		}
	}

	public List<User> getAssignedUsersForEvent(int eventId) {
		String sql = "SELECT u.id, u.username, r.role_name AS role FROM users u JOIN event_assignments ea ON u.id = ea.user_id LEFT JOIN roles r ON u.role_id = r.id WHERE ea.event_id = ?";
		try {
			return jdbcTemplate.query(sql, simpleUserRowMapper, eventId);
		} catch (Exception e) {
			logger.error("Error fetching assigned users for event ID: {}", eventId, e);
			return List.of();
		}
	}

	public List<Event> getCompletedEventsForUser(int userId) {
		String sql = "SELECT e.* FROM events e JOIN event_assignments ea ON e.id = ea.event_id WHERE ea.user_id = ? AND e.status = 'ABGESCHLOSSEN' ORDER BY e.event_datetime DESC";
		try {
			return jdbcTemplate.query(sql, eventRowMapper, userId);
		} catch (Exception e) {
			logger.error("Error fetching completed event history for user {}", userId, e);
			return List.of();
		}
	}

	public List<Event> getUpcomingEvents(int limit) {
		String sql = "SELECT * FROM events WHERE event_datetime > NOW() ORDER BY event_datetime ASC LIMIT ?";
		try {
			return jdbcTemplate.query(sql, eventRowMapper, limit);
		} catch (Exception e) {
			logger.error("Error fetching upcoming events with limit {}", limit, e);
			return List.of();
		}
	}
}