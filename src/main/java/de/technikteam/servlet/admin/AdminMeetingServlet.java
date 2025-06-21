// In: src/main/java/de/technikteam/servlet/admin/AdminMeetingServlet.java
package de.technikteam.servlet.admin;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/admin/meetings")
public class AdminMeetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MeetingDAO meetingDAO;
	private CourseDAO courseDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action") == null ? "list" : req.getParameter("action");
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		Course parentCourse = courseDAO.getCourseById(courseId);
		req.setAttribute("parentCourse", parentCourse);

		try {
			switch (action) {
			case "new":
			case "edit":
				showForm(req, resp);
				break;
			default: // "list"
				listMeetings(req, resp);
				break;
			}
		} catch (Exception e) {
			// Handle exceptions...
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");

		if ("delete".equals(action)) {
			handleDelete(req, resp);
		} else {
			handleCreateOrUpdate(req, resp);
		}
	}

	private void listMeetings(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
		req.setAttribute("meetings", meetings);
		req.getRequestDispatcher("/admin/admin_meeting_list.jsp").forward(req, resp);
	}

	private void showForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if ("edit".equals(req.getParameter("action"))) {
			int meetingId = Integer.parseInt(req.getParameter("meetingId"));
			Meeting meeting = meetingDAO.getMeetingById(meetingId);
			req.setAttribute("meeting", meeting);
		}
		req.getRequestDispatcher("/admin/admin_meeting_form.jsp").forward(req, resp);
	}

	private void handleCreateOrUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		String meetingIdParam = req.getParameter("meetingId");

		try {
			Meeting meeting = new Meeting();
			meeting.setCourseId(courseId);
			meeting.setName(req.getParameter("name"));
			meeting.setLeader(req.getParameter("leader"));
			meeting.setDescription(req.getParameter("description"));
			meeting.setMeetingDateTime(LocalDateTime.parse(req.getParameter("meetingDateTime")));

			if (meetingIdParam != null && !meetingIdParam.isEmpty()) { // UPDATE
				meeting.setId(Integer.parseInt(meetingIdParam));
				if (meetingDAO.updateMeeting(meeting)) {
					AdminLogService.log(adminUser.getUsername(), "MEETING_AKTUALISIERT",
							"Meeting '" + meeting.getName() + "' (ID: " + meeting.getId() + ") wurde aktualisiert.");
				}
			} else { // CREATE
				if (meetingDAO.createMeeting(meeting)) {
					AdminLogService.log(adminUser.getUsername(), "MEETING_GEPLANT",
							"Meeting '" + meeting.getName() + "' für Kurs ID " + courseId + " wurde geplant.");
				}
			}
		} catch (Exception e) {
			/* ... */ }

		resp.sendRedirect(req.getContextPath() + "/admin/meetings?courseId=" + courseId);
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		int meetingId = Integer.parseInt(req.getParameter("meetingId"));
		User adminUser = (User) req.getSession().getAttribute("user");

		Meeting meeting = meetingDAO.getMeetingById(meetingId); // Get name for log before deleting
		if (meetingDAO.deleteMeeting(meetingId)) {
			AdminLogService.log(adminUser.getUsername(), "MEETING_GELÖSCHT",
					"Meeting '" + meeting.getName() + "' (ID: " + meetingId + ") wurde gelöscht.");
		}

		resp.sendRedirect(req.getContextPath() + "/admin/meetings?courseId=" + courseId);
	}
}