package de.technikteam.servlet.admin;

import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

/**
 * Mapped to `/admin/attendance`, this servlet handles all actions related to
 * updating meeting attendance records, primarily called from the modal window
 * on the administrative qualifications matrix (`admin_matrix.jsp`).
 */
@WebServlet("/admin/attendance")
public class AdminAttendanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminAttendanceServlet.class);
	private MeetingAttendanceDAO attendanceDAO;
	private UserDAO userDAO;
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		attendanceDAO = new MeetingAttendanceDAO();
		userDAO = new UserDAO();
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		User adminUser = (User) request.getSession().getAttribute("user");
		String returnTo = request.getParameter("returnTo"); // e.g., "matrix"

		try {
			int userId = Integer.parseInt(request.getParameter("userId"));
			int meetingId = Integer.parseInt(request.getParameter("meetingId"));

			// A checkbox sends "true" if checked, and null if not. This is a standard way
			// to check it.
			boolean attended = "true".equals(request.getParameter("attended"));

			String remarks = request.getParameter("remarks");
			logger.debug("Processing attendance update for user ID {}, meeting ID {}. Attended: {}, Remarks: '{}'",
					userId, meetingId, attended, remarks);

			if (attendanceDAO.setAttendance(userId, meetingId, attended, remarks)) {
				// Fetch details for rich logging
				User targetUser = userDAO.getUserById(userId);
				Meeting meeting = meetingDAO.getMeetingById(meetingId);

				String status = attended ? "TEILGENOMMEN" : "NICHT TEILGENOMMEN";
				String logDetails = String.format(
						"Teilnahme für Nutzer '%s' (ID: %d) bei Meeting '%s' (ID: %d) auf '%s' gesetzt. Bemerkungen: '%s'.",
						(targetUser != null ? targetUser.getUsername() : "N/A"), userId,
						(meeting != null ? meeting.getName() : "N/A"), meetingId, status, remarks);

				AdminLogService.log(adminUser.getUsername(), "UPDATE_ATTENDANCE", logDetails);

				request.getSession().setAttribute("successMessage", "Teilnahmestatus erfolgreich aktualisiert.");
				logger.info("Attendance update successful for user ID {} / meeting ID {}.", userId, meetingId);
			} else {
				request.getSession().setAttribute("errorMessage",
						"Fehler: Teilnahmestatus konnte nicht aktualisiert werden.");
				logger.error("Attendance update failed for user ID {} / meeting ID {}.", userId, meetingId);
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid ID received in AdminAttendanceServlet.", e);
			request.getSession().setAttribute("errorMessage", "Fehler: Ungültige ID empfangen.");
		}

		// Redirect back to the matrix or a default page
		String redirectUrl = request.getContextPath()
				+ ("/matrix".equals(returnTo) ? "/admin/matrix" : "/admin/dashboard");
		logger.debug("Redirecting to {}", redirectUrl);
		response.sendRedirect(redirectUrl);
	}
}