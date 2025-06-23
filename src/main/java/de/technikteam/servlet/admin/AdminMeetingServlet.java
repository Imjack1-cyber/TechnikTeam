package de.technikteam.servlet.admin;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttachmentDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.MeetingAttachment;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.ServletUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/admin/meetings")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
public class AdminMeetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminMeetingServlet.class);
	private MeetingDAO meetingDAO;
	private CourseDAO courseDAO;
	private MeetingAttachmentDAO attachmentDAO;
	private UserDAO userDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
		courseDAO = new CourseDAO();
		attachmentDAO = new MeetingAttachmentDAO();
		userDAO = new UserDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if ("edit".equals(action)) {
			showEditForm(req, resp);
		} else {
			listMeetings(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");
		String action;

		if (req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/")) {
			action = ServletUtils.getPartValue(req.getPart("action"));
		} else {
			action = req.getParameter("action");
		}

		logger.debug("AdminMeetingServlet received POST with action: {}", action);

		switch (action) {
		case "create":
		case "update":
			handleCreateOrUpdate(req, resp);
			break;
		case "delete":
			handleDelete(req, resp);
			break;
		case "deleteAttachment":
			handleDeleteAttachment(req, resp);
			break;
		default:
			logger.warn("Unknown action received in doPost: {}", action);
			resp.sendRedirect(req.getContextPath() + "/admin/courses");
			break;
		}
	}

	private void listMeetings(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		logger.info("Listing all meetings for course ID: {}", courseId);
		Course parentCourse = courseDAO.getCourseById(courseId);
		List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
		List<User> allUsers = userDAO.getAllUsers();

		req.setAttribute("parentCourse", parentCourse);
		req.setAttribute("meetings", meetings);
		req.setAttribute("allUsers", allUsers);

		req.getRequestDispatcher("/admin/admin_meeting_list.jsp").forward(req, resp);
	}

	private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int meetingId = Integer.parseInt(req.getParameter("id"));
			logger.info("Showing edit form for meeting ID: {}", meetingId);
			Meeting meeting = meetingDAO.getMeetingById(meetingId);
			if (meeting != null) {
				req.setAttribute("meeting", meeting);
				req.setAttribute("attachments", attachmentDAO.getAttachmentsForMeeting(meetingId, "ADMIN")); // Admins
																												// see
																												// all
				req.setAttribute("allUsers", userDAO.getAllUsers()); // For leader dropdown
				req.getRequestDispatcher("/admin/admin_meeting_edit.jsp").forward(req, resp);
			} else {
				req.getSession().setAttribute("errorMessage", "Meeting nicht gefunden.");
				resp.sendRedirect(req.getContextPath() + "/admin/courses");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid ID for meeting edit form.", e);
			req.getSession().setAttribute("errorMessage", "Ungültige Meeting-ID.");
			resp.sendRedirect(req.getContextPath() + "/admin/courses");
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		User adminUser = (User) req.getSession().getAttribute("user");
		String action = ServletUtils.getPartValue(req.getPart("action"));
		boolean isUpdate = "update".equals(action);

		int courseId = Integer.parseInt(ServletUtils.getPartValue(req.getPart("courseId")));
		int meetingId = 0;
		if (isUpdate) {
			meetingId = Integer.parseInt(ServletUtils.getPartValue(req.getPart("meetingId")));
		}

		String redirectUrl = isUpdate ? req.getContextPath() + "/admin/meetings?action=edit&id=" + meetingId
				: req.getContextPath() + "/admin/meetings?courseId=" + courseId;

		try {
			Meeting meeting = new Meeting();
			meeting.setCourseId(courseId);
			meeting.setName(ServletUtils.getPartValue(req.getPart("name")));
			meeting.setDescription(ServletUtils.getPartValue(req.getPart("description")));

			String leaderIdStr = ServletUtils.getPartValue(req.getPart("leaderUserId"));
			if (leaderIdStr != null && !leaderIdStr.isEmpty()) {
				meeting.setLeaderUserId(Integer.parseInt(leaderIdStr));
			}

			String startDateTimeStr = ServletUtils.getPartValue(req.getPart("meetingDateTime"));
			if (startDateTimeStr != null && !startDateTimeStr.isEmpty()) {
				meeting.setMeetingDateTime(LocalDateTime.parse(startDateTimeStr));
			}
			String endDateTimeStr = ServletUtils.getPartValue(req.getPart("endDateTime"));
			if (endDateTimeStr != null && !endDateTimeStr.isEmpty()) {
				meeting.setEndDateTime(LocalDateTime.parse(endDateTimeStr));
			}

			Course parentCourse = courseDAO.getCourseById(courseId);
			String parentCourseName = (parentCourse != null) ? parentCourse.getName() : "N/A";

			int newMeetingId = -1;
			if (isUpdate) {
				meeting.setId(meetingId);
				if (meetingDAO.updateMeeting(meeting)) {
					AdminLogService.log(adminUser.getUsername(), "UPDATE_MEETING", "Meeting '" + meeting.getName()
							+ "' (ID: " + meetingId + ") für Lehrgang '" + parentCourseName + "' aktualisiert.");
					req.getSession().setAttribute("successMessage", "Meeting erfolgreich aktualisiert.");
				} else {
					req.getSession().setAttribute("infoMessage", "Keine Änderungen am Meeting vorgenommen.");
				}
			} else { // CREATE
				newMeetingId = meetingDAO.createMeeting(meeting);
				if (newMeetingId > 0) {
					AdminLogService.log(adminUser.getUsername(), "CREATE_MEETING", "Meeting '" + meeting.getName()
							+ "' (ID: " + newMeetingId + ") für Lehrgang '" + parentCourseName + "' geplant.");
					req.getSession().setAttribute("successMessage", "Neues Meeting erfolgreich geplant.");
					redirectUrl = req.getContextPath() + "/admin/meetings?action=edit&id=" + newMeetingId;
				} else {
					req.getSession().setAttribute("errorMessage", "Meeting konnte nicht erstellt werden.");
				}
			}

			Part filePart = req.getPart("attachment");
			if (filePart != null && filePart.getSize() > 0) {
				int targetMeetingId = isUpdate ? meetingId : newMeetingId;
				if (targetMeetingId > 0) {
					String requiredRole = ServletUtils.getPartValue(req.getPart("requiredRole"));
					handleAttachmentUpload(filePart, targetMeetingId, requiredRole, adminUser, req);
				}
			}

		} catch (DateTimeParseException | NumberFormatException e) {
			logger.error("Invalid data format in meeting form.", e);
			req.getSession().setAttribute("errorMessage", "Ungültiges Datenformat.");
		} catch (Exception e) {
			logger.error("Error creating/updating meeting.", e);
			req.getSession().setAttribute("errorMessage", "Fehler: " + e.getMessage());
		}

		resp.sendRedirect(redirectUrl);
	}

	private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		int meetingId = Integer.parseInt(req.getParameter("meetingId"));
		User adminUser = (User) req.getSession().getAttribute("user");
		logger.warn("Attempting to delete meeting ID {} from course ID {}", meetingId, courseId);

		Meeting meeting = meetingDAO.getMeetingById(meetingId);
		if (meetingDAO.deleteMeeting(meetingId)) {
			String meetingName = (meeting != null) ? meeting.getName() : "N/A";
			String courseName = (meeting != null && meeting.getParentCourseName() != null)
					? meeting.getParentCourseName()
					: "N/A";
			String logDetails = String.format("Meeting '%s' (ID: %d) vom Lehrgang '%s' (Kurs-ID: %d) wurde gelöscht.",
					meetingName, meetingId, courseName, courseId);
			AdminLogService.log(adminUser.getUsername(), "DELETE_MEETING", logDetails);
			req.getSession().setAttribute("successMessage", "Meeting erfolgreich gelöscht.");
		} else {
			req.getSession().setAttribute("errorMessage", "Meeting konnte nicht gelöscht werden.");
		}

		resp.sendRedirect(req.getContextPath() + "/admin/meetings?courseId=" + courseId);
	}

	private void handleDeleteAttachment(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getSession().getAttribute("user");
		int attachmentId = Integer.parseInt(req.getParameter("attachmentId"));
		logger.warn("Attempting to delete attachment ID {}", attachmentId);

		MeetingAttachment attachment = attachmentDAO.getAttachmentById(attachmentId);
		if (attachment != null) {
			int meetingId = attachment.getMeetingId();
			File physicalFile = new File(AppConfig.UPLOAD_DIRECTORY, attachment.getFilepath());

			if (physicalFile.exists()) {
				physicalFile.delete();
			}

			if (attachmentDAO.deleteAttachment(attachmentId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_MEETING_ATTACHMENT",
						"Anhang '" + attachment.getFilename() + "' von Meeting ID " + meetingId + " gelöscht.");
				req.getSession().setAttribute("successMessage", "Anhang gelöscht.");
			} else {
				req.getSession().setAttribute("errorMessage", "Anhang konnte nicht aus DB gelöscht werden.");
			}
			resp.sendRedirect(req.getContextPath() + "/admin/meetings?action=edit&id=" + meetingId);
		} else {
			req.getSession().setAttribute("errorMessage", "Anhang nicht gefunden.");
			resp.sendRedirect(req.getContextPath() + "/admin/courses");
		}
	}

	private void handleAttachmentUpload(Part filePart, int meetingId, String requiredRole, User adminUser,
			HttpServletRequest req) throws IOException {
		String uploadDir = AppConfig.UPLOAD_DIRECTORY + File.separator + "meetings";
		new File(uploadDir).mkdirs();

		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		File targetFile = new File(uploadDir, fileName);
		filePart.write(targetFile.getAbsolutePath());

		MeetingAttachment attachment = new MeetingAttachment();
		attachment.setMeetingId(meetingId);
		attachment.setFilename(fileName);
		// Always use forward slashes for URL paths
		attachment.setFilepath("meetings/" + fileName);
		attachment.setRequiredRole(requiredRole);
		if (attachmentDAO.addAttachment(attachment)) {
			logger.info("Attachment '{}' uploaded for meeting ID {} by '{}'", fileName, meetingId,
					adminUser.getUsername());
			String logDetails = String.format("Anhang '%s' zu Meeting ID %d hinzugefügt. Sichtbar für: %s.", fileName,
					meetingId, requiredRole);
			AdminLogService.log(adminUser.getUsername(), "ADD_MEETING_ATTACHMENT", logDetails);
			// Do not set success message here, let the main method handle it
		} else {
			req.getSession().setAttribute("errorMessage", "Anhang konnte nicht in DB gespeichert werden.");
		}
	}

}