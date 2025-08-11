package de.technikteam.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.technikteam.dao.SystemSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SystemSettingsService {

	private final SystemSettingsDAO settingsDAO;
	private final LoadingCache<String, String> settingsCache;

	public static final String MAINTENANCE_MODE_KEY = "maintenance_mode";

	@Autowired
	public SystemSettingsService(SystemSettingsDAO settingsDAO) {
		this.settingsDAO = settingsDAO;
		this.settingsCache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES)
				.build(key -> settingsDAO.getSetting(key));
	}

	public boolean isMaintenanceModeEnabled() {
		String value = settingsCache.get(MAINTENANCE_MODE_KEY);
		return "true".equalsIgnoreCase(value);
	}

	public void setMaintenanceMode(boolean isEnabled) {
		settingsDAO.updateSetting(MAINTENANCE_MODE_KEY, String.valueOf(isEnabled));
		settingsCache.invalidate(MAINTENANCE_MODE_KEY); // Invalidate cache immediately
	}

	public String getMaintenanceModeStatus() {
		return settingsDAO.getSetting(MAINTENANCE_MODE_KEY);
	}
}