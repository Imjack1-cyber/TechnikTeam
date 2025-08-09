import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';

const AdminSystemPage = () => {
	const apiCall = useCallback(() => apiClient.get('/system/stats'), []);
	const { data: stats, loading, error } = useApi(apiCall);

	const formatPercent = (value) => `${value.toFixed(1)}%`;
	const formatGB = (value) => `${value.toFixed(2)} GB`;

	if (loading) return <div>Lade Systeminformationen...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div>
			<h1><i className="fas fa-server"></i> Systeminformationen</h1>
			<p>Live-Statistiken über den Zustand des Servers, auf dem die Anwendung läuft.</p>
			<div className="responsive-dashboard-grid">
				<div className="card">
					<h2 className="card-title">CPU & Speicher</h2>
					<ul className="details-list">
						<li><strong>CPU-Auslastung:</strong> <span>{stats.cpuLoad > 0 ? formatPercent(stats.cpuLoad) : 'Wird geladen...'}</span></li>
						<li><strong>RAM-Nutzung:</strong> <span>{formatGB(stats.usedMemory)} / {formatGB(stats.totalMemory)}</span></li>
					</ul>
				</div>
				<div className="card">
					<h2 className="card-title">Festplattenspeicher</h2>
					<ul className="details-list">
						<li><strong>Speichernutzung (Root):</strong> <span>{formatGB(stats.usedDiskSpace)} / {formatGB(stats.totalDiskSpace)}</span></li>
					</ul>
				</div>
				<div className="card" style={{ gridColumn: '1 / -1' }}>
					<h2 className="card-title">Laufzeit & Energie</h2>
					<ul className="details-list">
						<li><strong>Server-Laufzeit:</strong> <span>{stats.uptime}</span></li>
						<li><strong>Batteriestatus:</strong> <span>{stats.batteryPercentage >= 0 ? `${stats.batteryPercentage}%` : 'Nicht verfügbar'}</span></li>
					</ul>
				</div>
			</div>
		</div>
	);
};

export default AdminSystemPage;