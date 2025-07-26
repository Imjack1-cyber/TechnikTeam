package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.TodoDAO;
import de.technikteam.model.TodoCategory;
import de.technikteam.model.TodoTask;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Singleton
public class TodoService {
	private static final Logger logger = LogManager.getLogger(TodoService.class);
	private final DatabaseManager dbManager;
	private final TodoDAO todoDAO;
	private final AdminLogService adminLogService;

	@Inject
	public TodoService(DatabaseManager dbManager, TodoDAO todoDAO, AdminLogService adminLogService) {
		this.dbManager = dbManager;
		this.todoDAO = todoDAO;
		this.adminLogService = adminLogService;
	}

	public List<TodoCategory> getAllTodos() {
		return todoDAO.getAllCategoriesWithTasks();
	}

	public TodoCategory createCategory(String name, User admin) {
		try {
			TodoCategory newCategory = todoDAO.createCategory(name);
			if (newCategory != null) {
				adminLogService.log(admin.getUsername(), "TODO_CREATE_CATEGORY",
						"To-Do Kategorie '" + name + "' erstellt.");
			}
			return newCategory;
		} catch (SQLException e) {
			logger.error("Service error creating To-Do category '{}'", name, e);
			return null;
		}
	}

	public TodoTask createTask(int categoryId, String content, User admin) {
		try {
			TodoTask newTask = todoDAO.createTask(categoryId, content);
			if (newTask != null) {
				adminLogService.log(admin.getUsername(), "TODO_CREATE_TASK",
						"To-Do Aufgabe '" + content + "' in Kategorie ID " + categoryId + " erstellt.");
			}
			return newTask;
		} catch (SQLException e) {
			logger.error("Service error creating To-Do task in category {}", categoryId, e);
			return null;
		}
	}

	public boolean updateTask(int taskId, String content, Boolean isCompleted, User admin) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				boolean success = false;
				if (content != null) {
					success = todoDAO.updateTaskContent(taskId, content, conn);
					if (success)
						adminLogService.log(admin.getUsername(), "TODO_UPDATE_TASK",
								"Aufgabe ID " + taskId + " aktualisiert.");
				}
				if (isCompleted != null) {
					success = todoDAO.updateTaskStatus(taskId, isCompleted, conn);
					if (success)
						adminLogService.log(admin.getUsername(), "TODO_UPDATE_STATUS",
								"Status für Aufgabe ID " + taskId + " auf '" + isCompleted + "' gesetzt.");
				}
				conn.commit();
				return success;
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			logger.error("Service transaction error updating task {}", taskId, e);
			return false;
		}
	}

	public boolean deleteTask(int taskId, User admin) {
		try (Connection conn = dbManager.getConnection()) {
			if (todoDAO.deleteTask(taskId, conn)) {
				adminLogService.log(admin.getUsername(), "TODO_DELETE_TASK", "Aufgabe ID " + taskId + " gelöscht.");
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("Service error deleting task {}", taskId, e);
			return false;
		}
	}

	public boolean deleteCategory(int categoryId, User admin) {
		try (Connection conn = dbManager.getConnection()) {
			if (todoDAO.deleteCategory(categoryId, conn)) {
				adminLogService.log(admin.getUsername(), "TODO_DELETE_CATEGORY",
						"Kategorie ID " + categoryId + " und alle zugehörigen Aufgaben gelöscht.");
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("Service error deleting category {}", categoryId, e);
			return false;
		}
	}

	public boolean reorder(Map<String, List<Integer>> reorderData, User admin) {
		try (Connection conn = dbManager.getConnection()) {
			conn.setAutoCommit(false);
			try {
				List<Integer> categoryOrder = reorderData.get("categoryOrder");
				if (categoryOrder != null) {
					todoDAO.updateCategoryOrder(categoryOrder, conn);
				}

				for (Map.Entry<String, List<Integer>> entry : reorderData.entrySet()) {
					if (entry.getKey().startsWith("category-")) {
						int categoryId = Integer.parseInt(entry.getKey().substring("category-".length()));
						todoDAO.updateTaskOrders(entry.getValue(), categoryId, conn);
					}
				}
				conn.commit();
				adminLogService.log(admin.getUsername(), "TODO_REORDER", "To-Do-Listen neu sortiert.");
				return true;
			} catch (Exception e) {
				conn.rollback();
				throw e;
			}
		} catch (SQLException e) {
			logger.error("Service transaction error during To-Do reorder.", e);
			return false;
		}
	}
}