// src/main/java/de/technikteam/api/v1/FileResource.java
package de.technikteam.api.v1;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.ApiResponse;
import de.technikteam.model.FileCategory;
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
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@MultipartConfig(maxFileSize = 20971520, // 20MB
		maxRequestSize = 52428800, // 50MB
		fileSizeThreshold = 1048576 // 1MB
)
public class FileResource extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FileResource.class);

	private final FileDAO fileDAO;
	private final AdminLogService adminLogService;
	private final ConfigurationService configService;
	private final Gson gson;

	@Inject
	public FileResource(FileDAO fileDAO, AdminLogService adminLogService, ConfigurationService configService,
			Gson gson) {
		this.fileDAO = fileDAO;
		this.adminLogService = adminLogService;
		this.configService = configService;
		this.gson = gson;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("FILE_READ")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			// GET /api/v1/files -> Get all files grouped by category
			User adminProxy = new User();
			adminProxy.setPermissions(new HashSet<>());
			adminProxy.getPermissions().add("ACCESS_ADMIN_PANEL");
			Map<String, List<de.technikteam.model.File>> groupedFiles = fileDAO
					.getAllFilesGroupedByCategory(adminProxy);
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "Files retrieved", groupedFiles));
		} else if ("/categories".equals(pathInfo)) {
			// GET /api/v1/files/categories
			List<FileCategory> categories = fileDAO.getAllCategories();
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Categories retrieved", categories));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/")) {
			handleCreateFile(req, resp);
		} else if (pathInfo.equals("/categories")) {
			handleCreateCategory(req, resp);
		} else {
			// POST /api/v1/files/{id} for updating a file version
			Integer fileId = parseIdFromPath(pathInfo);
			if (fileId != null) {
				handleUpdateFile(req, resp, fileId);
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
			}
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		if (pathParts.length == 2 && pathParts[1].equals("category")) {
			Integer fileId = parseId(pathParts[0]);
			if (fileId != null) {
				handleReassignCategory(req, resp, fileId);
			} else {
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID.");
			}
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request.");
			return;
		}

		String[] pathParts = pathInfo.substring(1).split("/");
		if (pathParts.length == 1) { // DELETE /api/v1/files/{id}
			Integer fileId = parseId(pathParts[0]);
			if (fileId != null)
				handleDeleteFile(resp, (User) req.getAttribute("user"), fileId);
			else
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid file ID.");
		} else if (pathParts.length == 2 && "categories".equals(pathParts[0])) { // DELETE /api/v1/files/categories/{id}
			Integer categoryId = parseId(pathParts[1]);
			if (categoryId != null)
				handleDeleteCategory(resp, (User) req.getAttribute("user"), categoryId);
			else
				sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid category ID.");
		} else {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
		}
	}

	private void handleCreateFile(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("FILE_CREATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		Part filePart = req.getPart("file");
		try {
			int categoryId = Integer.parseInt(req.getParameter("categoryId"));
			String requiredRole = req.getParameter("requiredRole");
			String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String sanitizedFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
			String uniqueFileName = UUID.randomUUID().toString() + "-" + sanitizedFileName;

			File targetFile = new File(configService.getProperty("upload.directory"), uniqueFileName);
			filePart.write(targetFile.getAbsolutePath());

			de.technikteam.model.File newDbFile = new de.technikteam.model.File();
			newDbFile.setFilename(sanitizedFileName);
			newDbFile.setFilepath(uniqueFileName);
			newDbFile.setCategoryId(categoryId);
			newDbFile.setRequiredRole(requiredRole);

			if (fileDAO.createFile(newDbFile)) {
				adminLogService.log(adminUser.getUsername(), "FILE_UPLOAD_API",
						"File '" + newDbFile.getFilename() + "' uploaded via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_CREATED,
						new ApiResponse(true, "File uploaded successfully.", newDbFile));
			} else {
				targetFile.delete();
				sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"Could not save file record to database.");
			}
		} catch (Exception e) {
			logger.error("Error creating file via API", e);
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Error during upload: " + e.getMessage());
		}
	}

	private void handleUpdateFile(HttpServletRequest req, HttpServletResponse resp, int fileId)
			throws IOException, ServletException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("FILE_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);
		if (dbFile == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "File to update not found.");
			return;
		}

		Part filePart = req.getPart("file");
		File targetFile = new File(configService.getProperty("upload.directory"), dbFile.getFilepath());
		filePart.write(targetFile.getAbsolutePath());

		if (fileDAO.touchFileRecord(fileId)) {
			adminLogService.log(adminUser.getUsername(), "FILE_UPDATE_API",
					"New version for file '" + dbFile.getFilename() + "' uploaded via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK, new ApiResponse(true, "File version updated.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not update file metadata.");
		}
	}

	private void handleReassignCategory(HttpServletRequest req, HttpServletResponse resp, int fileId)
			throws IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("FILE_UPDATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		try {
			String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			Map<String, Integer> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, Integer>>() {
			}.getType());
			int newCategoryId = payload.get("categoryId");

			if (fileDAO.reassignFileToCategory(fileId, newCategoryId)) {
				adminLogService.log(adminUser.getUsername(), "FILE_REASSIGN_API",
						"File ID " + fileId + " reassigned to category " + newCategoryId + " via API.");
				sendJsonResponse(resp, HttpServletResponse.SC_OK,
						new ApiResponse(true, "File reassigned successfully.", null));
			} else {
				sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "File not found or reassign failed.");
			}
		} catch (Exception e) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body.");
		}
	}

	private void handleCreateCategory(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		User adminUser = (User) req.getAttribute("user");
		if (adminUser == null || !adminUser.getPermissions().contains("FILE_CREATE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		String jsonPayload = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		Map<String, String> payload = gson.fromJson(jsonPayload, new TypeToken<Map<String, String>>() {
		}.getType());
		String categoryName = payload.get("name");

		if (categoryName == null || categoryName.trim().isEmpty()) {
			sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Category name cannot be empty.");
			return;
		}

		if (fileDAO.createCategory(categoryName)) {
			adminLogService.log(adminUser.getUsername(), "CREATE_FILE_CATEGORY_API",
					"File category '" + categoryName + "' created via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_CREATED, new ApiResponse(true, "Category created.", null));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_CONFLICT,
					"Category could not be created. Name might already exist.");
		}
	}

	private void handleDeleteFile(HttpServletResponse resp, User adminUser, int fileId) throws IOException {
		if (!adminUser.getPermissions().contains("FILE_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}
		de.technikteam.model.File file = fileDAO.getFileById(fileId);
		if (file == null) {
			sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "File not found.");
			return;
		}
		if (fileDAO.deleteFile(fileId)) {
			adminLogService.log(adminUser.getUsername(), "FILE_DELETE_API",
					"File '" + file.getFilename() + "' deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "File deleted.", Map.of("deletedId", fileId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete file.");
		}
	}

	private void handleDeleteCategory(HttpServletResponse resp, User adminUser, int categoryId) throws IOException {
		if (!adminUser.getPermissions().contains("FILE_DELETE")) {
			sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}
		String categoryName = fileDAO.getCategoryNameById(categoryId);
		if (fileDAO.deleteCategory(categoryId)) {
			adminLogService.log(adminUser.getUsername(), "DELETE_FILE_CATEGORY_API",
					"File category '" + categoryName + "' deleted via API.");
			sendJsonResponse(resp, HttpServletResponse.SC_OK,
					new ApiResponse(true, "Category deleted.", Map.of("deletedId", categoryId)));
		} else {
			sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete category.");
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