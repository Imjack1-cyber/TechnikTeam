package de.technikteam.servlet.admin;

import de.technikteam.servlet.admin.action.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A Front Controller that centralizes request handling for administrative actions.
 * It uses the Command pattern to delegate processing to specialized Action classes.
 */
@WebServlet("/admin/action/*")
public class FrontControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(FrontControllerServlet.class);
    private final Map<String, Action> actions = new HashMap<>();

    @Override
    public void init() throws ServletException {
        logger.info("Initializing FrontControllerServlet and mapping actions...");
        // User Actions
        actions.put("user.create", new CreateUserAction());
        actions.put("user.update", new UpdateUserAction());
        actions.put("user.delete", new DeleteUserAction());
        actions.put("user.resetPassword", new ResetPasswordAction());

        logger.info("FrontControllerServlet initialized with {} actions.", actions.size());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            logger.error("No action found for key: '{}'. This indicates a misconfigured form or a manual malformed request.", actionKey);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Action not found.");
            return;
        }

        logger.debug("Executing action for key: '{}'", actionKey);
        String result = action.execute(request, response);

        if (result != null) {
            if (result.startsWith("redirect:")) {
                String redirectUrl = request.getContextPath() + result.substring(9);
                logger.debug("Redirecting to: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
            } else {
                logger.debug("Forwarding to: {}", result);
                request.getRequestDispatcher(result).forward(request, response);
            }
        }
        // If result is null, it means the Action class handled the response directly (e.g., sent an error).
    }
}