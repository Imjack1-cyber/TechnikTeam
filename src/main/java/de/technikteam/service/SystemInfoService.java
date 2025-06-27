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
 * A service to gather system statistics from the underlying OS. This version is
 * made more platform-independent and robust for Windows environments.
 */
public class SystemInfoService {
	private static final Logger logger = LogManager.getLogger(SystemInfoService.class);
	private static final long GIGA_BYTE = 1024L * 1024L * 1024L;

	public SystemStatsDTO getSystemStats() {
		SystemStatsDTO stats = new SystemStatsDTO();
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		File root = new File("/");

		// CPU Load (cross-platform)
		stats.setCpuLoad(osBean.getSystemCpuLoad() * 100.0);

		// Physical Memory (cross-platform)
		long totalMemoryBytes = osBean.getTotalPhysicalMemorySize();
		long freeMemoryBytes = osBean.getFreePhysicalMemorySize();
		stats.setTotalMemory(totalMemoryBytes / GIGA_BYTE);
		stats.setUsedMemory((totalMemoryBytes - freeMemoryBytes) / GIGA_BYTE);

		// Disk Space (cross-platform)
		long totalDiskBytes = root.getTotalSpace();
		long usableDiskBytes = root.getUsableSpace();
		stats.setTotalDiskSpace(totalDiskBytes / GIGA_BYTE);
		stats.setUsedDiskSpace((totalDiskBytes - usableDiskBytes) / GIGA_BYTE);

		// Uptime (Linux-specific with fallback)
		stats.setUptime(getSystemUptime());

		// Battery (Linux-specific with fallback)
		stats.setBatteryPercentage(getBatteryPercentage());

		return stats;
	}

	private String getSystemUptime() {
		// This part is Linux-specific, so we wrap it in a try-catch.
		try {
			// On Linux, this file contains the system uptime in seconds.
			String content = new String(Files.readAllBytes(Paths.get("/proc/uptime")));
			double uptimeSeconds = Double.parseDouble(content.split(" ")[0]);
			long days = TimeUnit.SECONDS.toDays((long) uptimeSeconds);
			long hours = TimeUnit.SECONDS.toHours((long) uptimeSeconds) % 24;
			long minutes = TimeUnit.SECONDS.toMinutes((long) uptimeSeconds) % 60;
			return String.format("%d Tage, %d Stunden, %d Minuten", days, hours, minutes);
		} catch (IOException | NumberFormatException e) {
			// This will be caught on Windows or if the file is unreadable.
			logger.warn("Could not read /proc/uptime. Uptime not available. OS might not be Linux.");
			return "Nicht verfügbar";
		}
	}

	private int getBatteryPercentage() {
		// This part is also Linux-specific.
		try {
			// Common path for laptop batteries on Linux.
			String content = new String(Files.readAllBytes(Paths.get("/sys/class/power_supply/BAT0/capacity")));
			return Integer.parseInt(content.trim());
		} catch (IOException | NumberFormatException e) {
			// This will fail on systems without a battery or non-Linux OS.
			logger.trace("Could not read battery status. System might not have a battery or is not Linux.");
			return -1; // Indicates no battery or not available
		}
	}
}