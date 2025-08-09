import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import EventTrendChart from '../../components/admin/dashboard/EventTrendChart';
import UserActivityChart from '../../components/admin/reports/UserActivityChart';

const AdminReportsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/reports/dashboard'), []);
	const { data: reportData, loading, error } = useApi(apiCall);

	if (loading) return <div>Lade Berichte...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!reportData) return <div className="card"><p>Keine Berichtsdaten verfügbar.</p></div>;

	const { eventTrend, userActivity, totalInventoryValue } = reportData;

	const getCsvLink = (reportType) => `/api/v1/reports/${reportType}?export=csv`;

	return (
		<div>
			<h1><i className="fas fa-chart-pie"></i> Berichte & Analysen</h1>
			<p>Hier finden Sie zusammengefasste Daten und Analysen über die Anwendungsnutzung.</p>

			<div className="dashboard-grid">
				<div className="card" style={{ gridColumn: '1 / -1' }}>
					<h2 className="card-title">Event-Trend (Letzte 12 Monate)</h2>
					<EventTrendChart trendData={eventTrend} />
				</div>

				<div className="card">
					<h2 className="card-title">Top 10 Aktivste Benutzer</h2>
					<UserActivityChart activityData={userActivity} />
				</div>

				<div className="card">
					<h2 className="card-title">Sonstige Berichte & Exporte</h2>
					<ul className="details-list">
						<li>
							<span>Teilnahme-Zusammenfassung</span>
							<a href={getCsvLink('event-participation')} className="btn btn-small btn-success"><i className="fas fa-file-csv"></i> Als CSV exportieren</a>
						</li>
						<li>
							<span>Nutzungsfrequenz (Material)</span>
							<a href={getCsvLink('inventory-usage')} className="btn btn-small btn-success"><i className="fas fa-file-csv"></i> Als CSV exportieren</a>
						</li>
						<li>
							<span>Vollständige Benutzeraktivität</span>
							<a href={getCsvLink('user-activity')} className="btn btn-small btn-success"><i className="fas fa-file-csv"></i> Als CSV exportieren</a>
						</li>
						<li>
							<span>Gesamtwert des Lagers</span>
							<span style={{ fontWeight: 'bold' }}>
								{new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(totalInventoryValue)}
							</span>
						</li>
					</ul>
				</div>
			</div>
		</div>
	);
};

export default AdminReportsPage;