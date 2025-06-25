<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Systemstatus" />
	<c:param name="navType" value="admin" />
</c:import>

<h1>
	<i class="fas fa-server"></i> Systemstatus
</h1>
<p>Live-Statistiken des Servers. Die Daten werden alle 5 Sekunden
	aktualisiert.</p>

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-microchip"></i> CPU Auslastung
		</h2>
		<div class="progress-bar-container">
			<div id="cpu-progress" class="progress-bar"></div>
		</div>
		<p id="cpu-text" class="progress-text">Lade...</p>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-memory"></i> Arbeitsspeicher (RAM)
		</h2>
		<div class="progress-bar-container">
			<div id="ram-progress" class="progress-bar"></div>
		</div>
		<p id="ram-text" class="progress-text">Lade...</p>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-hdd"></i> Festplattenspeicher
		</h2>
		<div class="progress-bar-container">
			<div id="disk-progress" class="progress-bar"></div>
		</div>
		<p id="disk-text" class="progress-text">Lade...</p>
	</div>

	<div class="card" id="battery-card" style="display: none;">
		<h2 class="card-title">
			<i class="fas fa-battery-half"></i> Akku
		</h2>
		<div class="progress-bar-container">
			<div id="battery-progress" class="progress-bar"></div>
		</div>
		<p id="battery-text" class="progress-text">Lade...</p>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-clock"></i> Server Uptime
		</h2>
		<p id="uptime-text"
			style="font-size: 1.5rem; font-weight: 500; color: var(--primary-color); text-align: center;">Lade...</p>
	</div>
</div>

<style>
.progress-bar-container {
	width: 100%;
	background-color: var(--border-color);
	border-radius: 8px;
	height: 30px;
	margin-bottom: 0.5rem;
	overflow: hidden;
}

.progress-bar {
	height: 100%;
	background-color: var(--primary-color);
	border-radius: 8px 0 0 8px;
	transition: width 0.5s ease-in-out;
	width: 0%;
}

.progress-text {
	text-align: center;
	font-weight: 500;
	color: var(--text-muted-color);
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<script>
document.addEventListener('DOMContentLoaded', () => {
    const apiUrl = "${'${pageContext.request.contextPath}'}/api/admin/system-stats";

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

    const formatBytes = (megabytes) => {
        if (megabytes === 0) return '0 MB';
        const gigabytes = megabytes / 1024;
        return gigabytes >= 1 ? `${gigabytes.toFixed(2)} GB` : `${megabytes.toFixed(0)} MB`;
    };

    const updateUI = (stats) => {
        const cpuPercent = stats.cpuLoad.toFixed(1);
        cpuProgress.style.width = cpuPercent + '%';
        cpuText.textContent = cpuPercent + '%';

        const ramPercent = (stats.usedMemory / stats.totalMemory) * 100;
        ramProgress.style.width = ramPercent.toFixed(1) + '%';
        ramText.textContent = formatBytes(stats.usedMemory) + ' / ' + formatBytes(stats.totalMemory);

        const diskPercent = (stats.usedDiskSpace / stats.totalDiskSpace) * 100;
        diskProgress.style.width = diskPercent.toFixed(1) + '%';
        diskText.textContent = formatBytes(stats.usedDiskSpace) + ' / ' + formatBytes(stats.totalDiskSpace);

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
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
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
    setInterval(fetchStats, 5000);
});
</script>