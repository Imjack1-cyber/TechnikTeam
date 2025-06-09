package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.StorageDAO;
import de.technikteam.model.StorageItem;

// Servlet for the storage page.
@WebServlet("/lager")
public class StorageServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StorageDAO storageDAO;

	public void init() {
		storageDAO = new StorageDAO();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Fetch all items grouped by their location.
		Map<String, List<StorageItem>> storageData = storageDAO.getAllItemsGroupedByLocation();

		request.setAttribute("storageData", storageData);
		request.getRequestDispatcher("lager.jsp").forward(request, response);
	}
}