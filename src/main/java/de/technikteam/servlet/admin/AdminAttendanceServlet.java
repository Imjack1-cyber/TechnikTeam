package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

@Singleton
public class AdminAttendanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminAttendanceServlet.class);
	private final MeetingAttendanceDAO attendanceDAO;
	private final UserDAO userDAO;
	private final MeetingDAO meetingDAO;
	private final AdminLogService adminLogService;

	@Inject
	public AdminAttendanceServlet(MeetingAttendanceDAO attendanceDAO, UserDAO userDAO, MeetingDAO meetingDAO,
			AdminLogService adminLogService) {
		this.attendanceDAO = attendanceDAO;
		this.userDAO = userDAO;
		this.meetingDAO = meetingDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}
		request.setCharacterEncoding("UTF-8");
		User adminUser = (User) request.getSession().getAttribute("user");
		String returnTo = request.getParameter("returnTo");
		try {
			int userId = Integer.parseInt(request.getParameter("userId"));
			int meetingId = Integer.parseInt(request.getParameter("meetingId"));
			boolean attended = "true".equals(request.getParameter("attended"));
			String remarks = request.getParameter("remarks");

			if (attendanceDAO.setAttendance(userId, meetingId, attended, remarks)) {
				User targetUser = userDAO.getUserById(userId);
				Meeting meeting = meetingDAO.getMeetingById(meetingId);
				String status = attended ? "TEILGENOMMEN" : "NICHT TEILGENOMMEN";
				String logDetails = String.format(
						"Teilnahme für Nutzer '%s' (ID: %d) bei Meeting '%s' (ID: %d) auf '%s' gesetzt. Bemerkungen: '%s'.",
						(targetUser != null ? targetUser.getUsername() : "N/A"), userId,
						(meeting != null ? meeting.getName() : "N/A"), meetingId, status, remarks);
				adminLogService.log(adminUser.getUsername(), "UPDATE_ATTENDANCE", logDetails);
				request.getSession().setAttribute("successMessage", "Teilnahmestatus erfolgreich aktualisiert.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"Fehler: Teilnahmestatus konnte nicht aktualisiert werden.");
			}
		} catch (NumberFormatException e) {
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID empfangen.");
		}
		String redirectUrl = request.getContextPath()
				+ ("/matrix".equals(returnTo) ? "/admin/matrix" : "/admin/dashboard");
		response.sendRedirect(redirectUrl);
	}
}