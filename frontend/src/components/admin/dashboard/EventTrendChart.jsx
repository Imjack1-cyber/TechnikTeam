import React from 'react';
import { Line } from 'react-chartjs-2';
import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	PointElement,
	LineElement,
	Title,
	Tooltip,
	Legend,
	Filler,
} from 'chart.js';

ChartJS.register(
	CategoryScale,
	LinearScale,
	PointElement,
	LineElement,
	Title,
	Tooltip,
	Legend,
	Filler
);

const EventTrendChart = ({ trendData }) => {
	if (!trendData || trendData.length === 0) {
		return <p>Nicht genügend Daten für den Event-Trend vorhanden.</p>;
	}

	const data = {
		labels: trendData.map(d => d.month),
		datasets: [
			{
				label: 'Anzahl Events pro Monat',
				data: trendData.map(d => d.count),
				fill: true,
				borderColor: 'rgb(0, 123, 255)',
				backgroundColor: 'rgba(0, 123, 255, 0.1)',
				tension: 0.1,
			},
		],
	};

	const options = {
		responsive: true,
		maintainAspectRatio: false,
		scales: {
			y: {
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
		},
	};

	return (
		<div style={{ position: 'relative', height: '300px' }}>
			<Line options={options} data={data} />
		</div>
	);
};

export default EventTrendChart;