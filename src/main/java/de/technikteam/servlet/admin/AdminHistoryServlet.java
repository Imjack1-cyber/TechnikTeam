package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.HistoryDAO;
import de.technikteam.model.ParticipationHistory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Stellen Sie sicher, dass die Annotation exakt so lautet
@WebServlet("/admin/history")
public class AdminHistoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminHistoryServlet.class);
	private HistoryDAO historyDAO;

	@Override
	public void init() {
		historyDAO = new HistoryDAO();
		logger.info("AdminHistoryServlet initialized.");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.debug("GET request received for /admin/history.");

		try {
			List<ParticipationHistory> history = historyDAO.getFullParticipationHistory();
			logger.info("Fetched {} history entries.", history.size());
			request.setAttribute("history", history);
			request.getRequestDispatcher("/admin/admin_history.jsp").forward(request, response);
		} catch (Exception e) {
			logger.error("Error while serving history page.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Fehler beim Laden der Teilnahmehistorie.");
		}
	}
}