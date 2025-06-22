package de.technikteam.servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import de.technikteam.dao.FileDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapped to `/api/document`, this servlet provides a simple REST-like API for
 * the collaborative text editor. A GET request fetches the latest document
 * content, while a POST request (with the new content in its raw body) updates
 * the document in the database. It interacts with `FileDAO` to persist the
 * content.
 */
@WebServlet("/api/document")
public class DocumentApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DocumentApiServlet.class);
	private FileDAO fileDAO;
	private static final String DOCUMENT_NAME = "realtime_notes";

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	/**
	 * Handles GET requests to fetch the latest content of the shared document.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.trace("GET request for document content '{}'", DOCUMENT_NAME);
		String content = fileDAO.getDocumentContent(DOCUMENT_NAME);

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(content);
	}

	/**
	 * Handles POST requests to update the document content. It reads the raw text
	 * from the request body.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String content = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		logger.trace("POST request to update document content for '{}'. Content length: {}", DOCUMENT_NAME,
				content.length());

		boolean success = fileDAO.updateDocumentContent(DOCUMENT_NAME, content);

		if (success) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			logger.error("Failed to update document content for '{}'", DOCUMENT_NAME);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}