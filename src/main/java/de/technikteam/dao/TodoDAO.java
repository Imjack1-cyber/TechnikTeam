package de.technikteam.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.TodoCategory;
import de.technikteam.model.TodoTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class TodoDAO {
	private static final Logger logger = LogManager.getLogger(TodoDAO.class);
	private final DatabaseManager dbManager;

	@Inject
	public TodoDAO(DatabaseManager dbManager) {
		this.dbManager = dbManager;
	}

	public List<TodoCategory> getAllCategoriesWithTasks() {
		Map<Integer, TodoCategory> categoryMap = new LinkedHashMap<>();
		String sql = "SELECT c.id as cat_id, c.name as cat_name, c.display_order as cat_order, "
				+ "t.id as task_id, t.category_id as task_cat_id, t.content as task_content, "
				+ "t.is_completed as task_completed, t.display_order as task_order "
				+ "FROM todo_categories c LEFT JOIN todo_tasks t ON c.id = t.category_id "
				+ "ORDER BY c.display_order, t.display_order";

		try (Connection conn = dbManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				int categoryId = rs.getInt("cat_id");
				TodoCategory category = categoryMap.computeIfAbsent(categoryId, id -> {
					TodoCategory newCat = new TodoCategory();
					try {
						newCat.setId(id);
						newCat.setName(rs.getString("cat_name"));
						newCat.setDisplayOrder(rs.getInt("cat_order"));
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					return newCat;
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
			}
		} catch (SQLException e) {
			logger.error("Error fetching all To-Do categories with tasks.", e);
		}
		return new ArrayList<>(categoryMap.values());
	}

	public TodoCategory createCategory(String name) throws SQLException {
		String sql = "INSERT INTO todo_categories (name, display_order) "
				+ "SELECT ?, COALESCE(MAX(display_order), -1) + 1 FROM todo_categories";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, name);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						TodoCategory newCategory = new TodoCategory();
						newCategory.setId(rs.getInt(1));
						newCategory.setName(name);
						return newCategory;
					}
				}
			}
		}
		return null;
	}

	public TodoTask createTask(int categoryId, String content) throws SQLException {
		String sql = "INSERT INTO todo_tasks (category_id, content, display_order) "
				+ "SELECT ?, ?, COALESCE(MAX(display_order), -1) + 1 FROM todo_tasks WHERE category_id = ?";
		try (Connection conn = dbManager.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setInt(1, categoryId);
			pstmt.setString(2, content);
			pstmt.setInt(3, categoryId);
			int affectedRows = pstmt.executeUpdate();
			if (affectedRows > 0) {
				try (ResultSet rs = pstmt.getGeneratedKeys()) {
					if (rs.next()) {
						TodoTask newTask = new TodoTask();
						newTask.setId(rs.getInt(1));
						newTask.setCategoryId(categoryId);
						newTask.setContent(content);
						return newTask;
					}
				}
			}
		}
		return null;
	}

	public boolean updateTaskContent(int taskId, String content, Connection conn) throws SQLException {
		String sql = "UPDATE todo_tasks SET content = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, content);
			pstmt.setInt(2, taskId);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean updateTaskStatus(int taskId, boolean isCompleted, Connection conn) throws SQLException {
		String sql = "UPDATE todo_tasks SET is_completed = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setBoolean(1, isCompleted);
			pstmt.setInt(2, taskId);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean deleteTask(int taskId, Connection conn) throws SQLException {
		String sql = "DELETE FROM todo_tasks WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, taskId);
			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean deleteCategory(int categoryId, Connection conn) throws SQLException {
		String sql = "DELETE FROM todo_categories WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, categoryId);
			return pstmt.executeUpdate() > 0;
		}
	}

	public void updateCategoryOrder(List<Integer> categoryIds, Connection conn) throws SQLException {
		String sql = "UPDATE todo_categories SET display_order = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < categoryIds.size(); i++) {
				pstmt.setInt(1, i);
				pstmt.setInt(2, categoryIds.get(i));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		}
	}

	public void updateTaskOrders(List<Integer> taskIds, int categoryId, Connection conn) throws SQLException {
		String sql = "UPDATE todo_tasks SET display_order = ?, category_id = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < taskIds.size(); i++) {
				pstmt.setInt(1, i);
				pstmt.setInt(2, categoryId);
				pstmt.setInt(3, taskIds.get(i));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		}
	}
}