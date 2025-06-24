package de.technikteam.service;

import com.sun.management.OperatingSystemMXBean;
import de.technikteam.model.SystemStatsDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * A service to gather system statistics from the underlying OS.
 */
public class SystemInfoService {
	private static final Logger logger = LogManager.getLogger(SystemInfoService.class);
	private static final long MEGA_BYTE = 1024L * 1024L;

	public SystemStatsDTO getSystemStats() {
		SystemStatsDTO stats = new SystemStatsDTO();
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		Runtime runtime = Runtime.getRuntime();
		File root = new File("/");

		// CPU
		stats.setCpuLoad(osBean.getSystemCpuLoad() * 100.0);

		// Memory
		stats.setTotalMemory(runtime.totalMemory() / MEGA_BYTE);
		stats.setUsedMemory((runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE);

		// Disk
		stats.setTotalDiskSpace(root.getTotalSpace() / MEGA_BYTE);
		stats.setUsedDiskSpace((root.getTotalSpace() - root.getUsableSpace()) / MEGA_BYTE);

		// Uptime (Linux specific)
		stats.setUptime(getSystemUptime());

		// Battery (Linux specific)
		stats.setBatteryPercentage(getBatteryPercentage());

		return stats;
	}

	private String getSystemUptime() {
		try {
			String content = new String(Files.readAllBytes(Paths.get("/proc/uptime")));
			double uptimeSeconds = Double.parseDouble(content.split(" ")[0]);
			long days = TimeUnit.SECONDS.toDays((long) uptimeSeconds);
			long hours = TimeUnit.SECONDS.toHours((long) uptimeSeconds) % 24;
			long minutes = TimeUnit.SECONDS.toMinutes((long) uptimeSeconds) % 60;
			return String.format("%d Tage, %d Stunden, %d Minuten", days, hours, minutes);
		} catch (IOException | NumberFormatException e) {
			logger.warn("Could not read /proc/uptime. Uptime not available. OS might not be Linux.", e);
			return "Nicht verfügbar";
		}
	}

	private int getBatteryPercentage() {
		try {
			// Common path for laptop batteries on Linux
			String content = new String(Files.readAllBytes(Paths.get("/sys/class/power_supply/BAT0/capacity")));
			return Integer.parseInt(content.trim());
		} catch (IOException | NumberFormatException e) {
			logger.trace("Could not read battery status. System might not have a battery or is not Linux.");
			return -1; // Indicates no battery
		}
	}
}