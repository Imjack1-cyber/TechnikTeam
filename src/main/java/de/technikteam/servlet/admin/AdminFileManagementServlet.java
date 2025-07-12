package de.technikteam.servlet.admin;

import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Handles the main administrative page for file management. It lists all files
 * and categories, and provides the forms for creating/updating them.
 */
@WebServlet("/admin/dateien")
public class AdminFileManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileManagementServlet.class);
	private FileDAO fileDAO;

	@Override
	public void init() {
		fileDAO = new FileDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		logger.info("Admin file management page requested by user '{}' (Role: {})", user.getUsername(),
				user.getRoleName());

		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(user);
		List<FileCategory> allCategories = fileDAO.getAllCategories();

		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("allCategories", allCategories);

		logger.debug("Forwarding file and category data to admin_files.jsp.");
		request.getRequestDispatcher("/views/admin/admin_files.jsp").forward(request, response);
	}
}