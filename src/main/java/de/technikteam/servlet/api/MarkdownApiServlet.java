package de.technikteam.servlet.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Singleton
public class MarkdownApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MarkdownApiServlet.class);
	private final FileDAO fileDAO;
	private final AdminLogService adminLogService;

	@Inject
	public MarkdownApiServlet(FileDAO fileDAO, AdminLogService adminLogService) {
		this.fileDAO = fileDAO;
		this.adminLogService = adminLogService;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated.");
			return;
		}

		User user = (User) session.getAttribute("user");
		if (!user.getPermissions().contains("FILE_UPDATE") && !user.getPermissions().contains("ACCESS_ADMIN_PANEL")) {
			logger.warn("User '{}' tried to save markdown file without FILE_UPDATE permission.", user.getUsername());
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (!CSRFUtil.isTokenValid(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token.");
			return;
		}

		String fileIdParam = request.getParameter("fileId");
		String content = request.getParameter("content");

		if (fileIdParam == null || content == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters.");
			return;
		}

		try {
			int fileId = Integer.parseInt(fileIdParam);
			de.technikteam.model.File dbFile = fileDAO.getFileById(fileId);

			if (dbFile == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
				return;
			}

			boolean contentUpdated = fileDAO.updateFileContent(dbFile.getFilepath(), content);
			boolean recordTouched = fileDAO.touchFileRecord(fileId);

			if (contentUpdated && recordTouched) {
				adminLogService.log(user.getUsername(), "UPDATE_MARKDOWN_FILE",
						"Inhalt der Datei '" + dbFile.getFilename() + "' (ID: " + fileId + ") aktualisiert.");
				session.setAttribute("successMessage", "Änderungen erfolgreich gespeichert.");
			} else {
				session.setAttribute("errorMessage", "Fehler beim Speichern der Datei.");
			}

			response.sendRedirect(request.getContextPath() + "/editor?fileId=" + fileId);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fileId format.");
		}
	}
}