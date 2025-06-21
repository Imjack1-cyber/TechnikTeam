package de.technikteam.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.config.AppConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * Mapped to /download, this servlet handles file download requests. It takes a file parameter, securely constructs the file path, and streams the file from the server's upload directory to the user's browser, setting the correct headers for the download to start.
 */

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DownloadServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String fileName = request.getParameter("file");
		if (fileName == null || fileName.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fehlender 'file' Parameter.");
			return;
		}

		File file = new File(AppConfig.UPLOAD_DIRECTORY, fileName);
		if (!file.exists() || !file.isFile()) {
			logger.error("Download failed: File not found at {}", file.getAbsolutePath());
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Datei nicht gefunden.");
			return;
		}

		response.setContentType("application/octet-stream");
		response.setContentLength((int) file.length());

		// Stellt sicher, dass Umlaute etc. im Dateinamen korrekt behandelt werden
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"",
				URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20"));
		response.setHeader(headerKey, headerValue);

		logger.info("Serving file for download: {}", file.getAbsolutePath());
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