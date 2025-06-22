package de.technikteam.servlet;

import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Mapped to `/lehrgaenge`, this servlet is responsible for the main
 * course/meeting listing page for a logged-in user. It fetches a list of all
 * upcoming meetings and enriches each one with the user's specific attendance
 * status (e.g., ANGEMELDET, ABGEMELDET, OFFEN). This data is then passed to
 * `lehrgaenge.jsp` for rendering.
 */
@WebServlet("/lehrgaenge")
public class MeetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingServlet.class);
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		logger.info("Fetching upcoming meetings for user '{}' (ID: {})", user.getUsername(), user.getId());

		List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);

		request.setAttribute("meetings", meetings);
		logger.debug("Found {} upcoming meetings. Forwarding to lehrgaenge.jsp.", meetings.size());
		request.getRequestDispatcher("/lehrgaenge.jsp").forward(request, response);
	}
}