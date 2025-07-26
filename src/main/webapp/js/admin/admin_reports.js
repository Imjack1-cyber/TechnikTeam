// src/main/webapp/js/admin/admin_reports.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const eventTrendCanvas = document.getElementById('eventTrendChart');
	const userActivityCanvas = document.getElementById('userActivityChart');
	const totalValueEl = document.getElementById('total-inventory-value');

	const api = {
		getDashboardData: () => fetch(`${contextPath}/api/v1/reports/dashboard`).then(res => res.json())
	};

	const renderEventTrendChart = (trendData) => {
		if (!eventTrendCanvas || !trendData || trendData.length === 0) {
			if (eventTrendCanvas) eventTrendCanvas.parentElement.innerHTML = '<p>Nicht genügend Daten für den Event-Trend vorhanden.</p>';
			return;
		}
		const labels = trendData.map(d => d.month);
		const data = trendData.map(d => d.count);
		new Chart(eventTrendCanvas.getContext('2d'), { /* ... Chart.js config ... */ });
	};

	const renderUserActivityChart = (activityData) => {
		if (!userActivityCanvas || !activityData || activityData.length === 0) {
			if (userActivityCanvas) userActivityCanvas.parentElement.innerHTML = '<p>Nicht genügend Daten für Benutzeraktivität vorhanden.</p>';
			return;
		}
		const labels = activityData.map(d => d.username);
		const data = activityData.map(d => d.participation_count);
		new Chart(userActivityCanvas.getContext('2d'), { /* ... Chart.js config ... */ });
	};

	const loadReportDashboard = async () => {
		try {
			const result = await api.getDashboardData();
			if (result.success) {
				const data = result.data;
				renderEventTrendChart(data.eventTrend);
				renderUserActivityChart(data.userActivity);
				if (totalValueEl) {
					totalValueEl.textContent = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(data.totalInventoryValue);
				}
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error("Failed to load report data:", error);
		}
	};

	loadReportDashboard();
});