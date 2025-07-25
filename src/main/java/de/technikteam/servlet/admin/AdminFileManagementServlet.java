package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.FileDAO;
import de.technikteam.model.File;
import de.technikteam.model.FileCategory;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Singleton
public class AdminFileManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminFileManagementServlet.class);
	private final FileDAO fileDAO;

	@Inject
	public AdminFileManagementServlet(FileDAO fileDAO) {
		this.fileDAO = fileDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		logger.info("Admin file management page requested by user '{}' (Role: {})", user.getUsername(),
				user.getRoleName());

		User adminProxy = new User();
		adminProxy.setPermissions(new HashSet<>());
		adminProxy.getPermissions().add("ACCESS_ADMIN_PANEL");

		Map<String, List<File>> groupedFiles = fileDAO.getAllFilesGroupedByCategory(adminProxy);
		List<FileCategory> allCategories = fileDAO.getAllCategories();

		request.setAttribute("groupedFiles", groupedFiles);
		request.setAttribute("allCategories", allCategories);

		logger.debug("Forwarding file and category data to admin_files.jsp.");
		request.getRequestDispatcher("/views/admin/admin_files.jsp").forward(request, response);
	}
}