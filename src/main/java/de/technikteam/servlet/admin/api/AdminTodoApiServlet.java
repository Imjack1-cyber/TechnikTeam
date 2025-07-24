package de.technikteam.servlet.admin.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.TodoCategory;
import de.technikteam.model.TodoTask;
import de.technikteam.model.User;
import de.technikteam.service.TodoService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AdminTodoApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final TodoService todoService;
	private final Gson gson = new Gson();

	@Inject
	public AdminTodoApiServlet(TodoService todoService) {
		this.todoService = todoService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<TodoCategory> todos = todoService.getAllTodos();
		sendJsonResponse(resp, HttpServletResponse.SC_OK, todos);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN, ApiResponse.error("Invalid CSRF Token"));
			return;
		}
		User admin = (User) req.getSession().getAttribute("user");
		String action = req.getParameter("action");

		switch (action) {
		case "createCategory":
			String categoryName = req.getParameter("name");
			TodoCategory newCategory = todoService.createCategory(categoryName, admin);
			if (newCategory != null) {
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						ApiResponse.success("Kategorie erstellt.", newCategory));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Kategorie konnte nicht erstellt werden."));
			}
			break;
		case "createTask":
			int categoryId = Integer.parseInt(req.getParameter("categoryId"));
			String content = req.getParameter("content");
			TodoTask newTask = todoService.createTask(categoryId, content, admin);
			if (newTask != null) {
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						ApiResponse.success("Aufgabe erstellt.", newTask));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Aufgabe konnte nicht erstellt werden."));
			}
			break;
		default:
			sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Unbekannte Aktion."));
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN, ApiResponse.error("Invalid CSRF Token"));
			return;
		}
		User admin = (User) req.getSession().getAttribute("user");
		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Map<String, Object> data = gson.fromJson(jsonPayload, type);
		String action = (String) data.get("action");

		switch (action) {
		case "updateTask":
			int taskId = ((Double) data.get("taskId")).intValue();
			String content = (String) data.get("content");
			Boolean isCompleted = (Boolean) data.get("isCompleted");
			if (todoService.updateTask(taskId, content, isCompleted, admin)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Aufgabe aktualisiert."));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Aufgabe konnte nicht aktualisiert werden."));
			}
			break;
		case "reorder":
			Type reorderType = new TypeToken<Map<String, List<Integer>>>() {
			}.getType();
			Map<String, List<Integer>> reorderData = gson.fromJson(gson.toJson(data.get("orderData")), reorderType);
			if (todoService.reorder(reorderData, admin)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Sortierung gespeichert."));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Sortierung konnte nicht gespeichert werden."));
			}
			break;
		default:
			sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Unbekannte Aktion."));
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!CSRFUtil.isTokenValid(req)) {
			sendJsonResponse(resp, HttpServletResponse.SC_FORBIDDEN, ApiResponse.error("Invalid CSRF Token"));
			return;
		}
		User admin = (User) req.getSession().getAttribute("user");

		if (req.getParameter("taskId") != null) {
			int taskId = Integer.parseInt(req.getParameter("taskId"));
			if (todoService.deleteTask(taskId, admin)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Aufgabe gelöscht."));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Aufgabe konnte nicht gelöscht werden."));
			}
		} else if (req.getParameter("categoryId") != null) {
			int categoryId = Integer.parseInt(req.getParameter("categoryId"));
			if (todoService.deleteCategory(categoryId, admin)) {
				sendJsonResponse(resp, HttpServletResponse.SC_OK, ApiResponse.success("Kategorie gelöscht."));
			} else {
				sendJsonResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						ApiResponse.error("Kategorie konnte nicht gelöscht werden."));
			}
		} else {
			sendJsonResponse(resp, HttpServletResponse.SC_BAD_REQUEST, ApiResponse.error("Keine ID angegeben."));
		}
	}

	private void sendJsonResponse(HttpServletResponse response, int status, Object data) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(gson.toJson(data));
	}
}