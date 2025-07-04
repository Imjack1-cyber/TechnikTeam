package de.technikteam.servlet.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/api/admin/resource-calendar")
public class ResourceCalendarApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private Gson gson;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
				.setPrettyPrinting().create();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// FullCalendar sends start and end parameters
		String startParam = req.getParameter("start");
		String endParam = req.getParameter("end");

		// We can parse them to optimize the DB query
		LocalDate start = LocalDate.parse(startParam.substring(0, 10));
		LocalDate end = LocalDate.parse(endParam.substring(0, 10));

		List<Map<String, Object>> reservations = eventDAO.getReservationsForCalendar(start, end);

		Set<Map<String, Object>> resources = new HashSet<>();
		List<Map<String, Object>> events = new ArrayList<>();

		for (Map<String, Object> res : reservations) {
			// Add unique items to the resources set
			Map<String, Object> resource = new HashMap<>();
			resource.put("id", res.get("item_id").toString());
			resource.put("title", res.get("item_name").toString());
			resources.add(resource);

			// Create the calendar event
			Map<String, Object> event = new HashMap<>();
			event.put("resourceId", res.get("item_id").toString());
			event.put("title", res.get("event_name").toString());
			event.put("start", res.get("event_datetime").toString());
			event.put("end", res.get("end_datetime").toString());
			event.put("url", req.getContextPath() + "/veranstaltungen/details?id=" + res.get("event_id"));
			events.add(event);
		}

		Map<String, Object> responseData = new HashMap<>();
		responseData.put("resources", new ArrayList<>(resources));
		responseData.put("events", events);

		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(gson.toJson(responseData));
	}
}