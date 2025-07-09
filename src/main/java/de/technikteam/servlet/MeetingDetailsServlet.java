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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapped to `/meetingDetails`, this servlet handles GET requests to display the
 * detailed view of a single course meeting. It fetches the core meeting data as
 * well as any associated file attachments, applying role-based filtering for
 * the attachments.
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

			boolean hasAdminRights = user.getPermissions().contains("ACCESS_ADMIN_PANEL")
					|| user.getPermissions().contains("COURSE_READ");
			boolean isLeader = user.getId() == meeting.getLeaderUserId();

			boolean isParticipant = meetingDAO.isUserAssociatedWithMeeting(meetingId, user.getId());

			if (!hasAdminRights && !isLeader && !isParticipant) {
				logger.warn("Authorization DENIED for user '{}' trying to access meeting details for ID {}",
						user.getUsername(), meetingId);
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Sie sind nicht berechtigt, diese Meeting-Details anzuzeigen.");
				return;
			}

			String attachmentUserRole = "NUTZER";
			if (hasAdminRights || isLeader) {
				attachmentUserRole = "ADMIN";
				logger.debug("User {} granted admin view for attachments of meeting {}.", user.getUsername(),
						meetingId);
			}

			request.setAttribute("attachments", attachmentDAO.getAttachmentsForMeeting(meetingId, attachmentUserRole));
			request.setAttribute("meeting", meeting);

			logger.debug("Forwarding to meetingDetails.jsp for meeting '{}'", meeting.getName());
			request.getRequestDispatcher("/views/public/meetingDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid meeting ID format: {}", meetingIdParam, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Meeting-ID.");
		}
	}
}