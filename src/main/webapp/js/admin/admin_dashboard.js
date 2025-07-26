document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	let eventChartInstance = null;

	const upcomingEventsContainer = document.getElementById('widget-upcoming-events');
	const lowStockContainer = document.getElementById('widget-low-stock');
	const recentLogsContainer = document.getElementById('widget-recent-logs');
	const eventTrendCanvas = document.getElementById('eventTrendChart');

	function formatDateTime(isoString) {
		if (!isoString) return 'N/A';
		const date = new Date(isoString);
		return date.toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
	}

	function createAlertBanner(message, type = 'info') {
		const banner = document.createElement('div');
		banner.className = `${type}-message`; 
		const iconClass = type === 'danger' ? 'fa-exclamation-triangle' : 'fa-info-circle';
		banner.innerHTML = `<i class="fas ${iconClass}"></i> ${message}`;
		return banner;
	}

	function renderUpcomingEvents(events) {
		const header = '<h2><i class="fas fa-calendar-check"></i> Nächste Einsätze</h2>';
		if (!events || events.length === 0) {
			upcomingEventsContainer.innerHTML = header + '<p>Keine anstehenden Einsätze gefunden.</p>';
			return;
		}

		const list = events.map(event => `
            <li>
                <a href="${contextPath}/veranstaltungen/details?id=${event.id}">${event.name}</a>
                <small>${formatDateTime(event.eventDateTime)} Uhr</small>
            </li>
        `).join('');

		upcomingEventsContainer.innerHTML = header + `<ul class="details-list">${list}</ul>`;
	}

	function renderLowStockItems(items) {
		const header = '<h2><i class="fas fa-battery-quarter"></i> Niedriger Lagerbestand</h2>';

		let content = '';
		if (!items || items.length === 0) {
			content = '<p>Alle Artikel sind ausreichend vorhanden.</p>';
		} else {
			const list = items.map(item => {
				const percentage = item.maxQuantity > 0 ? ((item.quantity - item.defectiveQuantity) / item.maxQuantity * 100).toFixed(0) : 0;
				return `
                    <li>
                        <a href="${contextPath}/lager/details?id=${item.id}">${item.name}</a>
                        <span class="status-badge status-warn">${percentage}%</span>
                    </li>
                `;
			}).join('');
			content = `<ul class="details-list">${list}</ul>`;

			const alertMessage = `Es gibt ${items.length} Artikel mit niedrigem Lagerbestand. <a href="${contextPath}/admin/lager">Jetzt prüfen</a>.`;
			const banner = createAlertBanner(alertMessage, 'danger');
			lowStockContainer.prepend(banner);
		}

		lowStockContainer.innerHTML = header + content;
	}

	function renderRecentLogs(logs) {
		const header = '<h2><i class="fas fa-history"></i> Letzte Aktivitäten</h2>';
		if (!logs || logs.length === 0) {
			recentLogsContainer.innerHTML = header + '<p>Keine aktuellen Aktivitäten protokolliert.</p>';
			return;
		}

		const list = logs.map(log => `
            <li>
                <div>
                    <strong>${log.actionType}</strong> von <em>${log.adminUsername}</em>
                    <small style="display: block; color: var(--text-muted-color);">${log.details}</small>
                </div>
                <small>${formatDateTime(log.actionTimestamp)}</small>
            </li>
        `).join('');

		recentLogsContainer.innerHTML = header + `<ul class="details-list">${list}</ul>`;
	}

	function renderEventTrendChart(trendData) {
		if (eventChartInstance) {
			eventChartInstance.destroy();
		}

		if (!trendData || trendData.length === 0) {
			return;
		}

		const labels = trendData.map(d => d.month);
		const data = trendData.map(d => d.count);

		const chartConfig = {
			type: 'line',
			data: {
				labels: labels,
				datasets: [{
					label: 'Anzahl Events pro Monat',
					data: data,
					fill: true,
					borderColor: 'rgb(0, 123, 255)',
					backgroundColor: 'rgba(0, 123, 255, 0.1)',
					tension: 0.1
				}]
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					y: {
						beginAtZero: true,
						ticks: {
							stepSize: 1,
							precision: 0
						}
					}
				},
				plugins: {
					legend: {
						display: false
					}
				}
			}
		};

		eventChartInstance = new Chart(eventTrendCanvas.getContext('2d'), chartConfig);
	}

	async function fetchData() {
		try {
			const response = await fetch(`${contextPath}/api/admin/dashboard-data`);
			if (!response.ok) {
				throw new Error(`HTTP error! status: ${response.status}`);
			}
			const data = await response.json();

			renderUpcomingEvents(data.upcomingEvents);
			renderLowStockItems(data.lowStockItems);
			renderRecentLogs(data.recentLogs);
			renderEventTrendChart(data.eventTrendData);

		} catch (error) {
			console.error("Failed to fetch dashboard data:", error);
			upcomingEventsContainer.innerHTML = '<h2><i class="fas fa-calendar-check"></i> Nächste Einsätze</h2><p class="error-message">Fehler beim Laden der Daten.</p>';
			lowStockContainer.innerHTML = '<h2><i class="fas fa-battery-quarter"></i> Niedriger Lagerbestand</h2><p class="error-message">Fehler beim Laden der Daten.</p>';
			recentLogsContainer.innerHTML = '<h2><i class="fas fa-history"></i> Letzte Aktivitäten</h2><p class="error-message">Fehler beim Laden der Daten.</p>';
		}
	}

	fetchData();
	setInterval(fetchData, 60000); 
});