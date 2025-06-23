package de.technikteam.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.StorageLogDAO;
import de.technikteam.model.StorageLogEntry;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Provides a JSON API to fetch the usage history for a specific storage item.
 */
@WebServlet("/api/storage-history")
public class StorageHistoryApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(StorageHistoryApiServlet.class);
    private StorageLogDAO logDAO;
    private Gson gson;

    @Override
    public void init() {
        logDAO = new StorageLogDAO();
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int itemId = Integer.parseInt(request.getParameter("itemId"));
            logger.debug("API request for storage history for item ID: {}", itemId);
            List<StorageLogEntry> history = logDAO.getHistoryForItem(itemId);

            String jsonResponse = gson.toJson(history);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            logger.warn("Bad request to storage history API: invalid or missing itemId");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing itemId.");
        }
    }
}