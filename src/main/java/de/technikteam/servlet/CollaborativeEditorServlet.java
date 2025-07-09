package de.technikteam.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Mapped to `/editor-page`, this is a very simple servlet whose only purpose is
 * to forward the user to the `collaborative_editor.jsp` page. All the dynamic
 * functionality for the editor is handled client-side by JavaScript and the
 * `DocumentApiServlet`.
 */
@WebServlet("/editor-page")
public class CollaborativeEditorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("/views/public/collaborative_editor.jsp").forward(request, response);
	}
}