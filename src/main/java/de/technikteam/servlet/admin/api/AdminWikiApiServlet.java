package de.technikteam.servlet.admin.api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.config.Permissions;
import de.technikteam.model.User;
import de.technikteam.service.WikiService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Singleton
public class AdminWikiApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final WikiService wikiService;
	private final Gson gson = new Gson();

	@Inject
	public AdminWikiApiServlet(WikiService wikiService) {
		this.wikiService = wikiService;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null || !user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
			return;
		}

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Object treeData = wikiService.getWikiTreeAsData();
		response.getWriter().write(gson.toJson(treeData));
	}
}