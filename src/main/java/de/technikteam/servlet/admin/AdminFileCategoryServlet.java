package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.FileCategoryDAO;
import de.technikteam.model.FileCategory;

@WebServlet("/admin/file-categories")
public class AdminFileCategoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private FileCategoryDAO categoryDAO;

	@Override
	public void init() {
		categoryDAO = new FileCategoryDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<FileCategory> categories = categoryDAO.getAll();
		req.setAttribute("categories", categories);
		req.getRequestDispatcher("/admin/admin_file_categories.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		if ("create".equals(action)) {
			FileCategory cat = new FileCategory();
			cat.setName(req.getParameter("name"));
			categoryDAO.create(cat);
		} else if ("delete".equals(action)) {
			int id = Integer.parseInt(req.getParameter("id"));
			categoryDAO.delete(id);
		}
		resp.sendRedirect(req.getContextPath() + "/admin/file-categories");
	}
}