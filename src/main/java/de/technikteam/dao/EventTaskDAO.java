package de.technikteam.dao;

import de.technikteam.model.EventTask;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EventTaskDAO {
	private static final Logger logger = LogManager.getLogger(EventTaskDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public EventTaskDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	public int saveTask(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities, String[] kitIds,
			int[] dependencyIds) {
		boolean isUpdate = task.getId() > 0;
		logger.debug("DAO saveTask called for task '{}'. Is update: {}", task.getDescription(), isUpdate);
		if (isUpdate) {
			updateTask(task);
		} else {
			int newId = createTask(task);
			task.setId(newId);
		}

		if (task.getId() == 0) {
			logger.error("Failed to create or find task ID for task: {}", task.getDescription());
			throw new RuntimeException("Failed to create or find task ID.");
		}

		clearAssociations(task.getId());
		saveUserAssignments(task.getId(), userIds);
		saveItemRequirements(task.getId(), itemIds, itemQuantities);
		saveKitRequirements(task.getId(), kitIds);
		saveDependencies(task.getId(), dependencyIds);

		logger.info("Successfully saved task ID {}", task.getId());
		return task.getId();
	}

	private int createTask(EventTask task) {
		logger.debug("DAO createTask: eventId={}, description='{}'", task.getEventId(), task.getDescription());
		String taskSql = "INSERT INTO event_tasks (event_id, description, details, status, display_order, required_persons) VALUES (?, ?, ?, 'OFFEN', ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(taskSql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, task.getEventId());
			ps.setString(2, task.getDescription());
			ps.setString(3, task.getDetails());
			ps.setInt(4, task.getDisplayOrder());
			ps.setInt(5, task.getRequiredPersons());
			return ps;
		}, keyHolder);
		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		logger.debug("DAO createTask successful. New task ID: {}", newId);
		return newId;
	}

	private void updateTask(EventTask task) {
		logger.debug("DAO updateTask for task ID: {}", task.getId());
		String taskSql = "UPDATE event_tasks SET description = ?, details = ?, status = ?, display_order = ?, required_persons = ? WHERE id = ?";
		jdbcTemplate.update(taskSql, task.getDescription(), task.getDetails(), task.getStatus(), task.getDisplayOrder(),
				task.getRequiredPersons(), task.getId());
	}

	private void clearAssociations(int taskId) {
		jdbcTemplate.update("DELETE FROM event_task_assignments WHERE task_id = ?", taskId);
		jdbcTemplate.update("DELETE FROM event_task_storage_items WHERE task_id = ?", taskId);
		jdbcTemplate.update("DELETE FROM event_task_kits WHERE task_id = ?", taskId);
		jdbcTemplate.update("DELETE FROM event_task_dependencies WHERE task_id = ?", taskId);
	}

	private void saveUserAssignments(int taskId, int[] userIds) {
		if (userIds == null || userIds.length == 0)
			return;
		String sql = "INSERT INTO event_task_assignments (task_id, user_id) VALUES (?, ?)";
		List<Integer> userIdList = Arrays.stream(userIds).boxed().collect(Collectors.toList());
		jdbcTemplate.batchUpdate(sql, userIdList, 100, (ps, userId) -> {
			ps.setInt(1, taskId);
			ps.setInt(2, userId);
		});
	}

	private void saveItemRequirements(int taskId, String[] itemIds, String[] itemQuantities) {
		if (itemIds == null || itemQuantities == null || itemIds.length != itemQuantities.length)
			return;
		String sql = "INSERT INTO event_task_storage_items (task_id, item_id, quantity) VALUES (?, ?, ?)";
		List<String> itemIdList = List.of(itemIds);
		jdbcTemplate.batchUpdate(sql, itemIdList, 100, (ps, itemIdStr) -> {
			if (itemIdStr != null && !itemIdStr.isEmpty()) {
				int index = itemIdList.indexOf(itemIdStr);
				ps.setInt(1, taskId);
				ps.setInt(2, Integer.parseInt(itemIdStr));
				ps.setInt(3, Integer.parseInt(itemQuantities[index]));
			}
		});
	}

	private void saveKitRequirements(int taskId, String[] kitIds) {
		if (kitIds == null || kitIds.length == 0)
			return;
		String sql = "INSERT INTO event_task_kits (task_id, kit_id) VALUES (?, ?)";
		List<String> kitIdList = List.of(kitIds);
		jdbcTemplate.batchUpdate(sql, kitIdList, 100, (ps, kitIdStr) -> {
			if (kitIdStr != null && !kitIdStr.isEmpty()) {
				ps.setInt(1, taskId);
				ps.setInt(2, Integer.parseInt(kitIdStr));
			}
		});
	}

	private void saveDependencies(int taskId, int[] dependencyIds) {
		if (dependencyIds == null || dependencyIds.length == 0)
			return;
		String sql = "INSERT INTO event_task_dependencies (task_id, depends_on_task_id) VALUES (?, ?)";
		List<Integer> dependencyIdList = Arrays.stream(dependencyIds).boxed().collect(Collectors.toList());
		jdbcTemplate.batchUpdate(sql, dependencyIdList, 100, (ps, dependencyId) -> {
			ps.setInt(1, taskId);
			ps.setInt(2, dependencyId);
		});
	}

	public List<EventTask> getTasksForEvent(int eventId) {
		Map<Integer, EventTask> tasksById = new LinkedHashMap<>();
		String sql = "SELECT t.*, u.id as user_id, u.username, si.id as item_id, si.name as item_name, tsi.quantity as item_quantity, ik.id as kit_id, ik.name as kit_name FROM event_tasks t LEFT JOIN event_task_assignments ta ON t.id = ta.task_id LEFT JOIN users u ON ta.user_id = u.id LEFT JOIN event_task_storage_items tsi ON t.id = tsi.task_id LEFT JOIN storage_items si ON tsi.item_id = si.id LEFT JOIN event_task_kits tk ON t.id = tk.task_id LEFT JOIN inventory_kits ik ON tk.kit_id = ik.id WHERE t.event_id = ? ORDER BY FIELD(t.status, 'OFFEN', 'IN_ARBEIT', 'ERLEDIGT'), CASE WHEN t.status = 'OFFEN' AND ta.user_id IS NULL THEN 0 ELSE 1 END, t.updated_at DESC";

		jdbcTemplate.query(sql, (ResultSet rs) -> {
			int currentTaskId = rs.getInt("id");
			EventTask task = tasksById.computeIfAbsent(currentTaskId, id -> mapResultSetToTask(rs));

			int currentUserId = rs.getInt("user_id");
			if (currentUserId > 0 && task.getAssignedUsers().stream().noneMatch(u -> u.getId() == currentUserId)) {
				User user = new User();
				user.setId(currentUserId);
				user.setUsername(rs.getString("username"));
				task.getAssignedUsers().add(user);
			}
			int currentItemId = rs.getInt("item_id");
			if (currentItemId > 0 && task.getRequiredItems().stream().noneMatch(i -> i.getId() == currentItemId)) {
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
		}, eventId);

		// Now fetch and assemble dependencies
		if (!tasksById.isEmpty()) {
			String depSql = "SELECT * FROM event_task_dependencies WHERE task_id IN ("
					+ tasksById.keySet().stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
			jdbcTemplate.query(depSql, rs -> {
				int taskId = rs.getInt("task_id");
				int dependsOnId = rs.getInt("depends_on_task_id");
				EventTask task = tasksById.get(taskId);
				EventTask parentTask = tasksById.get(dependsOnId);
				if (task != null && parentTask != null) {
					task.getDependsOn().add(parentTask);
					parentTask.getDependencyFor().add(task);
				}
			});
		}

		return new ArrayList<>(tasksById.values());
	}

	private EventTask mapResultSetToTask(ResultSet rs) {
		try {
			EventTask task = new EventTask();
			task.setId(rs.getInt("id"));
			task.setEventId(rs.getInt("event_id"));
			task.setDescription(rs.getString("description"));
			task.setDetails(rs.getString("details"));
			task.setStatus(rs.getString("status"));
			task.setDisplayOrder(rs.getInt("display_order"));
			task.setRequiredPersons(rs.getInt("required_persons"));
			task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
			task.setAssignedUsers(new ArrayList<>());
			task.setRequiredItems(new ArrayList<>());
			task.setRequiredKits(new ArrayList<>());
			task.setDependsOn(new ArrayList<>());
			task.setDependencyFor(new ArrayList<>());
			return task;
		} catch (SQLException e) {
			throw new RuntimeException("Failed to map ResultSet to EventTask", e);
		}
	}

	public boolean deleteTask(int taskId) {
		String sql = "DELETE FROM event_tasks WHERE id = ?";
		return jdbcTemplate.update(sql, taskId) > 0;
	}

	public boolean updateTaskStatus(int taskId, String status) {
		String sql = "UPDATE event_tasks SET status = ? WHERE id = ?";
		return jdbcTemplate.update(sql, status, taskId) > 0;
	}

	public boolean assignUserToTask(int taskId, int userId) {
		String sql = "INSERT INTO event_task_assignments (task_id, user_id) VALUES (?, ?)";
		try {
			return jdbcTemplate.update(sql, taskId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error assigning user {} to task {}", userId, taskId, e);
			return false;
		}
	}

	public boolean unassignUserFromTask(int taskId, int userId) {
		String sql = "DELETE FROM event_task_assignments WHERE task_id = ? AND user_id = ?";
		try {
			return jdbcTemplate.update(sql, taskId, userId) > 0;
		} catch (Exception e) {
			logger.error("Error un-assigning user {} from task {}", userId, taskId, e);
			return false;
		}
	}

	public List<EventTask> getOpenTasksForUser(int userId) {
		String sql = "SELECT t.*, e.name as event_name FROM event_tasks t JOIN event_task_assignments ta ON t.id = ta.task_id JOIN events e ON t.event_id = e.id WHERE ta.user_id = ? AND t.status = 'OFFEN' ORDER BY e.event_datetime ASC";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			EventTask task = new EventTask();
			task.setId(rs.getInt("id"));
			task.setEventId(rs.getInt("event_id"));
			task.setDescription(rs.getString("description"));
			task.setStatus(rs.getString("status"));
			task.setEventName(rs.getString("event_name"));
			return task;
		}, userId);
	}
}