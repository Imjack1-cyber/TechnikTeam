package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dateien")
public class FileServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, List<File>> fileData = fileDAO.getAllFilesGroupedByCategory();
		request.setAttribute("fileData", fileData);
		request.getRequestDispatcher("dateien.jsp").forward(request, response);
	}
}