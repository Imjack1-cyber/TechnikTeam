package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.technikteam.api.v1.dto.MaintenanceStatusDTO;
import de.technikteam.dao.SystemSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SystemSettingsService {

	private final SystemSettingsDAO settingsDAO;
	private final NotificationService notificationService;
	private final LoadingCache<String, String> settingsCache;

	public static final String MAINTENANCE_MODE_KEY = "maintenance_mode";
	public static final String MAINTENANCE_MESSAGE_KEY = "maintenance_message";

	@Autowired
	public SystemSettingsService(SystemSettingsDAO settingsDAO, NotificationService notificationService) {
		this.settingsDAO = settingsDAO;
		this.notificationService = notificationService;
		this.settingsCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
				.build(key -> settingsDAO.getSetting(key));
	}

	public MaintenanceStatusDTO getMaintenanceStatus() {
		String mode = settingsCache.get(MAINTENANCE_MODE_KEY);
		String message = settingsCache.get(MAINTENANCE_MESSAGE_KEY);
		return new MaintenanceStatusDTO(mode != null ? mode : "OFF", message);
	}

	public void setMaintenanceMode(MaintenanceStatusDTO status) {
		settingsDAO.updateSetting(MAINTENANCE_MODE_KEY, status.mode());
		settingsDAO.updateSetting(MAINTENANCE_MESSAGE_KEY, status.message());
		settingsCache.invalidateAll(); // Invalidate cache immediately
		notificationService.broadcastSystemStatusUpdate(status);
	}
}