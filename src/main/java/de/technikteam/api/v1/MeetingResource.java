// src/main/java/de/technikteam/api/v1/MeetingResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.Attachment;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.ConfigurationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@MultipartConfig(maxFileSize = 20971520, // 20MB
		maxRequestSize = 52428800, // 50MB
		fileSizeThreshold = 1048576 // 1MB
)
public class MeetingResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingResource.class);

	private final MeetingDAO meetingDAO;
	private final AttachmentDAO attachmentDAO;
	private final AdminLogService adminLogService;
	private final ConfigurationService configService;
	private final Gson gson;

	@Inject
	public MeetingResource(MeetingDAO meetingDAO, AttachmentDAO attachmentDAO, AdminLogService adminLogService,
			ConfigurationService configService, Gson gson) {
		this.meetingDAO = meetingDAO;
		this.attachmentDAO = attachmentDAO;
		this.adminLogService = adminLogService;
		this.configService = configService;
		this.gson = gson;
	}

	/**
	 * Handles GET requests. GET /api/v1/meetings?courseId={id} -> Returns a list of
	 * meetings for a course. GET /api/v1/meetings/{id} -> Returns a single meeting
	 * with its attachments.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		String courseIdParam = req.getParameter("courseId");

		if (pathInfo == null || pathInfo.equals("/")) {
			if (courseIdParam != null) {
				try {
					int courseId = Integer.parseInt(courseIdParam);
					List<Meeting> meetings = meetingDAO.getMeetingsForCourse(courseId);
					sendJsonResponse(resp, HttpServletResponse.SC_OK,
							new ApiResponse(true, "Meetings retrieved successfully", meetings));
				} catch (NumberFormatException e) {
					sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid courseId parameter.");
				}
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required 'courseId' query parameter.");
			}
		} else {
			Integer meetingId = parseIdFromPath(pathInfo);
			if (meetingId != null) {
				Meeting meeting = meetingDAO.getMeetingById(meetingId);
				if (meeting != null) {
					List<Attachment> attachments = attachmentDAO.getAttachmentsForParent("MEETING", meetingId, "ADMIN");
					Map<String, Object> responseData = new HashMap<>();
					responseData.put("meeting", meeting);
					responseData.put("attachments", attachments);
					sendJsonResponse(resp, HttpServletResponse.SC_OK,
							new ApiResponse(true, "Meeting details retrieved", responseData));
				} else {
					sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Meeting not found");
				}
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid meeting ID format in URL.");
			}
		}
	}

	/**
	 * Handles POST requests. Since this endpoint needs to support file uploads, it
	 * uses POST for both create and update, and expects multipart/form-data. POST
	 * /api/v1/meetings -> Creates a new meeting. POST /api/v1/meetings/{id} ->
	 * Updates an existing meeting.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		String pathInfo = req.getPathInfo();
		boolean isUpdate = (pathInfo != null && !pathInfo.equals("/"));

		String requiredPermission = isUpdate ? "COURSE_UPDATE" : "COURSE_CREATE";
		if (adminUser == null || !adminUser.getPermissions().contains(requiredPermission)) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			Meeting meeting = new Meeting();
			meeting.setCourseId(Integer.parseInt(req.getParameter("courseId")));
			meeting.setName(req.getParameter("name"));
			meeting.setDescription(req.getParameter("description"));
			meeting.setLocation(req.getParameter("location"));
			meeting.setMeetingDateTime(LocalDateTime.parse(req.getParameter("meetingDateTime")));

			String endDateTimeStr = req.getParameter("endDateTime");
			if (endDateTimeStr != null && !endDateTimeStr.isEmpty()) {
				meeting.setEndDateTime(LocalDateTime.parse(endDateTimeStr));
			}

			String leaderIdStr = req.getParameter("leaderUserId");
			if (leaderIdStr != null && !leaderIdStr.isEmpty()) {
				meeting.setLeaderUserId(Integer.parseInt(leaderIdStr));
			}

			int meetingId;
			if (isUpdate) {
				meetingId = parseIdFromPath(pathInfo);
				meeting.setId(meetingId);
				meetingDAO.updateMeeting(meeting);
				adminLogService.log(adminUser.getUsername(), "UPDATE_MEETING_API",
						"Meeting '" + meeting.getName() + "' (ID: " + meetingId + ") updated via API.");
			} else {
				meetingId = meetingDAO.createMeeting(meeting);
				adminLogService.log(adminUser.getUsername(), "CREATE_MEETING_API",
						"Meeting '" + meeting.getName() + "' (ID: " + meetingId + ") created via API.");
			}

			Part filePart = req.getPart("attachment");
			if (filePart != null && filePart.getSize() > 0) {
				handleAttachmentUpload(filePart, meetingId, req.getParameter("requiredRole"), adminUser);
			}

			Meeting resultMeeting = meetingDAO.getMeetingById(meetingId);
			sendJsonResponse(resp, isUpdate ? HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED,
					new ApiResponse(true, "Meeting saved successfully", resultMeeting));

		} catch (DateTimeParseException | NumberFormatException e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid data format for date or number.");
		} catch (Exception e) {
			logger.error("Error saving meeting via API", e);
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"An internal error occurred: " + e.getMessage());
		}
	}

	/**
	 * Handles DELETE requests. DELETE /api/v1/meetings/{id} -> Deletes a meeting.
	 * DELETE /api/v1/meetings/{meetingId}/attachments/{attachmentId} -> Deletes an
	 * attachment.
	 */
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("COURSE_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing ID for delete operation.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");

		// Handle attachment deletion:
		// /api/v1/meetings/{meetingId}/attachments/{attachmentId}
		if (pathParts.length == 3 && pathParts[1].equals("attachments")) {
			Integer meetingId = parseId(pathParts[0]);
			Integer attachmentId = parseId(pathParts[2]);
			if (meetingId != null && attachmentId != null) {
				handleDeleteAttachment(resp, adminUser, attachmentId);
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid meeting or attachment ID.");
			}
			return;
		}

		// Handle meeting deletion: /api/v1/meetings/{id}
		if (pathParts.length == 1) {
			Integer meetingId = parseId(pathParts[0]);
			if (meetingId != null) {
				handleDeleteMeeting(resp, adminUser, meetingId);
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid meeting ID.");
			}
			return;
		}

		sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid DELETE endpoint format.");
	}

	private void handleDeleteMeeting(HttpServletResponse resp, User adminUser, int meetingId) throws IOException {
		Meeting meeting = meetingDAO.getMeetingById(meetingId);
		if (meeting == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Meeting to delete not found.");
			return;
		}

		if (meetingDAO.deleteMeeting(meetingId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_MEETING_API",
					"Meeting '" + meeting.getName() + "' (ID: " + meetingId + ") deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Meeting deleted successfully.", Map.of("deletedId", meetingId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete meeting.");
		}
	}

	private void handleDeleteAttachment(HttpServletResponse resp, User adminUser, int attachmentId) throws IOException {
		Attachment attachment = attachmentDAO.getAttachmentById(attachmentId);
		if (attachment == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Attachment not found.");
			return;
		}

		File physicalFile = new File(configService.getProperty("upload.directory"), attachment.getFilepath());
		if (physicalFile.exists()) {
			physicalFile.delete();
		}

		if (attachmentDAO.deleteAttachment(attachmentId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_ATTACHMENT_API", "Attachment '"
					+ attachment.getFilename() + "' deleted from meeting " + attachment.getParentId() + " via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Attachment deleted.", Map.of("deletedId", attachmentId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Failed to delete attachment from database.");
		}
	}

	private void handleAttachmentUpload(Part filePart, int meetingId, String requiredRole, User adminUser)
			throws IOException, SQLException {
		String uploadDir = configService.getProperty("upload.directory") + File.separator + "meetings";
		new File(uploadDir).mkdirs();
		String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
		File targetFile = new File(uploadDir, fileName);
		filePart.write(targetFile.getAbsolutePath());
		Attachment attachment = new Attachment();
		attachment.setParentId(meetingId);
		attachment.setParentType("MEETING");
		attachment.setFilename(fileName);
		attachment.setFilepath("meetings/" + fileName);
		attachment.setRequiredRole(requiredRole);
		if (attachmentDAO.addAttachment(attachment)) {
			adminLogService.log(adminUser.getUsername(), "ADD_MEETING_ATTACHMENT_API",
					String.format("Attachment '%s' added to meeting ID %d via API.", fileName, meetingId));
		} else {
			targetFile.delete(); // Rollback file creation if DB fails
			throw new SQLException("Failed to save attachment to database.");
		}
	}

	private Integer parseId(String pathSegment) {
		try {
			return Integer.parseInt(pathSegment);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseIdFromPath(String pathInfo) {
		if (pathInfo == null || pathInfo.length() <= 1)
			return null;
		return parseId(pathInfo.substring(1));
	}

	private void sendJsonResponse(HttpServletResponse resp, int statusCode, ApiResponse apiResponse)
			throws IOException {
		resp.setStatus(statusCode);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		try (PrintWriter out = resp.getWriter()) {
			out.print(gson.toJson(apiResponse));
			out.flush();
		}
	}

	private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
		sendJsonResponse(resp, statusCode, new ApiResponse(false, message, null));
	}
}