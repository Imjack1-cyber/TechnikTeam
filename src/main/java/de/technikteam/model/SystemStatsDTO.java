package de.technikteam.model;

/**
 * A Data Transfer Object (DTO) for holding system statistics.
 */
public class SystemStatsDTO {
	private double cpuLoad;
	private long totalMemory;
	private long usedMemory;
	private long totalDiskSpace;
	private long usedDiskSpace;
	private String uptime;
	private int batteryPercentage;

	public double getCpuLoad() {
		return cpuLoad;
	}

	public void setCpuLoad(double cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public long getTotalDiskSpace() {
		return totalDiskSpace;
	}

	public void setTotalDiskSpace(long totalDiskSpace) {
		this.totalDiskSpace = totalDiskSpace;
	}

	public long getUsedDiskSpace() {
		return usedDiskSpace;
	}

	public void setUsedDiskSpace(long usedDiskSpace) {
		this.usedDiskSpace = usedDiskSpace;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public int getBatteryPercentage() {
		return batteryPercentage;
	}

	public void setBatteryPercentage(int batteryPercentage) {
		this.batteryPercentage = batteryPercentage;
	}
}