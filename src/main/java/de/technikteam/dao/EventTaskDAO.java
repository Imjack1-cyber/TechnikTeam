package de.technikteam.dao;

import de.technikteam.model.EventTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing event-specific tasks in the `event_tasks`
 * table. It handles creating, assigning, updating status, and deleting tasks
 * associated with a "running" event.
 */
public class EventTaskDAO {
	private static final Logger logger = LogManager.getLogger(EventTaskDAO.class);

	/**
	 * Creates a new task for an event.
	 * 
	 * @param task The EventTask object to create.
	 * @return The ID of the newly created task, or 0 on failure.
	 */
	public int createTask(EventTask task) {
		String sql = "INSERT INTO event_tasks (event_id, description, status) VALUES (?, ?, 'OFFEN')";
		logger.debug("Creating new task '{}' for event ID {}", task.getDescription(), task.getEventId());
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setInt(1, task.getEventId());
			pstmt.setString(2, task.getDescription());
			if (pstmt.executeUpdate() > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						int taskId = rs.getInt(1);
						logger.info("Created task '{}' with ID {} for event {}", task.getDescription(), taskId,
								task.getEventId());
						return taskId;
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error creating task for event {}", task.getEventId(), e);
		}
		return 0;
	}

	/**
	 * Assigns a task to one or more users. This is a transactional operation that
	 * first clears all existing assignments for the task and then adds the new
	 * ones.
	 * 
	 * @param taskId  The ID of the task.
	 * @param userIds The array of user IDs to assign to the task.
	 */
	public void assignTaskToUsers(int taskId, int[] userIds) {
		// Transactional: clear old assignments, add new ones.
		String deleteSql = "DELETE FROM event_task_assignments WHERE task_id = ?";
		String insertSql = "INSERT INTO event_task_assignments (task_id, user_id) VALUES (?, ?)";
		logger.debug("Assigning task ID {} to {} users.", taskId, userIds != null ? userIds.length : 0);
		try (Connection conn = DatabaseManager.getConnection()) {
			conn.setAutoCommit(false); // Start transaction
			// 1. Delete old assignments
			try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
				deleteStmt.setInt(1, taskId);
				deleteStmt.executeUpdate();
			}
			// 2. Insert new assignments
			if (userIds != null && userIds.length > 0) {
				try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
					for (int userId : userIds) {
						insertStmt.setInt(1, taskId);
						insertStmt.setInt(2, userId);
						insertStmt.addBatch();
					}
					insertStmt.executeBatch();
				}
			}
			conn.commit(); // Commit transaction
			logger.info("Successfully assigned task {} to {} users.", taskId, userIds != null ? userIds.length : 0);
		} catch (SQLException e) {
			logger.error("Error during transaction for assigning task {}", taskId, e);
			// Consider rollback logic here if connection is not auto-closed with
			// try-with-resources
		}
	}

	/**
	 * Updates the status of a task (e.g., from "OFFEN" to "ERLEDIGT").
	 * 
	 * @param taskId The ID of the task.
	 * @param status The new status string.
	 * @return true if the update was successful.
	 */
	public boolean updateTaskStatus(int taskId, String status) {
		String sql = "UPDATE event_tasks SET status = ? WHERE id = ?";
		logger.debug("Updating status for task ID {} to '{}'", taskId, status);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for task {}", taskId, e);
			return false;
		}
	}

	/**
	 * Deletes a task and its assignments (due to database foreign key constraints).
	 * 
	 * @param taskId The ID of the task to delete.
	 * @return true if successful.
	 */
	public boolean deleteTask(int taskId) {
		String sql = "DELETE FROM event_tasks WHERE id = ?";
		logger.warn("Attempting to delete task with ID: {}", taskId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting task {}", taskId, e);
			return false;
		}
	}

	/**
	 * Fetches all tasks for a given event, including a comma-separated list of
	 * usernames of assigned users for easy display.
	 * 
	 * @param eventId The event's ID.
	 * @return A list of EventTask objects.
	 */
	public List<EventTask> getTasksForEvent(int eventId) {
		List<EventTask> tasks = new ArrayList<>();
		// This query uses GROUP_CONCAT to aggregate assigned usernames into a single
		// string
		String sql = "SELECT t.id, t.event_id, t.description, t.status, "
				+ "GROUP_CONCAT(u.username SEPARATOR ', ') as assigned_usernames " + "FROM event_tasks t "
				+ "LEFT JOIN event_task_assignments ta ON t.id = ta.task_id "
				+ "LEFT JOIN users u ON ta.user_id = u.id " + "WHERE t.event_id = ? "
				+ "GROUP BY t.id, t.event_id, t.description, t.status " + "ORDER BY t.id";
		logger.debug("Fetching all tasks for event ID: {}", eventId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventTask task = new EventTask();
					task.setId(rs.getInt("id"));
					task.setEventId(rs.getInt("event_id"));
					task.setDescription(rs.getString("description"));
					task.setStatus(rs.getString("status"));
					task.setAssignedUsernames(rs.getString("assigned_usernames"));
					tasks.add(task);
				}
				logger.info("Found {} tasks for event ID: {}", tasks.size(), eventId);
			}
		} catch (SQLException e) {
			logger.error("Error fetching tasks for event {}", eventId, e);
		}
		return tasks;
	}
}