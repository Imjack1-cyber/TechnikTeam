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
	private static final boolean IS_LINUX = System.getProperty("os.name").toLowerCase().startsWith("linux");

	public SystemStatsDTO getSystemStats() {
		SystemStatsDTO stats = new SystemStatsDTO();
		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		File root = new File("/");

		stats.setCpuLoad(osBean.getSystemCpuLoad() * 100.0);

		long totalMemoryBytes = osBean.getTotalPhysicalMemorySize();
		long freeMemoryBytes = osBean.getFreePhysicalMemorySize();
		stats.setTotalMemory(totalMemoryBytes / GIGA_BYTE);
		stats.setUsedMemory((totalMemoryBytes - freeMemoryBytes) / GIGA_BYTE);

		long totalDiskBytes = root.getTotalSpace();
		long usableDiskBytes = root.getUsableSpace();
		stats.setTotalDiskSpace(totalDiskBytes / GIGA_BYTE);
		stats.setUsedDiskSpace((totalDiskBytes - usableDiskBytes) / GIGA_BYTE);

		stats.setUptime(getSystemUptime());

		stats.setBatteryPercentage(getBatteryPercentage());

		return stats;
	}

	private String getSystemUptime() {
		if (!IS_LINUX) {
			logger.trace("Uptime not available on non-Linux OS.");
			return "Nicht verfügbar";
		}
		try {
			String content = new String(Files.readAllBytes(Paths.get("/proc/uptime")));
			double uptimeSeconds = Double.parseDouble(content.split(" ")[0]);
			long days = TimeUnit.SECONDS.toDays((long) uptimeSeconds);
			long hours = TimeUnit.SECONDS.toHours((long) uptimeSeconds) % 24;
			long minutes = TimeUnit.SECONDS.toMinutes((long) uptimeSeconds) % 60;
			return String.format("%d Tage, %d Stunden, %d Minuten", days, hours, minutes);
		} catch (IOException | NumberFormatException e) {
			logger.warn("Could not read /proc/uptime, even on a Linux-like system.");
			return "Nicht verfügbar";
		}
	}

	private int getBatteryPercentage() {
		if (!IS_LINUX) {
			logger.trace("Battery status not available on non-Linux OS.");
			return -1;
		}
		try {
			String content = new String(Files.readAllBytes(Paths.get("/sys/class/power_supply/BAT0/capacity")));
			return Integer.parseInt(content.trim());
		} catch (IOException | NumberFormatException e) {
			logger.trace("Could not read battery status. System might not have a battery or is not Linux.");
			return -1;
		}
	}
}