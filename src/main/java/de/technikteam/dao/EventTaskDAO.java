package de.technikteam.dao;

import de.technikteam.model.EventTask;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventTaskDAO {
	private static final Logger logger = LogManager.getLogger(EventTaskDAO.class);

	public int saveTask(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities, String[] kitIds) {
		boolean isUpdate = task.getId() > 0;
		String taskSql = isUpdate
				? "UPDATE event_tasks SET description = ?, details = ?, status = ?, display_order = ?, required_persons = ? WHERE id = ?"
				: "INSERT INTO event_tasks (event_id, description, details, status, display_order, required_persons) VALUES (?, ?, ?, 'OFFEN', ?, ?)";

		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement pstmt = conn.prepareStatement(taskSql, Statement.RETURN_GENERATED_KEYS)) {
				if (isUpdate) {
					pstmt.setString(1, task.getDescription());
					pstmt.setString(2, task.getDetails());
					pstmt.setString(3, task.getStatus());
					pstmt.setInt(4, task.getDisplayOrder());
					pstmt.setInt(5, task.getRequiredPersons());
					pstmt.setInt(6, task.getId());
					pstmt.executeUpdate();
				} else {
					pstmt.setInt(1, task.getEventId());
					pstmt.setString(2, task.getDescription());
					pstmt.setString(3, task.getDetails());
					pstmt.setInt(4, task.getDisplayOrder());
					pstmt.setInt(5, task.getRequiredPersons());
					pstmt.executeUpdate();
					try (ResultSet rs = pstmt.getGeneratedKeys()) {
						if (rs.next()) {
							task.setId(rs.getInt(1));
						}
					}
				}
			}

			int taskId = task.getId();
			if (taskId == 0)
				throw new SQLException("Failed to create task, no ID obtained.");

			clearAssociations(conn, taskId);
			saveUserAssignments(conn, taskId, userIds);
			saveItemRequirements(conn, taskId, itemIds, itemQuantities);
			saveKitRequirements(conn, taskId, kitIds);

			conn.commit();
			logger.info("Successfully saved task ID {}", taskId);
			return taskId;

		} catch (SQLException | NumberFormatException e) {
			logger.error("Error saving task transaction. Rolling back.", e);
			if (conn != null)
				try {
					conn.rollback();
				} catch (SQLException ex) {
					logger.error("Rollback failed.", ex);
				}
			return 0;
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

	private void clearAssociations(Connection conn, int taskId) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			stmt.addBatch("DELETE FROM event_task_assignments WHERE task_id = " + taskId);
			stmt.addBatch("DELETE FROM event_task_storage_items WHERE task_id = " + taskId);
			stmt.addBatch("DELETE FROM event_task_kits WHERE task_id = " + taskId);
			stmt.executeBatch();
		}
	}

	private void saveUserAssignments(Connection conn, int taskId, int[] userIds) throws SQLException {
		if (userIds != null && userIds.length > 0) {
			String sql = "INSERT INTO event_task_assignments (task_id, user_id) VALUES (?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				for (int userId : userIds) {
					pstmt.setInt(1, taskId);
					pstmt.setInt(2, userId);
					pstmt.addBatch();
				}
				pstmt.executeBatch();
			}
		}
	}

	private void saveItemRequirements(Connection conn, int taskId, String[] itemIds, String[] itemQuantities)
			throws SQLException, NumberFormatException {
		if (itemIds != null && itemQuantities != null && itemIds.length == itemQuantities.length) {
			String sql = "INSERT INTO event_task_storage_items (task_id, item_id, quantity) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				for (int i = 0; i < itemIds.length; i++) {
					if (!itemIds[i].isEmpty()) {
						pstmt.setInt(1, taskId);
						pstmt.setInt(2, Integer.parseInt(itemIds[i]));
						pstmt.setInt(3, Integer.parseInt(itemQuantities[i]));
						pstmt.addBatch();
					}
				}
				pstmt.executeBatch();
			}
		}
	}

	private void saveKitRequirements(Connection conn, int taskId, String[] kitIds)
			throws SQLException, NumberFormatException {
		if (kitIds != null && kitIds.length > 0) {
			String sql = "INSERT INTO event_task_kits (task_id, kit_id) VALUES (?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				for (String kitId : kitIds) {
					if (!kitId.isEmpty()) {
						pstmt.setInt(1, taskId);
						pstmt.setInt(2, Integer.parseInt(kitId));
						pstmt.addBatch();
					}
				}
				pstmt.executeBatch();
			}
		}
	}

	public List<EventTask> getTasksForEvent(int eventId) {
		Map<Integer, EventTask> tasksById = new HashMap<>();
		String sql = "SELECT t.* FROM event_tasks t WHERE t.event_id = ? ORDER BY t.display_order ASC, t.id ASC";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventTask task = new EventTask();
					task.setId(rs.getInt("id"));
					task.setEventId(rs.getInt("event_id"));
					task.setDescription(rs.getString("description"));
					task.setDetails(rs.getString("details"));
					task.setStatus(rs.getString("status"));
					task.setDisplayOrder(rs.getInt("display_order"));
					task.setRequiredPersons(rs.getInt("required_persons"));
					task.setAssignedUsers(new ArrayList<>());
					task.setRequiredItems(new ArrayList<>());
					task.setRequiredKits(new ArrayList<>());
					tasksById.put(task.getId(), task);
				}
			}
			if (!tasksById.isEmpty()) {
				fetchTaskAssociations(conn, tasksById);
			}
		} catch (SQLException e) {
			logger.error("Error fetching tasks for event {}", eventId, e);
		}
		return new ArrayList<>(tasksById.values());
	}

	private void fetchTaskAssociations(Connection conn, Map<Integer, EventTask> tasksById) throws SQLException {
		String taskIds = tasksById.keySet().stream().map(String::valueOf).collect(Collectors.joining(","));
		String userSql = "SELECT ta.task_id, u.id, u.username FROM event_task_assignments ta JOIN users u ON ta.user_id = u.id WHERE ta.task_id IN ("
				+ taskIds + ")";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(userSql)) {
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setUsername(rs.getString("username"));
				tasksById.get(rs.getInt("task_id")).getAssignedUsers().add(user);
			}
		}
		String itemSql = "SELECT tsi.task_id, si.id, si.name, tsi.quantity FROM event_task_storage_items tsi JOIN storage_items si ON tsi.item_id = si.id WHERE tsi.task_id IN ("
				+ taskIds + ")";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(itemSql)) {
			while (rs.next()) {
				StorageItem item = new StorageItem();
				item.setId(rs.getInt("id"));
				item.setName(rs.getString("name"));
				item.setQuantity(rs.getInt("quantity"));
				tasksById.get(rs.getInt("task_id")).getRequiredItems().add(item);
			}
		}
		String kitSql = "SELECT tk.task_id, ik.id, ik.name FROM event_task_kits tk JOIN inventory_kits ik ON tk.kit_id = ik.id WHERE tk.task_id IN ("
				+ taskIds + ")";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(kitSql)) {
			while (rs.next()) {
				InventoryKit kit = new InventoryKit();
				kit.setId(rs.getInt("id"));
				kit.setName(rs.getString("name"));
				tasksById.get(rs.getInt("task_id")).getRequiredKits().add(kit);
			}
		}
	}

	public boolean deleteTask(int taskId) {
		String sql = "DELETE FROM event_tasks WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting task {}", taskId, e);
			return false;
		}
	}

	public boolean updateTaskStatus(int taskId, String status) {
		String sql = "UPDATE event_tasks SET status = ? WHERE id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for task {}", taskId, e);
			return false;
		}
	}

	public boolean claimTask(int taskId, int userId) {
		String sql = "INSERT INTO event_task_assignments (task_id, user_id) " + "SELECT ?, ? FROM event_tasks "
				+ "WHERE id = ? AND required_persons > (SELECT COUNT(*) FROM event_task_assignments WHERE task_id = ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			pstmt.setInt(2, userId);
			pstmt.setInt(3, taskId);
			pstmt.setInt(4, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				logger.warn("User {} already claimed task {}.", userId, taskId);
				return true;
			}
			logger.error("Error claiming task {} for user {}", taskId, userId, e);
			return false;
		}
	}

	public boolean unclaimTask(int taskId, int userId) {
		String sql = "DELETE FROM event_task_assignments WHERE task_id = ? AND user_id = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error unclaiming task {} for user {}", taskId, userId, e);
			return false;
		}
	}

	public List<EventTask> getOpenTasksForUser(int userId) {
		List<EventTask> tasks = new ArrayList<>();
		String sql = "SELECT t.*, e.name as event_name " + "FROM event_tasks t "
				+ "JOIN event_task_assignments ta ON t.id = ta.task_id " + "JOIN events e ON t.event_id = e.id "
				+ "WHERE ta.user_id = ? AND t.status = 'OFFEN' " + "ORDER BY e.event_datetime ASC";
		logger.debug("Fetching open tasks for user ID {}", userId);
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					EventTask task = new EventTask();
					task.setId(rs.getInt("id"));
					task.setEventId(rs.getInt("event_id"));
					task.setDescription(rs.getString("description"));
					task.setStatus(rs.getString("status"));
					task.setEventName(rs.getString("event_name"));
					tasks.add(task);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching open tasks for user {}", userId, e);
		}
		return tasks;
	}
}