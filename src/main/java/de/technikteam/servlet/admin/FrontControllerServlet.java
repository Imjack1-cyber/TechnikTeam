package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.ApiResponse;
import de.technikteam.servlet.admin.action.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A Front Controller that centralizes request handling for administrative
 * actions. It uses the Command pattern to delegate processing to specialized
 * Action classes and returns a standardized JSON ApiResponse.
 */
@WebServlet("/admin/action/*")
@MultipartConfig
public class FrontControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FrontControllerServlet.class);
	private final Map<String, Action> actions = new HashMap<>();
	private Gson gson;

	@Override
	public void init() throws ServletException {
		logger.info("Initializing FrontControllerServlet and mapping actions...");
		// User Actions
		actions.put("user.create", new CreateUserAction());
		actions.put("user.update", new UpdateUserAction());
		actions.put("user.delete", new DeleteUserAction());
		actions.put("user.resetPassword", new ResetPasswordAction());
		actions.put("user.unlock", new UnlockUserAction());

		// Profile Change Request Actions
		actions.put("request.approve", new ApproveChangeAction());
		actions.put("request.deny", new DenyChangeAction());

		// Feedback Actions
		actions.put("feedback.updateStatus", new UpdateFeedbackStatusAction());
		actions.put("feedback.delete", new DeleteFeedbackAction());

		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

		logger.info("FrontControllerServlet initialized with {} actions.", actions.size());
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo(); // e.g., "/user"
		String actionName = request.getParameter("action"); // e.g., "create"

		if (pathInfo == null || pathInfo.equals("/") || actionName == null || actionName.isEmpty()) {
			logger.warn("Invalid request to Front Controller: PathInfo='{}', Action='{}'", pathInfo, actionName);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action request.");
			return;
		}

		String entity = pathInfo.substring(1); // "user"
		String actionKey = entity + "." + actionName; // "user.create"

		Action action = actions.get(actionKey);

		if (action == null) {
			logger.error(
					"No action found for key: '{}'. This indicates a misconfigured form or a manual malformed request.",
					actionKey);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action not found.");
			return;
		}

		logger.debug("Executing action for key: '{}'", actionKey);
		ApiResponse apiResponse = action.execute(request, response);

		if (apiResponse == null) {
			// Action handled the response directly (e.g., file download, error, etc.)
			return;
		}

		// Set HTTP status code based on success
		if (apiResponse.isSuccess()) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			// Use a generic bad request for client-side errors, could be refined
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		// Serialize ApiResponse to JSON and send it
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}
}