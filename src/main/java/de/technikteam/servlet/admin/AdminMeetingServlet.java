package de.technikteam.servlet.admin;

import de.technikteam.config.AppConfig;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttachmentDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.MeetingAttachment;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@WebServlet("/admin/meetings/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 20, maxRequestSize = 1024 * 1024 * 50)
public class AdminMeetingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminMeetingServlet.class);
	private MeetingDAO meetingDAO;
	private CourseDAO courseDAO;
	private MeetingAttachmentDAO attachmentDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
		courseDAO = new CourseDAO();
		attachmentDAO = new MeetingAttachmentDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo != null && pathInfo.equals("/edit")) {
			showEditForm(req, resp);
		} else {
			listMeetings(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		req.setCharacterEncoding("UTF-8");
		String contentType = req.getContentType();

		if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
			handleCreateOrUpdate(req, resp);
		} else {
			String action = req.getParameter("action");
			logger.debug("AdminMeetingServlet received non-multipart POST with action: {}", action);
			if ("delete".equals(action)) {
				handleDelete(req, resp);
			} else if ("deleteAttachment".equals(action)) {
				handleDeleteAttachment(req, resp);
			} else {
				logger.warn("Unknown non-multipart action received in doPost: {}", action);
				resp.sendRedirect(req.getContextPath() + "/admin/courses");
			}
		}
	}

	private void handleCreateOrUpdate(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		User adminUser = (User) req.getSession().getAttribute("user");
		String action = getPartValue(req.getPart("action"));
		boolean isUpdate = "update".equals(action);

		int courseId = Integer.parseInt(getPartValue(req.getPart("courseId")));
		String redirectUrl = req.getContextPath() + "/admin/meetings?courseId=" + courseId;

		try {
			Meeting meeting = new Meeting();
			meeting.setCourseId(courseId);
			meeting.setName(getPartValue(req.getPart("name")));
			meeting.setLeader(getPartValue(req.getPart("leader")));
			meeting.setDescription(getPartValue(req.getPart("description")));

			String startDateTimeStr = getPartValue(req.getPart("meetingDateTime"));
			if (startDateTimeStr != null && !startDateTimeStr.isEmpty()) {
				meeting.setMeetingDateTime(LocalDateTime.parse(startDateTimeStr));
			}
			String endDateTimeStr = getPartValue(req.getPart("endDateTime"));
			if (endDateTimeStr != null && !endDateTimeStr.isEmpty()) {
				meeting.setEndDateTime(LocalDateTime.parse(endDateTimeStr));
			}

			Course parentCourse = courseDAO.getCourseById(courseId);
			String parentCourseName = (parentCourse != null) ? parentCourse.getName() : "N/A";

			if (isUpdate) {
				int meetingId = Integer.parseInt(getPartValue(req.getPart("meetingId")));
				meeting.setId(meetingId);
				if (meetingDAO.updateMeeting(meeting)) {
					AdminLogService.log(adminUser.getUsername(), "UPDATE_MEETING", "Meeting '" + meeting.getName()
							+ "' (ID: " + meetingId + ") für Lehrgang '" + parentCourseName + "' aktualisiert.");
					req.getSession().setAttribute("successMessage", "Meeting erfolgreich aktualisiert.");
				} else {
					req.getSession().setAttribute("infoMessage", "Keine Änderungen am Meeting vorgenommen.");
				}
				redirectUrl = req.getContextPath() + "/admin/meetings/edit?id=" + meetingId + "&courseId=" + courseId;
			} else { // CREATE
				int newMeetingId = meetingDAO.createMeeting(meeting);
				if (newMeetingId > 0) {
					AdminLogService.log(adminUser.getUsername(), "CREATE_MEETING", "Meeting '" + meeting.getName()
							+ "' (ID: " + newMeetingId + ") für Lehrgang '" + parentCourseName + "' geplant.");
					req.getSession().setAttribute("successMessage", "Neues Meeting erfolgreich geplant.");
				} else {
					req.getSession().setAttribute("errorMessage", "Meeting konnte nicht erstellt werden.");
				}
			}

			Part filePart = req.getPart("attachment");
			if (filePart != null && filePart.getSize() > 0) {
				int meetingIdForAttachment = isUpdate ? meeting.getId() : meetingDAO.createMeeting(meeting);
				if (meetingIdForAttachment > 0) {
					String requiredRole = getPartValue(req.getPart("requiredRole"));
					handleAttachmentUpload(filePart, meetingIdForAttachment, requiredRole, adminUser, req);
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

	private void listMeetings(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		logger.info("Listing all meetings for course ID: {}", courseId);
		Course parentCourse = courseDAO.getCourseById(courseId);
		List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
		req.setAttribute("parentCourse", parentCourse);
		req.setAttribute("meetings", meetings);
		req.getRequestDispatcher("/admin/admin_meeting_list.jsp").forward(req, resp);
	}

	private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int meetingId = Integer.parseInt(req.getParameter("id"));
			User user = (User) req.getSession().getAttribute("user");
			logger.info("Showing edit form for meeting ID: {}", meetingId);
			Meeting meeting = meetingDAO.getMeetingById(meetingId);
			if (meeting != null) {
				req.setAttribute("meeting", meeting);
				req.setAttribute("attachments", attachmentDAO.getAttachmentsForMeeting(meetingId, user.getRole()));
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
		int meetingId = Integer.parseInt(req.getParameter("meetingId"));
		int courseId = Integer.parseInt(req.getParameter("courseId"));
		logger.warn("Attempting to delete attachment ID {} from meeting ID {}", attachmentId, meetingId);

		MeetingAttachment attachment = attachmentDAO.getAttachmentById(attachmentId);
		if (attachment != null) {
			File physicalFile = new File(AppConfig.UPLOAD_DIRECTORY, attachment.getFilepath());
			if (physicalFile.exists()) {
				physicalFile.delete();
			}
			if (attachmentDAO.deleteAttachment(attachmentId)) {
				AdminLogService.log(adminUser.getUsername(), "DELETE_MEETING_ATTACHMENT",
						"Anhang '" + attachment.getFilename() + "' von Meeting ID " + meetingId + " gelöscht.");
				req.getSession().setAttribute("successMessage", "Anhang gelöscht.");
			}
		} else {
			req.getSession().setAttribute("errorMessage", "Anhang nicht gefunden.");
		}
		resp.sendRedirect(req.getContextPath() + "/admin/meetings/edit?id=" + meetingId + "&courseId=" + courseId);
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
		attachment.setFilepath("meetings" + File.separator + fileName);
		attachment.setRequiredRole(requiredRole);
		if (attachmentDAO.addAttachment(attachment)) {
			logger.info("Attachment '{}' uploaded for meeting ID {} by '{}'", fileName, meetingId,
					adminUser.getUsername());
			String logDetails = String.format("Anhang '%s' zu Meeting ID %d hinzugefügt. Sichtbar für: %s.", fileName,
					meetingId, requiredRole);
			AdminLogService.log(adminUser.getUsername(), "ADD_MEETING_ATTACHMENT", logDetails);
			req.getSession().setAttribute("successMessage", "Anhang '" + fileName + "' erfolgreich hochgeladen.");
		}
	}

	private String getPartValue(Part part) throws IOException {
		if (part == null) {
			return null;
		}
		try (InputStream inputStream = part.getInputStream();
				Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
			return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
		}
	}
}