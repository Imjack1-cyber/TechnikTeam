package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.InventoryKitItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class PackKitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(PackKitServlet.class);

	private final InventoryKitDAO kitDAO;

	@Inject
	public PackKitServlet(InventoryKitDAO kitDAO) {
		this.kitDAO = kitDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String kitIdParam = request.getParameter("kitId");
		if (kitIdParam == null || kitIdParam.isEmpty()) {
			logger.warn("Pack kit page requested with no kitId.");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Kit-ID fehlt.");
			return;
		}

		try {
			int kitId = Integer.parseInt(kitIdParam);
			InventoryKit kit = kitDAO.getKitById(kitId);

			if (kit == null) {
				logger.warn("Pack kit page requested for non-existent kitId: {}", kitId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Kit nicht gefunden.");
				return;
			}

			List<InventoryKitItem> kitItems = kitDAO.getItemsForKit(kitId);

			request.setAttribute("kit", kit);
			request.setAttribute("kitItems", kitItems);

			request.getRequestDispatcher("/views/public/pack_kit.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid kitId format: {}", kitIdParam, e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Kit-ID.");
		}
	}
}