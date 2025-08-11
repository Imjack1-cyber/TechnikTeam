import React, { useCallback, useState, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const MaintenanceModeToggle = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/system/maintenance'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const { addToast } = useToast();

	const isEnabled = data?.isEnabled || false;

	const handleToggle = async () => {
		const newState = !isEnabled;
		const action = newState ? 'aktivieren' : 'deaktivieren';
		if (window.confirm(`Sind Sie sicher, dass Sie den Wartungsmodus ${action} möchten? Alle nicht-administrativen Benutzer werden ausgesperrt.`)) {
			setIsSubmitting(true);
			try {
				const result = await apiClient.post('/admin/system/maintenance', { isEnabled: newState });
				if (result.success) {
					addToast(result.message, 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(err.message, 'error');
			} finally {
				setIsSubmitting(false);
			}
		}
	};

	if (loading) return <p>Lade Wartungsmodus-Status...</p>;
	if (error) return <p className="error-message">{error}</p>;

	return (
		<div className="card">
			<h2 className="card-title">Wartungsmodus</h2>
			<p>Wenn aktiviert, können sich nur Administratoren anmelden. Alle anderen Benutzer sehen eine Wartungsseite.</p>
			<div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '1rem' }}>
				<button onClick={handleToggle} className={`btn ${isEnabled ? 'btn-success' : 'btn-danger'}`} disabled={isSubmitting}>
					{isSubmitting ? 'Wird geändert...' : (isEnabled ? 'Wartungsmodus Deaktivieren' : 'Wartungsmodus Aktivieren')}
				</button>
				<span className={`status-badge ${isEnabled ? 'status-danger' : 'status-ok'}`}>
					{isEnabled ? 'AKTIV' : 'INAKTIV'}
				</span>
			</div>
		</div>
	);
};

const AdminSystemPage = () => {
	const apiCall = useCallback(() => apiClient.get('/system/stats'), []);
	const { data: stats, loading, error } = useApi(apiCall);

	const formatPercent = (value) => `${value.toFixed(1)}%`;
	const formatGB = (value) => `${value.toFixed(2)} GB`;

	return (
		<div>
			<h1><i className="fas fa-server"></i> Systeminformationen</h1>
			<p>Live-Statistiken über den Zustand des Servers, auf dem die Anwendung läuft.</p>

			<MaintenanceModeToggle />

			{loading && <p>Lade Systemstatistiken...</p>}
			{error && <p className="error-message">{error}</p>}
			{stats && (
				<div className="responsive-dashboard-grid" style={{ marginTop: '1.5rem' }}>
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
			)}
		</div>
	);
};

export default AdminSystemPage;