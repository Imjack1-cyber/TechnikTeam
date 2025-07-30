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
	public int saveTask(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities, String[] kitIds) {
		boolean isUpdate = task.getId() > 0;
		if (isUpdate) {
			updateTask(task);
		} else {
			int newId = createTask(task);
			task.setId(newId);
		}

		if (task.getId() == 0) {
			throw new RuntimeException("Failed to create or find task ID.");
		}

		clearAssociations(task.getId());
		saveUserAssignments(task.getId(), userIds);
		saveItemRequirements(task.getId(), itemIds, itemQuantities);
		saveKitRequirements(task.getId(), kitIds);

		logger.info("Successfully saved task ID {}", task.getId());
		return task.getId();
	}

	private int createTask(EventTask task) {
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
		return Objects.requireNonNull(keyHolder.getKey()).intValue();
	}

	private void updateTask(EventTask task) {
		String taskSql = "UPDATE event_tasks SET description = ?, details = ?, status = ?, display_order = ?, required_persons = ? WHERE id = ?";
		jdbcTemplate.update(taskSql, task.getDescription(), task.getDetails(), task.getStatus(), task.getDisplayOrder(),
				task.getRequiredPersons(), task.getId());
	}

	private void clearAssociations(int taskId) {
		jdbcTemplate.update("DELETE FROM event_task_assignments WHERE task_id = ?", taskId);
		jdbcTemplate.update("DELETE FROM event_task_storage_items WHERE task_id = ?", taskId);
		jdbcTemplate.update("DELETE FROM event_task_kits WHERE task_id = ?", taskId);
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

	public List<EventTask> getTasksForEvent(int eventId) {
		Map<Integer, EventTask> tasksById = new LinkedHashMap<>();
		String sql = "SELECT t.*, u.id as user_id, u.username, si.id as item_id, si.name as item_name, tsi.quantity as item_quantity, ik.id as kit_id, ik.name as kit_name FROM event_tasks t LEFT JOIN event_task_assignments ta ON t.id = ta.task_id LEFT JOIN users u ON ta.user_id = u.id LEFT JOIN event_task_storage_items tsi ON t.id = tsi.task_id LEFT JOIN storage_items si ON tsi.item_id = si.id LEFT JOIN event_task_kits tk ON t.id = tk.task_id LEFT JOIN inventory_kits ik ON tk.kit_id = ik.id WHERE t.event_id = ? ORDER BY t.display_order ASC, t.id ASC, u.username, si.name, ik.name";

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
			task.setAssignedUsers(new ArrayList<>());
			task.setRequiredItems(new ArrayList<>());
			task.setRequiredKits(new ArrayList<>());
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