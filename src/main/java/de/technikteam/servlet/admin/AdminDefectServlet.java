package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Singleton
public class AdminDefectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final StorageDAO storageDAO;

	@Inject
	public AdminDefectServlet(StorageDAO storageDAO) {
		this.storageDAO = storageDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<StorageItem> defectiveItems = storageDAO.getDefectiveItems();
		request.setAttribute("defectiveItems", defectiveItems);
		request.getRequestDispatcher("/views/admin/admin_defect_list.jsp").forward(request, response);
	}
}