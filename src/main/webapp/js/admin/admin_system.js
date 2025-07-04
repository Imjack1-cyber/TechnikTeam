document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const apiUrl = `${contextPath}/api/admin/system-stats`;

	const cpuProgress = document.getElementById('cpu-progress');
	const cpuText = document.getElementById('cpu-text');
	const ramProgress = document.getElementById('ram-progress');
	const ramText = document.getElementById('ram-text');
	const diskProgress = document.getElementById('disk-progress');
	const diskText = document.getElementById('disk-text');
	const uptimeText = document.getElementById('uptime-text');
	const batteryCard = document.getElementById('battery-card');
	const batteryProgress = document.getElementById('battery-progress');
	const batteryText = document.getElementById('battery-text');

	const formatGigaBytes = (gb) => {
		if (gb === 0) return '0 GB';
		if (gb < 1) return `${(gb * 1024).toFixed(0)} MB`;
		return `${gb.toFixed(2)} GB`;
	};

	const updateUI = (stats) => {
		const cpuPercent = stats.cpuLoad.toFixed(1);
		cpuProgress.style.width = cpuPercent + '%';
		cpuText.textContent = cpuPercent + '%';

		if (stats.totalMemory > 0) {
			const ramPercent = (stats.usedMemory / stats.totalMemory) * 100;
			ramProgress.style.width = ramPercent.toFixed(1) + '%';
			ramText.textContent = `${formatGigaBytes(stats.usedMemory)} / ${formatGigaBytes(stats.totalMemory)}`;
		}

		if (stats.totalDiskSpace > 0) {
			const diskPercent = (stats.usedDiskSpace / stats.totalDiskSpace) * 100;
			diskProgress.style.width = diskPercent.toFixed(1) + '%';
			diskText.textContent = `${formatGigaBytes(stats.usedDiskSpace)} / ${formatGigaBytes(stats.totalDiskSpace)}`;
		}

		uptimeText.textContent = stats.uptime;

		if (stats.batteryPercentage >= 0) {
			batteryCard.style.display = 'block';
			const batteryPercent = stats.batteryPercentage;
			batteryProgress.style.width = batteryPercent + '%';
			batteryText.textContent = batteryPercent + '%';
		} else {
			batteryCard.style.display = 'none';
		}
	};

	const fetchStats = async () => {
		try {
			const response = await fetch(apiUrl);
			if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
			const data = await response.json();
			updateUI(data);
		} catch (error) {
			console.error("Could not fetch system stats:", error);
			cpuText.textContent = "Fehler";
			ramText.textContent = "Fehler";
			diskText.textContent = "Fehler";
			uptimeText.textContent = "Fehler";
		}
	};

	fetchStats();
	let intervalId = setInterval(fetchStats, 5000);

	document.addEventListener("visibilitychange", () => {
		if (document.hidden) {
			clearInterval(intervalId);
		} else {
			fetchStats();
			intervalId = setInterval(fetchStats, 5000);
		}
	});
});