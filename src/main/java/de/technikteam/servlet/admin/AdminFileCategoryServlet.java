package de.technikteam.servlet.admin;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.FileDAO;

@WebServlet({ "/admin/categories/create", "/admin/categories/update", "/admin/categories/delete" })
public class AdminFileCategoryServlet extends HttpServlet {
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