package de.technikteam.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to /image, this servlet acts as a secure proxy to serve images. It
 * prevents direct filesystem access by taking a file parameter, locating the
 * image within a designated images subdirectory in the upload path, and then
 * streaming it to the browser with the correct Content-Disposition: inline
 * header so it displays directly on the page.
 */
@WebServlet("/image")
public class ImageServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ImageServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String filename = request.getParameter("file");
		if (filename == null || filename.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'file' parameter.");
			return;
		}

		try {
			filename = URLDecoder.decode(filename, "UTF-8");
		} catch (IllegalArgumentException e) {
			logger.warn("Could not decode filename: {}", filename, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid filename encoding.");
			return;
		}

		// Construct the full path to the image file inside the 'images' subdirectory
		File imageFile = new File(AppConfig.UPLOAD_DIRECTORY + File.separator + "images", filename);

		if (!imageFile.exists() || !imageFile.isFile()) {
			logger.warn("Image not found at path: {}", imageFile.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found.");
			return;
		}

		// Determine content type (MIME type) from file extension
		String contentType = getServletContext().getMimeType(imageFile.getName());
		if (contentType == null) {
			contentType = "application/octet-stream"; // Fallback if type is unknown
		}

		response.setContentType(contentType);
		response.setContentLength((int) imageFile.length());

		// Header 'inline' tells the browser to display the file, not to download it.
		response.setHeader("Content-Disposition", "inline; filename=\"" + imageFile.getName() + "\"");

		logger.debug("Serving image: {}", imageFile.getAbsolutePath());

		// Stream the file content to the response
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