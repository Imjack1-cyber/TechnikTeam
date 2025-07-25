package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.ApiResponse;
import de.technikteam.servlet.admin.action.*;
import jakarta.servlet.ServletException;
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

@Singleton
public class FrontControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FrontControllerServlet.class);
	private final Map<String, Action> actions = new HashMap<>();
	private final Gson gson;

	@Inject
	public FrontControllerServlet(CreateUserAction createUserAction, UpdateUserAction updateUserAction,
			DeleteUserAction deleteUserAction, ResetPasswordAction resetPasswordAction,
			UnlockUserAction unlockUserAction, ApproveChangeAction approveChangeAction,
			DenyChangeAction denyChangeAction, UpdateFeedbackStatusAction updateFeedbackStatusAction,
			UpdateFeedbackOrderAction updateFeedbackOrderAction, DeleteFeedbackAction deleteFeedbackAction,
			GetFeedbackDetailsAction getFeedbackDetailsAction, UpdateWikiAction updateWikiAction) {

		actions.put("user.create", createUserAction);
		actions.put("user.update", updateUserAction);
		actions.put("user.delete", deleteUserAction);
		actions.put("user.resetPassword", resetPasswordAction);
		actions.put("user.unlock", unlockUserAction);
		actions.put("request.approve", approveChangeAction);
		actions.put("request.deny", denyChangeAction);
		actions.put("feedback.updateStatus", updateFeedbackStatusAction);
		actions.put("feedback.reorder", updateFeedbackOrderAction);
		actions.put("feedback.delete", deleteFeedbackAction);
		actions.put("feedback.getDetails", getFeedbackDetailsAction);
		actions.put("wiki.update", updateWikiAction);

		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		String actionName = request.getParameter("action");

		if (pathInfo == null || pathInfo.equals("/") || actionName == null || actionName.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action request.");
			return;
		}

		String entity = pathInfo.substring(1);
		String actionKey = entity + "." + actionName;

		Action action = actions.get(actionKey);

		if (action == null) {
			logger.error("No action found for key: '{}'", actionKey);
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action not found.");
			return;
		}

		logger.debug("Executing action for key: '{}'", actionKey);
		ApiResponse apiResponse = action.execute(request, response);

		if (apiResponse == null) {
			return;
		}

		if (apiResponse.isSuccess()) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}
}