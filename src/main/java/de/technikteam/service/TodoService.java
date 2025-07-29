package de.technikteam.service;

import de.technikteam.dao.TodoDAO;
import de.technikteam.model.TodoCategory;
import de.technikteam.model.TodoTask;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class TodoService {
	private static final Logger logger = LogManager.getLogger(TodoService.class);
	private final TodoDAO todoDAO;
	private final AdminLogService adminLogService;

	@Autowired
	public TodoService(TodoDAO todoDAO, AdminLogService adminLogService) {
		this.todoDAO = todoDAO;
		this.adminLogService = adminLogService;
	}

	public List<TodoCategory> getAllTodos() {
		return todoDAO.getAllCategoriesWithTasks();
	}

	@Transactional
	public TodoCategory createCategory(String name, User admin) {
		try {
			TodoCategory newCategory = todoDAO.createCategory(name);
			if (newCategory != null) {
				adminLogService.log(admin.getUsername(), "TODO_CREATE_CATEGORY",
						"To-Do Kategorie '" + name + "' erstellt.");
			}
			return newCategory;
		} catch (Exception e) {
			logger.error("Service error creating To-Do category '{}'", name, e);
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public TodoTask createTask(int categoryId, String content, User admin) {
		try {
			TodoTask newTask = todoDAO.createTask(categoryId, content);
			if (newTask != null) {
				adminLogService.log(admin.getUsername(), "TODO_CREATE_TASK",
						"To-Do Aufgabe '" + content + "' in Kategorie ID " + categoryId + " erstellt.");
			}
			return newTask;
		} catch (Exception e) {
			logger.error("Service error creating To-Do task in category {}", categoryId, e);
			throw new RuntimeException(e);
		}
	}

	@Transactional
	public boolean updateTask(int taskId, String content, Boolean isCompleted, User admin) {
		boolean success = false;
		if (content != null) {
			success = todoDAO.updateTaskContent(taskId, content);
			if (success)
				adminLogService.log(admin.getUsername(), "TODO_UPDATE_TASK", "Aufgabe ID " + taskId + " aktualisiert.");
		}
		if (isCompleted != null) {
			success = todoDAO.updateTaskStatus(taskId, isCompleted);
			if (success)
				adminLogService.log(admin.getUsername(), "TODO_UPDATE_STATUS",
						"Status für Aufgabe ID " + taskId + " auf '" + isCompleted + "' gesetzt.");
		}
		return success;
	}

	@Transactional
	public boolean deleteTask(int taskId, User admin) {
		if (todoDAO.deleteTask(taskId)) {
			adminLogService.log(admin.getUsername(), "TODO_DELETE_TASK", "Aufgabe ID " + taskId + " gelöscht.");
			return true;
		}
		return false;
	}

	@Transactional
	public boolean deleteCategory(int categoryId, User admin) {
		if (todoDAO.deleteCategory(categoryId)) {
			adminLogService.log(admin.getUsername(), "TODO_DELETE_CATEGORY",
					"Kategorie ID " + categoryId + " und alle zugehörigen Aufgaben gelöscht.");
			return true;
		}
		return false;
	}

	@Transactional
	public boolean reorder(Map<String, List<Integer>> reorderData, User admin) {
		List<Integer> categoryOrder = reorderData.get("categoryOrder");
		if (categoryOrder != null) {
			todoDAO.updateCategoryOrder(categoryOrder);
		}

		for (Map.Entry<String, List<Integer>> entry : reorderData.entrySet()) {
			if (entry.getKey().startsWith("category-")) {
				int categoryId = Integer.parseInt(entry.getKey().substring("category-".length()));
				todoDAO.updateTaskOrders(entry.getValue(), categoryId);
			}
		}
		adminLogService.log(admin.getUsername(), "TODO_REORDER", "To-Do-Listen neu sortiert.");
		return true;
	}
}