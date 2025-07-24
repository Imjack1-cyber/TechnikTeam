package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AttachmentDAO;
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

@Singleton
public class MeetingDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingDetailsServlet.class);
	private final MeetingDAO meetingDAO;
	private final AttachmentDAO attachmentDAO;

	@Inject
	public MeetingDetailsServlet(MeetingDAO meetingDAO, AttachmentDAO attachmentDAO) {
		this.meetingDAO = meetingDAO;
		this.attachmentDAO = attachmentDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String meetingIdParam = request.getParameter("id");
		if (meetingIdParam == null || meetingIdParam.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Meeting-ID fehlt.");
			return;
		}

		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		try {
			int meetingId = Integer.parseInt(meetingIdParam);
			logger.info("Meeting details for ID {} requested by user '{}'", meetingId, user.getUsername());

			Meeting meeting = meetingDAO.getMeetingById(meetingId);
			if (meeting == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Meeting nicht gefunden.");
				return;
			}

			boolean hasAdminRights = user.getPermissions().contains("ACCESS_ADMIN_PANEL")
					|| user.getPermissions().contains("COURSE_READ");
			boolean isLeader = user.getId() == meeting.getLeaderUserId();
			boolean isParticipant = meetingDAO.isUserAssociatedWithMeeting(meetingId, user.getId());

			if (!hasAdminRights && !isLeader && !isParticipant) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Sie sind nicht berechtigt, diese Meeting-Details anzuzeigen.");
				return;
			}

			String attachmentUserRole = (hasAdminRights || isLeader) ? "ADMIN" : "NUTZER";
			request.setAttribute("attachments",
					attachmentDAO.getAttachmentsForParent("MEETING", meetingId, attachmentUserRole));
			request.setAttribute("meeting", meeting);
			request.getRequestDispatcher("/views/public/meetingDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid meeting ID format: {}", meetingIdParam, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Meeting-ID.");
		}
	}
}