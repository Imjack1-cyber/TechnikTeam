<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Systemstatus" />
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

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script type="text/javascript" src="/js/admin/admin_system.js"></script>