// Path: src/main/java/de/technikteam/servlet/DocumentApiServlet.java
package de.technikteam.servlet;

import java.io.IOException;
import de.technikteam.dao.FileDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/document")
public class DocumentApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FileDAO fileDAO;
    private static final String DOCUMENT_NAME = "realtime_notes";

    @Override
    public void init() {
        fileDAO = new FileDAO();
    }

    /**
     * Handles GET requests to fetch the latest document content.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String content = fileDAO.getDocumentContent(DOCUMENT_NAME);
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(content);
    }

    /**
     * Handles POST requests to update the document content.
     * Reads the raw text from the request body.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Read the text content sent from the browser
        String content = request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
        
        // Save it to the database
        boolean success = fileDAO.updateDocumentContent(DOCUMENT_NAME, content);
        
        // Send back a success or failure status
        if (success) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}