package de.technikteam.service;

import de.technikteam.dao.AdminLogDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.ReportDAO;
import de.technikteam.dao.StorageDAO;
import de.technikteam.model.DashboardDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
	private final EventDAO eventDAO;
	private final StorageDAO storageDAO;
	private final AdminLogDAO adminLogDAO;
	private final ReportDAO reportDAO;

	private static final int WIDGET_LIMIT = 5;
	private static final int TREND_MONTHS = 12;

	@Autowired
	public AdminDashboardService(EventDAO eventDAO, StorageDAO storageDAO, AdminLogDAO adminLogDAO,
			ReportDAO reportDAO) {
		this.eventDAO = eventDAO;
		this.storageDAO = storageDAO;
		this.adminLogDAO = adminLogDAO;
		this.reportDAO = reportDAO;
	}

	public DashboardDataDTO getDashboardData() {
		DashboardDataDTO dto = new DashboardDataDTO();
		dto.setUpcomingEvents(eventDAO.getUpcomingEvents(WIDGET_LIMIT));
		dto.setLowStockItems(storageDAO.getLowStockItems(WIDGET_LIMIT));
		dto.setRecentLogs(adminLogDAO.getRecentLogs(WIDGET_LIMIT));
		dto.setEventTrendData(reportDAO.getEventCountByMonth(TREND_MONTHS));
		return dto;
	}
}