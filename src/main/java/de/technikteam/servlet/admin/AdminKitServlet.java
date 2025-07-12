package de.technikteam.servlet.admin;

import com.google.gson.Gson;
import de.technikteam.dao.InventoryKitDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.InventoryKit;
import de.technikteam.model.InventoryKitItem;
import de.technikteam.model.StorageItem;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/kits")
public class AdminKitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(AdminKitServlet.class);
	private InventoryKitDAO kitDAO;
	private StorageDAO storageDAO;
	private Gson gson = new Gson();

	@Override
	public void init() {
		kitDAO = new InventoryKitDAO();
		storageDAO = new StorageDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");

		if ("getKitItems".equals(action)) {
			getKitItemsAsJson(req, resp);
			return;
		}

		// Performance: Fetch all kits and their items in one go to prevent N+1 queries.
		List<InventoryKit> kits = kitDAO.getAllKitsWithItems();
		List<StorageItem> allItems = storageDAO.getAllItems();

		req.setAttribute("kits", kits);
		req.setAttribute("allItems", allItems);
		req.setAttribute("allItemsJson", gson.toJson(allItems));

		req.getRequestDispatcher("/views/admin/admin_kits.jsp").forward(req, resp);
	}

	private void getKitItemsAsJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			int kitId = Integer.parseInt(req.getParameter("id"));
			List<InventoryKitItem> items = kitDAO.getItemsForKit(kitId);
			String jsonResponse = gson.toJson(items);
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().write(jsonResponse);
		} catch (NumberFormatException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid kit ID.");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");

		if (!CSRFUtil.isTokenValid(req)) {
			logger.warn("CSRF token validation failed for kit action.");
			resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		User adminUser = (User) req.getSession().getAttribute("user");
		String action = req.getParameter("action");
		logger.debug("AdminKitServlet received POST with action: {}", action);

		try {
			switch (action) {
			case "create":
				handleCreateKit(req, adminUser);
				break;
			case "update":
				handleUpdateKit(req, adminUser);
				break;
			case "delete":
				handleDeleteKit(req, adminUser);
				break;
			case "updateKitItems":
				handleUpdateKitItems(req, adminUser);
				break;
			default:
				logger.warn("Unknown kit action: {}", action);
				break;
			}
		} catch (Exception e) {
			logger.error("Error processing kit action '{}'", action, e);
			req.getSession().setAttribute("errorMessage", "Ein Fehler ist aufgetreten: " + e.getMessage());
		}

		resp.sendRedirect(req.getContextPath() + "/admin/kits");
	}

	private void handleCreateKit(HttpServletRequest req, User adminUser) {
		InventoryKit kit = new InventoryKit();
		kit.setName(req.getParameter("name"));
		kit.setDescription(req.getParameter("description"));
		kit.setLocation(req.getParameter("location"));
		int newId = kitDAO.createKit(kit);
		if (newId > 0) {
			AdminLogService.log(adminUser.getUsername(), "CREATE_KIT",
					"Kit '" + kit.getName() + "' (ID: " + newId + ") erstellt.");
			req.getSession().setAttribute("successMessage", "Kit erfolgreich erstellt.");
		} else {
			req.getSession().setAttribute("errorMessage", "Kit konnte nicht erstellt werden.");
		}
	}

	private void handleUpdateKit(HttpServletRequest req, User adminUser) {
		InventoryKit kit = new InventoryKit();
		kit.setId(Integer.parseInt(req.getParameter("id")));
		kit.setName(req.getParameter("name"));
		kit.setDescription(req.getParameter("description"));
		kit.setLocation(req.getParameter("location"));

		if (kitDAO.updateKit(kit)) {
			AdminLogService.log(adminUser.getUsername(), "UPDATE_KIT",
					"Kit '" + kit.getName() + "' (ID: " + kit.getId() + ") aktualisiert.");
			req.getSession().setAttribute("successMessage", "Kit erfolgreich aktualisiert.");
		} else {
			req.getSession().setAttribute("errorMessage", "Kit konnte nicht aktualisiert werden.");
		}
	}

	private void handleDeleteKit(HttpServletRequest req, User adminUser) {
		int kitId = Integer.parseInt(req.getParameter("id"));
		InventoryKit kit = kitDAO.getKitById(kitId);
		if (kitDAO.deleteKit(kitId)) {
			AdminLogService.log(adminUser.getUsername(), "DELETE_KIT",
					"Kit '" + (kit != null ? kit.getName() : "N/A") + "' (ID: " + kitId + ") gelöscht.");
			req.getSession().setAttribute("successMessage", "Kit erfolgreich gelöscht.");
		} else {
			req.getSession().setAttribute("errorMessage", "Kit konnte nicht gelöscht werden.");
		}
	}

	private void handleUpdateKitItems(HttpServletRequest req, User adminUser) {
		int kitId = Integer.parseInt(req.getParameter("kitId"));
		String[] itemIds = req.getParameterValues("itemIds");
		String[] quantities = req.getParameterValues("quantities");

		if (kitDAO.updateKitItems(kitId, itemIds, quantities)) {
			AdminLogService.log(adminUser.getUsername(), "UPDATE_KIT_ITEMS",
					"Inhalt für Kit ID " + kitId + " aktualisiert.");
			req.getSession().setAttribute("successMessage", "Kit-Inhalt erfolgreich gespeichert.");
		} else {
			req.getSession().setAttribute("errorMessage", "Fehler beim Speichern des Kit-Inhalts.");
		}
	}
}