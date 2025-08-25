import React, { useCallback, useState, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const MaintenanceModeManager = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/system/maintenance'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [mode, setMode] = useState('OFF');
	const [message, setMessage] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		if (data) {
			setMode(data.mode || 'OFF');
			setMessage(data.message || '');
		}
	}, [data]);

	const handleSubmit = async (e) => {
		e.preventDefault();
		const newStatus = { mode, message };
		const actionText = {
			OFF: 'deaktivieren',
			SOFT: 'aktivieren (Warnung)',
			HARD: 'aktivieren (Sperre)'
		}[mode];

		if (window.confirm(`Sind Sie sicher, dass Sie den Wartungsmodus ${actionText} möchten?`)) {
			setIsSubmitting(true);
			try {
				const result = await apiClient.post('/admin/system/maintenance', newStatus);
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
			<p>Steuern Sie den globalen Zugriffsstatus der Anwendung.</p>
			<form onSubmit={handleSubmit}>
				<div className="form-group">
					<label>Modus</label>
					<div style={{ display: 'flex', gap: '1.5rem' }}>
						<label><input type="radio" name="mode" value="OFF" checked={mode === 'OFF'} onChange={e => setMode(e.target.value)} /> Aus</label>
						<label><input type="radio" name="mode" value="SOFT" checked={mode === 'SOFT'} onChange={e => setMode(e.target.value)} /> Warnung (Banner)</label>
						<label><input type="radio" name="mode" value="HARD" checked={mode === 'HARD'} onChange={e => setMode(e.target.value)} /> Sperre (Nur Admins)</label>
					</div>
				</div>
				<div className="form-group">
					<label htmlFor="maintenance-message">Angezeigte Nachricht</label>
					<textarea
						id="maintenance-message"
						value={message}
						onChange={e => setMessage(e.target.value)}
						rows="3"
						placeholder="z.B. Führen gerade Datenbank-Updates durch. Voraussichtlich wieder verfügbar um 15:00 Uhr."
					/>
				</div>
				<button type="submit" className="btn btn-success" disabled={isSubmitting}>
					{isSubmitting ? 'Wird geändert...' : 'Status aktualisieren'}
				</button>
			</form>
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

			<MaintenanceModeManager />

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