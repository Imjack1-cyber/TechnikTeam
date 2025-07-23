package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.ReportDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.DashboardDataDTO;

/**
 * A service class that aggregates data from multiple DAOs to populate the
 * DashboardDataDTO for the admin dashboard API.
 */
public class AdminDashboardService {
	private final EventDAO eventDAO = new EventDAO();
	private final StorageDAO storageDAO = new StorageDAO();
	private final AdminLogDAO adminLogDAO = new AdminLogDAO();
	private final ReportDAO reportDAO = new ReportDAO();

	private static final int WIDGET_LIMIT = 5;
	private static final int TREND_MONTHS = 12;

	public DashboardDataDTO getDashboardData() {
		DashboardDataDTO dto = new DashboardDataDTO();

		dto.setUpcomingEvents(eventDAO.getUpcomingEvents(WIDGET_LIMIT));
		dto.setLowStockItems(storageDAO.getLowStockItems(WIDGET_LIMIT));
		dto.setRecentLogs(adminLogDAO.getRecentLogs(WIDGET_LIMIT));
		dto.setEventTrendData(reportDAO.getEventCountByMonth(TREND_MONTHS));

		return dto;
	}
}