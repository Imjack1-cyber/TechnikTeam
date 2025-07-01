package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // CORRECTION: Import the WebServlet annotation.
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// CORRECTION: Add the WebServlet annotation to map this servlet to the "/profile" URL.
@WebServlet("/profil")
public class ProfileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private UserQualificationsDAO qualificationsDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		qualificationsDAO = new UserQualificationsDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		// Fetch all data needed for the profile page
		List<Event> eventHistory = eventDAO.getEventHistoryForUser(user.getId());
		List<UserQualification> qualifications = qualificationsDAO.getQualificationsForUser(user.getId());

		// Set the data as request attributes for the JSP
		request.setAttribute("eventHistory", eventHistory);
		request.setAttribute("qualifications", qualifications);

		// Forward the request to the JSP for rendering
		// CORRECTED: Forward to the actual JSP file path.
		request.getRequestDispatcher("/views/public/profile.jsp").forward(request, response);
	}
}