import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	BarElement,
	Title,
	Tooltip,
	Legend,
} from 'chart.js';

ChartJS.register(
	CategoryScale,
	LinearScale,
	BarElement,
	Title,
	Tooltip,
	Legend
);

const UserActivityChart = ({ activityData }) => {
	if (!activityData || activityData.length === 0) {
		return <p>Nicht genügend Daten für Benutzeraktivität vorhanden.</p>;
	}

	const data = {
		labels: activityData.map(d => d.username),
		datasets: [
			{
				label: 'Anzahl Event-Teilnahmen',
				data: activityData.map(d => d.participation_count),
				backgroundColor: 'rgba(0, 123, 255, 0.6)',
				borderColor: 'rgba(0, 123, 255, 1)',
				borderWidth: 1,
			},
		],
	};

	const options = {
		indexAxis: 'y', // Makes it a horizontal bar chart
		responsive: true,
		maintainAspectRatio: false,
		scales: {
			x: {
				beginAtZero: true,
				ticks: {
					stepSize: 1,
					precision: 0,
				},
			},
		},
		plugins: {
			legend: {
				display: false,
			},
			title: {
				display: true,
				text: 'Event-Teilnahmen pro Benutzer',
			},
		},
	};

	return (
		<div style={{ position: 'relative', height: '400px' }}>
			<Bar options={options} data={data} />
		</div>
	);
};

export default UserActivityChart;