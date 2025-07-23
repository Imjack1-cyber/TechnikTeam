package de.technikteam.model;

import java.util.List;
import java.util.Map;

/**
 * A Data Transfer Object to encapsulate all data required for the dynamic admin
 * dashboard.
 */
public class DashboardDataDTO {
	private List<Event> upcomingEvents;
	private List<StorageItem> lowStockItems;
	private List<AdminLog> recentLogs;
	private List<Map<String, Object>> eventTrendData;

	// Getters and Setters
	public List<Event> getUpcomingEvents() {
		return upcomingEvents;
	}

	public void setUpcomingEvents(List<Event> upcomingEvents) {
		this.upcomingEvents = upcomingEvents;
	}

	public List<StorageItem> getLowStockItems() {
		return lowStockItems;
	}

	public void setLowStockItems(List<StorageItem> lowStockItems) {
		this.lowStockItems = lowStockItems;
	}

	public List<AdminLog> getRecentLogs() {
		return recentLogs;
	}

	public void setRecentLogs(List<AdminLog> recentLogs) {
		this.recentLogs = recentLogs;
	}

	public List<Map<String, Object>> getEventTrendData() {
		return eventTrendData;
	}

	public void setEventTrendData(List<Map<String, Object>> eventTrendData) {
		this.eventTrendData = eventTrendData;
	}
}