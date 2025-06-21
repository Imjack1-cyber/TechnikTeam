package de.technikteam.servlet.admin;

import java.io.IOException;

import de.technikteam.dao.FileDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/* 
 *  This servlet is uniquely mapped to multiple URL patterns (/admin/categories/create, /admin/categories/update, /admin/categories/delete) to handle specific actions for file categories. It processes POST requests to create, update, or delete a category and then redirects back to the main admin file management page.
 */

@WebServlet({ "/admin/categories/create", "/admin/categories/update", "/admin/categories/delete" })
public class AdminFileCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getServletPath(); // Gibt den Pfad zurück, z.B. /admin/categories/create

		if (action.endsWith("/create")) {
			String categoryName = req.getParameter("categoryName");
			fileDAO.createCategory(categoryName);
		} else if (action.endsWith("/update")) {
			int categoryId = Integer.parseInt(req.getParameter("categoryId"));
			String categoryName = req.getParameter("categoryName");
			fileDAO.updateCategory(categoryId, categoryName);
		} else if (action.endsWith("/delete")) {
			int categoryId = Integer.parseInt(req.getParameter("categoryId"));
			fileDAO.deleteCategory(categoryId);
		}
		resp.sendRedirect(req.getContextPath() + "/admin/files");
	}
}