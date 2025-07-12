package de.technikteam.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapped to `/dateien`, this servlet handles the display of the main files and
 * documents page for users. It fetches all files the user is permitted to see,
 * grouped by category.
 */
@WebServlet("/dateien")
public class FileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(FileServlet.class);
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		logger.info("File page requested by user '{}' (Role: {})", user.getUsername(), user.getRoleName());

		Map<String, List<File>> fileData = fileDAO.getAllFilesGroupedByCategory(user);

		request.setAttribute("fileData", fileData);
		logger.debug("Forwarding file data to dateien.jsp.");
		request.getRequestDispatcher("/views/public/dateien.jsp").forward(request, response);
	}
}