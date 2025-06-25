package de.technikteam.servlet.admin;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Servlet to display a list of all defective items.
 */
@WebServlet("/admin/defects")
public class AdminDefectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private StorageDAO storageDAO;

	@Override
	public void init() {
		storageDAO = new StorageDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<StorageItem> defectiveItems = storageDAO.getDefectiveItems();
		request.setAttribute("defectiveItems", defectiveItems);
		request.getRequestDispatcher("/admin/admin_defect_list.jsp").forward(request, response);
	}
}