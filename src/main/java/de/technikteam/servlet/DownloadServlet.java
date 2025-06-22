package de.technikteam.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to `/download`, this servlet handles file download requests for both
 * general files and meeting attachments. It takes a `file` parameter which
 * corresponds to a path relative to the application's upload directory. It
 * performs basic security checks, constructs the full file path, and streams
 * the file from the server's filesystem to the user's browser, setting the
 * correct headers to trigger a download dialog.
 */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String relativePath = request.getParameter("file");
		if (relativePath == null || relativePath.isEmpty()) {
			logger.warn("Download request rejected: missing 'file' parameter.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fehlender 'file' Parameter.");
			return;
		}

		User user = (User) request.getSession().getAttribute("user");

		// Basic security measure to prevent path traversal attacks (e.g., ../../)
		if (relativePath.contains("..")) {
			logger.warn("Potential path traversal attack from user '{}' for file '{}'. Access denied.",
					(user != null ? user.getUsername() : "GUEST"), relativePath);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Zugriff verweigert.");
			return;
		}

		File file = new File(AppConfig.UPLOAD_DIRECTORY, relativePath);
		if (!file.exists() || !file.isFile()) {
			logger.error("Download failed: File not found at resolved path {}", file.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datei nicht gefunden.");
			return;
		}

		response.setContentType("application/octet-stream");
		response.setContentLengthLong(file.length());

		String headerKey = "Content-Disposition";
		// URL-encode the filename to handle special characters and spaces correctly.
		String headerValue = String.format("attachment; filename=\"%s\"",
				URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString()));
		response.setHeader(headerKey, headerValue);

		logger.info("User '{}' is downloading file: {}. Size: {} bytes.", (user != null ? user.getUsername() : "GUEST"),
				file.getAbsolutePath(), file.length());

		try (FileInputStream inStream = new FileInputStream(file);
				OutputStream outStream = response.getOutputStream()) {

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		}
	}
}