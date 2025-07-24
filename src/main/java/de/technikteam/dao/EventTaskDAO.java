package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.EventTask;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class EventTaskDAO {
	private static final Logger logger = LogManager.getLogger(EventTaskDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public EventTaskDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public int saveTask(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities, String[] kitIds) {
		boolean isUpdate = task.getId() > 0;
		String taskSql = isUpdate
				? "UPDATE event_tasks SET description = ?, details = ?, status = ?, display_order = ?, required_persons = ? WHERE id = ?"
				: "INSERT INTO event_tasks (event_id, description, details, status, display_order, required_persons) VALUES (?, ?, ?, 'OFFEN', ?, ?)";
		Connection conn = null;
		try {
			conn = dbManager.getConnection();
			conn.setAutoCommit(false);
			try {
				try (PreparedStatement pstmt = conn.prepareStatement(taskSql, Statement.RETURN_GENERATED_KEYS)) {
					if (isUpdate) {
						setUpdateTaskStatementParams(pstmt, task);
					} else {
						setCreateTaskStatementParams(pstmt, task);
					}
					pstmt.executeUpdate();
					if (!isUpdate) {
						try (ResultSet rs = pstmt.getGeneratedKeys()) {
							if (rs.next()) {
								task.setId(rs.getInt(1));
							}
						}
					}
				}
				int taskId = task.getId();
				if (taskId == 0)
					throw new SQLException("Failed to create or find task ID.");

				clearAssociations(conn, taskId);
				saveUserAssignments(conn, taskId, userIds);
				saveItemRequirements(conn, taskId, itemIds, itemQuantities);
				saveKitRequirements(conn, taskId, kitIds);

				conn.commit();
				logger.info("Successfully saved task ID {}", taskId);
				return taskId;
			} catch (Exception e) {
				conn.rollback();
				logger.error("Error in task transaction. Rolling back.", e);
				return 0;
			}
		} catch (SQLException e) {
			logger.error("Failed to get DB connection for task transaction.", e);
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

	public List<EventTask> getTasksForEvent(int eventId) {
		Map<Integer, EventTask> tasksById = new LinkedHashMap<>();
		String sql = "SELECT t.*, u.id as user_id, u.username, si.id as item_id, si.name as item_name, tsi.quantity as item_quantity, ik.id as kit_id, ik.name as kit_name FROM event_tasks t LEFT JOIN event_task_assignments ta ON t.id = ta.task_id LEFT JOIN users u ON ta.user_id = u.id LEFT JOIN event_task_storage_items tsi ON t.id = tsi.task_id LEFT JOIN storage_items si ON tsi.item_id = si.id LEFT JOIN event_task_kits tk ON t.id = tk.task_id LEFT JOIN inventory_kits ik ON tk.kit_id = ik.id WHERE t.event_id = ? ORDER BY t.display_order ASC, t.id ASC, u.username, si.name, ik.name";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, eventId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					int currentTaskId = rs.getInt("id");
					EventTask task = tasksById.get(currentTaskId);
					if (task == null) {
						task = mapResultSetToTask(rs);
						tasksById.put(currentTaskId, task);
					}

					// CORRECTED: Perform checks outside of the lambda to avoid checked exception
					// issues.
					int currentUserId = rs.getInt("user_id");
					if (currentUserId > 0
							&& task.getAssignedUsers().stream().noneMatch(u -> u.getId() == currentUserId)) {
						User user = new User();
						user.setId(currentUserId);
						user.setUsername(rs.getString("username"));
						task.getAssignedUsers().add(user);
					}

					int currentItemId = rs.getInt("item_id");
					if (currentItemId > 0
							&& task.getRequiredItems().stream().noneMatch(i -> i.getId() == currentItemId)) {
						StorageItem item = new StorageItem();
						item.setId(currentItemId);
						item.setName(rs.getString("item_name"));
						item.setQuantity(rs.getInt("item_quantity"));
						task.getRequiredItems().add(item);
					}

					int currentKitId = rs.getInt("kit_id");
					if (currentKitId > 0 && task.getRequiredKits().stream().noneMatch(k -> k.getId() == currentKitId)) {
						InventoryKit kit = new InventoryKit();
						kit.setId(currentKitId);
						kit.setName(rs.getString("kit_name"));
						task.getRequiredKits().add(kit);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching tasks for event {}", eventId, e);
		}
		return new ArrayList<>(tasksById.values());
	}

	public EventTask getTaskById(int taskId) {
		String sql = "SELECT * FROM event_tasks WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToTask(rs);
				}
			}
		} catch (SQLException e) {
			logger.error("Error fetching task by ID {}", taskId, e);
		}
		return null;
	}

	public boolean deleteTask(int taskId) {
		String sql = "DELETE FROM event_tasks WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error deleting task {}", taskId, e);
			return false;
		}
	}

	public boolean updateTaskStatus(int taskId, String status) {
		String sql = "UPDATE event_tasks SET status = ? WHERE id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error updating status for task {}", taskId, e);
			return false;
		}
	}

	public boolean claimTask(int taskId, int userId) {
		String sql = "INSERT INTO event_task_assignments (task_id, user_id) SELECT ?, ? FROM event_tasks WHERE id = ? AND required_persons > (SELECT COUNT(*) FROM event_task_assignments WHERE task_id = ?)";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			pstmt.setInt(2, userId);
			pstmt.setInt(3, taskId);
			pstmt.setInt(4, taskId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				return true;
			}
			logger.error("Error claiming task {} for user {}", taskId, userId, e);
			return false;
		}
	}

	public boolean unclaimTask(int taskId, int userId) {
		String sql = "DELETE FROM event_task_assignments WHERE task_id = ? AND user_id = ?";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			pstmt.setInt(2, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			logger.error("Error unclaiming task {} for user {}", taskId, userId, e);
			return false;
		}
	}

	public boolean isUserAssignedToTask(int taskId, int userId) {
		String sql = "SELECT 1 FROM event_task_assignments WHERE task_id = ? AND user_id = ? LIMIT 1";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			pstmt.setInt(2, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			logger.error("Error checking user assignment for task {} and user {}", taskId, userId, e);
			return false;
		}
	}

	public List<EventTask> getOpenTasksForUser(int userId) {
		List<EventTask> tasks = new ArrayList<>();
		String sql = "SELECT t.*, e.name as event_name FROM event_tasks t JOIN event_task_assignments ta ON t.id = ta.task_id JOIN events e ON t.event_id = e.id WHERE ta.user_id = ? AND t.status = 'OFFEN' ORDER BY e.event_datetime ASC";
		try (Connection conn = dbManager.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
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

	private EventTask mapResultSetToTask(ResultSet rs) throws SQLException {
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
		return task;
	}

	private void setCreateTaskStatementParams(PreparedStatement pstmt, EventTask task) throws SQLException {
		pstmt.setInt(1, task.getEventId());
		pstmt.setString(2, task.getDescription());
		pstmt.setString(3, task.getDetails());
		pstmt.setInt(4, task.getDisplayOrder());
		pstmt.setInt(5, task.getRequiredPersons());
	}

	private void setUpdateTaskStatementParams(PreparedStatement pstmt, EventTask task) throws SQLException {
		pstmt.setString(1, task.getDescription());
		pstmt.setString(2, task.getDetails());
		pstmt.setString(3, task.getStatus());
		pstmt.setInt(4, task.getDisplayOrder());
		pstmt.setInt(5, task.getRequiredPersons());
		pstmt.setInt(6, task.getId());
	}

	private void clearAssociations(Connection conn, int taskId) throws SQLException {
		try (PreparedStatement userStmt = conn.prepareStatement("DELETE FROM event_task_assignments WHERE task_id = ?");
				PreparedStatement itemStmt = conn
						.prepareStatement("DELETE FROM event_task_storage_items WHERE task_id = ?");
				PreparedStatement kitStmt = conn.prepareStatement("DELETE FROM event_task_kits WHERE task_id = ?")) {
			userStmt.setInt(1, taskId);
			userStmt.executeUpdate();
			itemStmt.setInt(1, taskId);
			itemStmt.executeUpdate();
			kitStmt.setInt(1, taskId);
			kitStmt.executeUpdate();
		}
	}

	private void saveUserAssignments(Connection conn, int taskId, int[] userIds) throws SQLException {
		if (userIds == null || userIds.length == 0)
			return;
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

	private void saveItemRequirements(Connection conn, int taskId, String[] itemIds, String[] itemQuantities)
			throws SQLException, NumberFormatException {
		if (itemIds == null || itemQuantities == null || itemIds.length != itemQuantities.length)
			return;
		String sql = "INSERT INTO event_task_storage_items (task_id, item_id, quantity) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < itemIds.length; i++) {
				if (itemIds[i] != null && !itemIds[i].isEmpty()) {
					pstmt.setInt(1, taskId);
					pstmt.setInt(2, Integer.parseInt(itemIds[i]));
					pstmt.setInt(3, Integer.parseInt(itemQuantities[i]));
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
		}
	}

	private void saveKitRequirements(Connection conn, int taskId, String[] kitIds)
			throws SQLException, NumberFormatException {
		if (kitIds == null || kitIds.length == 0)
			return;
		String sql = "INSERT INTO event_task_kits (task_id, kit_id) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (String kitId : kitIds) {
				if (kitId != null && !kitId.isEmpty()) {
					pstmt.setInt(1, taskId);
					pstmt.setInt(2, Integer.parseInt(kitId));
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
		}
	}
}