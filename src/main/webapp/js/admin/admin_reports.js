document.addEventListener('DOMContentLoaded', () => {
	const eventTrendCanvas = document.getElementById('eventTrendChart');
	const userActivityCanvas = document.getElementById('userActivityChart');

	const getJsonData = (id) => {
		const element = document.getElementById(id);
		if (element) {
			try {
				return JSON.parse(element.textContent);
			} catch (e) {
				console.error(`Failed to parse JSON from #${id}`, e);
				return null;
			}
		}
		return null;
	};

	const eventTrendData = getJsonData('eventTrendData');
	const userActivityData = getJsonData('userActivityData');

	if (eventTrendCanvas && eventTrendData && eventTrendData.length > 0) {
		const labels = eventTrendData.map(d => d.month);
		const data = eventTrendData.map(d => d.count);

		new Chart(eventTrendCanvas.getContext('2d'), {
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
							stepSize: 1
						}
					}
				}
			}
		});
	} else if (eventTrendCanvas) {
		eventTrendCanvas.parentElement.innerHTML = '<p>Nicht genügend Daten für den Event-Trend vorhanden.</p>';
	}

	if (userActivityCanvas && userActivityData && userActivityData.length > 0) {
		const labels = userActivityData.map(d => d.username);
		const data = userActivityData.map(d => d.participation_count);

		new Chart(userActivityCanvas.getContext('2d'), {
			type: 'bar',
			data: {
				labels: labels,
				datasets: [{
					label: 'Anzahl zugewiesener Events',
					data: data,
					backgroundColor: 'rgba(0, 123, 255, 0.6)',
					borderColor: 'rgb(0, 123, 255)',
					borderWidth: 1
				}]
			},
			options: {
				indexAxis: 'y', 
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					x: {
						beginAtZero: true,
						ticks: {
							stepSize: 1
						}
					}
				},
				plugins: {
					legend: {
						display: false
					}
				}
			}
		});
	} else if (userActivityCanvas) {
		userActivityCanvas.parentElement.innerHTML = '<p>Nicht genügend Daten für Benutzeraktivität vorhanden.</p>';
	}
});