package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;

@WebServlet("/dateien")
public class FileServlet extends HttpServlet {
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