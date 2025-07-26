package de.technikteam.servlet;

import com.google.inject.Injector;
import de.technikteam.config.GuiceConfig;
import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.ConfigurationService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@WebServlet("/upload")
@MultipartConfig(maxFileSize = 41943040, // 40MB 
		maxRequestSize = 83886080, // 80MB
		fileSizeThreshold = 1048576 // 1MB
)
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FileUploadServlet.class);

	private FileDAO fileDAO;
	private AttachmentDAO attachmentDAO;
	private ConfigurationService configService;
	private AdminLogService adminLogService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Injector injector = GuiceConfig.getInjectorInstance();
		if (injector == null) {
			throw new ServletException("Guice Injector has not been initialized.");
		}
		this.fileDAO = injector.getInstance(FileDAO.class);
		this.attachmentDAO = injector.getInstance(AttachmentDAO.class);
		this.configService = injector.getInstance(ConfigurationService.class);
		this.adminLogService = injector.getInstance(AdminLogService.class);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");

		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String uploadType = request.getParameter("uploadType");
		if (uploadType == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'uploadType' parameter.");
			return;
		}

		switch (uploadType) {
		case "ADMIN_FILE_CREATE":
			handleAdminFileCreate(request, response, user);
			break;
		case "ADMIN_FILE_UPDATE":
			handleAdminFileUpdate(request, response, user);
			break;
		case "STORAGE_IMAGE":
			response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "This upload type is handled elsewhere.");
			break;
		case "EVENT_ATTACHMENT":
			handleAttachmentUpload(request, response, user, "EVENT", "events");
			break;
		case "MEETING_ATTACHMENT":
			handleAttachmentUpload(request, response, user, "MEETING", "meetings");
			break;
		default:
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown 'uploadType'.");
		}
	}

	private void handleAdminFileCreate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException, ServletException {
		Part filePart = request.getPart("file");
		String redirectUrl = request.getContextPath() + "/admin/dateien";

		try {
			int categoryId = Integer.parseInt(request.getParameter("categoryId"));
			String requiredRole = request.getParameter("requiredRole");
			String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			String sanitizedOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
			String uniqueFileName = UUID.randomUUID().toString() + "-" + sanitizedOriginalFileName;

			File targetFile = new File(configService.getProperty("upload.directory"), uniqueFileName);
			filePart.write(targetFile.getAbsolutePath());

			de.technikteam.model.File newDbFile = new de.technikteam.model.File();
			newDbFile.setFilename(sanitizedOriginalFileName);
			newDbFile.setFilepath(uniqueFileName);
			newDbFile.setCategoryId(categoryId);
			newDbFile.setRequiredRole(requiredRole);

			if (fileDAO.createFile(newDbFile)) {
				adminLogService.log(adminUser.getUsername(), "FILE_UPLOAD",
						"Datei '" + newDbFile.getFilename() + "' hochgeladen.");
				request.getSession().setAttribute("successMessage", "Datei erfolgreich hochgeladen.");
			} else {
				targetFile.delete();
				request.getSession().setAttribute("errorMessage", "DB-Fehler: Datei konnte nicht gespeichert werden.");
			}
		} catch (Exception e) {
			logger.error("Error during admin file creation.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Upload: " + e.getMessage());
		}
		response.sendRedirect(redirectUrl);
	}

	private void handleAdminFileUpdate(HttpServletRequest request, HttpServletResponse response, User adminUser)
			throws IOException, ServletException {
		Part filePart = request.getPart("file");
		String redirectUrl = request.getContextPath() + "/admin/dateien";

		try {
			int fileId = Integer.parseInt(request.getParameter("fileId"));
			de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);
			if (dbFile == null) {
				request.getSession().setAttribute("errorMessage", "Datei zum Aktualisieren nicht gefunden.");
				response.sendRedirect(redirectUrl);
				return;
			}

			File targetFile = new File(configService.getProperty("upload.directory"), dbFile.getFilepath());
			filePart.write(targetFile.getAbsolutePath());

			if (fileDAO.touchFileRecord(dbFile.getId())) {
				adminLogService.log(adminUser.getUsername(), "FILE_UPDATE",
						"Neue Version für Datei '" + dbFile.getFilename() + "' hochgeladen.");
				request.getSession().setAttribute("successMessage", "Neue Version erfolgreich hochgeladen.");
			} else {
				request.getSession().setAttribute("errorMessage",
						"DB-Fehler: Datei-Metadaten konnten nicht aktualisiert werden.");
			}
		} catch (Exception e) {
			logger.error("Error during admin file update.", e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Aktualisieren: " + e.getMessage());
		}
		response.sendRedirect(redirectUrl);
	}

	private void handleAttachmentUpload(HttpServletRequest request, HttpServletResponse response, User adminUser,
			String parentType, String subfolder) throws IOException, ServletException {
		Part filePart = request.getPart("attachment");
		String parentIdStr = request.getParameter("parentId");
		String requiredRole = request.getParameter("requiredRole");
		String redirectUrl = request.getContextPath() + (parentType.equals("EVENT") ? "/admin/veranstaltungen"
				: "/admin/meetings?courseId=" + request.getParameter("courseId"));

		try {
			int parentId = Integer.parseInt(parentIdStr);
			String uploadDir = configService.getProperty("upload.directory") + File.separator + subfolder;
			new File(uploadDir).mkdirs();

			String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
			File targetFile = new File(uploadDir, fileName);
			filePart.write(targetFile.getAbsolutePath());

			Attachment attachment = new Attachment();
			attachment.setParentId(parentId);
			attachment.setParentType(parentType);
			attachment.setFilename(fileName);
			attachment.setFilepath(subfolder + "/" + fileName);
			attachment.setRequiredRole(requiredRole);

			if (attachmentDAO.addAttachment(attachment)) {
				adminLogService.log(adminUser.getUsername(), "ADD_" + parentType + "_ATTACHMENT",
						"Anhang '" + fileName + "' zu " + parentType + " ID " + parentId + " hinzugefügt.");
				request.getSession().setAttribute("successMessage", "Anhang erfolgreich hinzugefügt.");
			} else {
				request.getSession().setAttribute("errorMessage", "Anhang konnte nicht in DB gespeichert werden.");
			}
		} catch (Exception e) {
			logger.error("Error handling attachment upload for type {}", parentType, e);
			request.getSession().setAttribute("errorMessage", "Fehler beim Upload des Anhangs: " + e.getMessage());
		}
		response.sendRedirect(redirectUrl);
	}
}