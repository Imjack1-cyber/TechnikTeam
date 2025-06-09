package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.HistoryDAO;
import de.technikteam.model.ParticipationHistory;

@WebServlet("/admin/history")
public class AdminHistoryServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HistoryDAO historyDAO;

	@Override
	public void init() {
		historyDAO = new HistoryDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<ParticipationHistory> history = historyDAO.getFullParticipationHistory();
		request.setAttribute("history", history);
		request.getRequestDispatcher("/admin/admin_history.jsp").forward(request, response);
	}
}