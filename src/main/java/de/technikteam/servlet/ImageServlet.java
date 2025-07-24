package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.model.User;
import de.technikteam.service.ConfigurationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Singleton
public class ImageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ImageServlet.class);
	private final ConfigurationService configService;

	@Inject
	public ImageServlet(ConfigurationService configService) {
		this.configService = configService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required.");
			return;
		}
		User user = (User) session.getAttribute("user");

		String filename = request.getParameter("file");
		if (filename == null || filename.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'file' parameter.");
			return;
		}

		try {
			filename = URLDecoder.decode(filename, StandardCharsets.UTF_8.toString());
		} catch (IllegalArgumentException e) {
			logger.warn("Could not decode filename: {}", filename, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid filename encoding.");
			return;
		}

		File imageUploadDir = new File(configService.getProperty("upload.directory"), "images");
		String imageDirCanonicalPath = imageUploadDir.getCanonicalPath();

		File imageFile = new File(imageUploadDir, filename);
		String requestedFileCanonicalPath = imageFile.getCanonicalPath();

		if (!requestedFileCanonicalPath.startsWith(imageDirCanonicalPath)) {
			String username = (user != null) ? user.getUsername() : "GUEST";
			logger.fatal(
					"CRITICAL: Path Traversal Attack Detected! User: '{}' attempted to access '{}' via image servlet.",
					username, requestedFileCanonicalPath);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
			return;
		}

		if (!imageFile.exists() || !imageFile.isFile()) {
			logger.warn("Image not found at path: {}", imageFile.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found.");
			return;
		}

		String contentType = getServletContext().getMimeType(imageFile.getName());
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		response.setContentType(contentType);
		response.setContentLengthLong(imageFile.length());
		response.setHeader("Content-Disposition", "inline; filename=\"" + imageFile.getName() + "\"");

		logger.debug("Serving image: {} with content type {}", imageFile.getAbsolutePath(), contentType);

		try (FileInputStream inStream = new FileInputStream(imageFile);
				OutputStream outStream = response.getOutputStream()) {

			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
		}
	}
}