package de.technikteam.servlet.admin.action;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.FeedbackSubmissionDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class UpdateFeedbackOrderAction implements Action {
    private static final Logger logger = LogManager.getLogger(UpdateFeedbackOrderAction.class);
    private final FeedbackSubmissionDAO submissionDAO;
    private final AdminLogService adminLogService;
    private final DatabaseManager dbManager;
    private final Gson gson = new Gson();

    @Inject
    public UpdateFeedbackOrderAction(FeedbackSubmissionDAO submissionDAO, AdminLogService adminLogService, DatabaseManager dbManager) {
        this.submissionDAO = submissionDAO;
        this.adminLogService = adminLogService;
        this.dbManager = dbManager;
    }

    @Override
    public ApiResponse execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User adminUser = (User) request.getSession().getAttribute("user");
        if (!adminUser.hasAdminAccess()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return null;
        }

        String jsonPayload = request.getParameter("reorderData");
        if (jsonPayload == null) {
            return ApiResponse.error("Missing reorder data.");
        }

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Type type = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> data = gson.fromJson(jsonPayload, type);
                
                int submissionId = (int) Double.parseDouble(String.valueOf(data.get("submissionId")));
                String newStatus = (String) data.get("newStatus");

                @SuppressWarnings("unchecked")
                List<Double> orderedIdsDouble = (List<Double>) data.get("orderedIds");
                if (orderedIdsDouble == null) {
                	throw new IllegalArgumentException("orderedIds list is missing in the payload.");
                }
                List<Integer> orderedIds = orderedIdsDouble.stream().map(Double::intValue).collect(Collectors.toList());

                // 1. Update the status of the moved item
                submissionDAO.updateStatus(submissionId, newStatus, conn);

                // 2. Update the display order for all items in that column
                submissionDAO.updateOrderForStatus(orderedIds, conn);
                
                conn.commit();
                
                adminLogService.log(adminUser.getUsername(), "UPDATE_FEEDBACK_ORDER",
						"Feedback ID " + submissionId + " status auf '" + newStatus + "' gesetzt und Liste neu sortiert.");
                return ApiResponse.success("Feedback-Status und Sortierung aktualisiert.");
                
            } catch (Exception e) {
                conn.rollback();
                throw e; // Re-throw to be caught by the outer catch block
            }
        } catch (Exception e) {
            logger.error("Error processing feedback reorder request", e);
            return ApiResponse.error("Serverfehler beim Verarbeiten der Anfrage: " + e.getMessage());
        }
    }
}