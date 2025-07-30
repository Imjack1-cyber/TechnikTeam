package de.technikteam.dao;

import de.technikteam.model.TodoCategory;
import de.technikteam.model.TodoTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class TodoDAO {
	private static final Logger logger = LogManager.getLogger(TodoDAO.class);
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public TodoDAO(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<TodoCategory> getAllCategoriesWithTasks() {
		Map<Integer, TodoCategory> categoryMap = new LinkedHashMap<>();
		String sql = "SELECT c.id as cat_id, c.name as cat_name, c.display_order as cat_order, "
				+ "t.id as task_id, t.category_id as task_cat_id, t.content as task_content, "
				+ "t.is_completed as task_completed, t.display_order as task_order "
				+ "FROM todo_categories c LEFT JOIN todo_tasks t ON c.id = t.category_id "
				+ "ORDER BY c.display_order, t.display_order";

		jdbcTemplate.query(sql, rs -> {
			int categoryId = rs.getInt("cat_id");
			TodoCategory category = categoryMap.computeIfAbsent(categoryId, id -> {
				try {
					TodoCategory newCat = new TodoCategory();
					newCat.setId(id);
					newCat.setName(rs.getString("cat_name"));
					newCat.setDisplayOrder(rs.getInt("cat_order"));
					return newCat;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			});

			if (rs.getObject("task_id") != null) {
				TodoTask task = new TodoTask();
				task.setId(rs.getInt("task_id"));
				task.setCategoryId(rs.getInt("task_cat_id"));
				task.setContent(rs.getString("task_content"));
				task.setCompleted(rs.getBoolean("task_completed"));
				task.setDisplayOrder(rs.getInt("task_order"));
				category.getTasks().add(task);
			}
		});
		return new ArrayList<>(categoryMap.values());
	}

	public TodoCategory createCategory(String name) {
		String sql = "INSERT INTO todo_categories (name, display_order) "
				+ "SELECT ?, COALESCE(MAX(display_order), -1) + 1 FROM todo_categories";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, name);
			return ps;
		}, keyHolder);

		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		TodoCategory newCategory = new TodoCategory();
		newCategory.setId(newId);
		newCategory.setName(name);
		return newCategory;
	}

	public TodoTask createTask(int categoryId, String content) {
		String sql = "INSERT INTO todo_tasks (category_id, content, display_order) "
				+ "SELECT ?, ?, COALESCE(MAX(display_order), -1) + 1 FROM todo_tasks WHERE category_id = ?";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setInt(1, categoryId);
			ps.setString(2, content);
			ps.setInt(3, categoryId);
			return ps;
		}, keyHolder);

		int newId = Objects.requireNonNull(keyHolder.getKey()).intValue();
		TodoTask newTask = new TodoTask();
		newTask.setId(newId);
		newTask.setCategoryId(categoryId);
		newTask.setContent(content);
		return newTask;
	}

	public boolean updateTaskContent(int taskId, String content) {
		String sql = "UPDATE todo_tasks SET content = ? WHERE id = ?";
		return jdbcTemplate.update(sql, content, taskId) > 0;
	}

	public boolean updateTaskStatus(int taskId, boolean isCompleted) {
		String sql = "UPDATE todo_tasks SET is_completed = ? WHERE id = ?";
		return jdbcTemplate.update(sql, isCompleted, taskId) > 0;
	}

	public boolean deleteTask(int taskId) {
		String sql = "DELETE FROM todo_tasks WHERE id = ?";
		return jdbcTemplate.update(sql, taskId) > 0;
	}

	public boolean deleteCategory(int categoryId) {
		jdbcTemplate.update("DELETE FROM todo_tasks WHERE category_id = ?", categoryId);
		String sql = "DELETE FROM todo_categories WHERE id = ?";
		return jdbcTemplate.update(sql, categoryId) > 0;
	}

	public void updateCategoryOrder(List<Integer> categoryIds) {
		String sql = "UPDATE todo_categories SET display_order = ? WHERE id = ?";
		jdbcTemplate.batchUpdate(sql, categoryIds, 100, (ps, categoryId) -> {
			ps.setInt(1, categoryIds.indexOf(categoryId));
			ps.setInt(2, categoryId);
		});
	}

	public void updateTaskOrders(List<Integer> taskIds, int categoryId) {
		String sql = "UPDATE todo_tasks SET display_order = ?, category_id = ? WHERE id = ?";
		jdbcTemplate.batchUpdate(sql, taskIds, 100, (ps, taskId) -> {
			ps.setInt(1, taskIds.indexOf(taskId));
			ps.setInt(2, categoryId);
			ps.setInt(3, taskId);
		});
	}
}