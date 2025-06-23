package de.technikteam.servlet;

import de.technikteam.dao.MeetingAttachmentDAO;
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

/**
 * Mapped to `/meetingDetails`, this servlet handles GET requests to display the
 * detailed view of a single course meeting. It fetches the core meeting data as
 * well as any associated file attachments, applying role-based filtering for
 * the attachments. The collected data is then forwarded to
 * `meetingDetails.jsp`.
 */
@WebServlet("/meetingDetails")
public class MeetingDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingDetailsServlet.class);
	private MeetingDAO meetingDAO;
	private MeetingAttachmentDAO attachmentDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
		attachmentDAO = new MeetingAttachmentDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String meetingIdParam = request.getParameter("id");
		if (meetingIdParam == null || meetingIdParam.isEmpty()) {
			logger.warn("Bad request to MeetingDetailsServlet: missing ID parameter.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Meeting-ID fehlt.");
			return;
		}

		try {
			int meetingId = Integer.parseInt(meetingIdParam);
			User user = (User) request.getSession().getAttribute("user");
			logger.info("Meeting details for ID {} requested by user '{}'", meetingId, user.getUsername());

			Meeting meeting = meetingDAO.getMeetingById(meetingId);

			if (meeting == null) {
				logger.warn("Meeting with ID {} not found.", meetingId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Meeting nicht gefunden.");
				return;
			}

			// If current user is the leader, they act as an ADMIN for viewing attachments
			String attachmentUserRole = user.getRole();
			if (user.getId() == meeting.getLeaderUserId()) {
				attachmentUserRole = "ADMIN";
				logger.debug("User {} is leader of meeting {}. Granting admin view for attachments.",
						user.getUsername(), meetingId);
			}

			// Fetch attachments for the meeting, respecting the user's role (or leader
			// override)
			request.setAttribute("attachments", attachmentDAO.getAttachmentsForMeeting(meetingId, attachmentUserRole));
			request.setAttribute("meeting", meeting);

			logger.debug("Forwarding to meetingDetails.jsp for meeting '{}'", meeting.getName());
			request.getRequestDispatcher("/meetingDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid meeting ID format: {}", meetingIdParam, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Meeting-ID.");
		}
	}
}