package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class MeetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingServlet.class);
	private final MeetingDAO meetingDAO;

	@Inject
	public MeetingServlet(MeetingDAO meetingDAO) {
		this.meetingDAO = meetingDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		logger.info("Fetching upcoming meetings for user '{}' (ID: {})", user.getUsername(), user.getId());

		List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);

		request.setAttribute("meetings", meetings);
		logger.debug("Found {} upcoming meetings. Forwarding to lehrgaenge.jsp.", meetings.size());
		request.getRequestDispatcher("/views/public/lehrgaenge.jsp").forward(request, response);
	}
}