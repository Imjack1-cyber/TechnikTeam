import React, { useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import Widget from '../../components/admin/dashboard/Widget';
import EventTrendChart from '../../components/admin/dashboard/EventTrendChart';

const AdminDashboardPage = () => {
	const { user } = useAuthStore();
	// This single endpoint now provides all necessary data for the dashboard.
	const apiCall = useCallback(() => apiClient.get('/admin/dashboard'), []);
	const { data: dashboardData, loading, error } = useApi(apiCall);

	const renderWidgetContent = (widgetData, renderItem, emptyMessage) => {
		if (!widgetData || widgetData.length === 0) {
			return <p>{emptyMessage}</p>;
		}
		return <ul className="details-list">{widgetData.map(renderItem)}</ul>;
	};

	if (loading) {
		return <h1>Lade Admin Dashboard...</h1>;
	}

	if (error) {
		return <div className="error-message">{error}</div>;
	}

	return (
		<div>
			<h1>Willkommen im Admin-Bereich, {user?.username}!</h1>
			<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option aus der Navigation oder nutzen Sie den Schnellzugriff.</p>

			<div className="dashboard-grid">
				<Widget icon="fa-calendar-alt" title="Anstehende Events" linkTo="/admin/veranstaltungen" linkText="Alle Events anzeigen">
					{renderWidgetContent(
						dashboardData?.upcomingEvents,
						(event) => (
							<li key={event.id}>
								<Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link>
								<small>{new Date(event.eventDateTime).toLocaleDateString('de-DE')}</small>
							</li>
						),
						'Keine anstehenden Events.'
					)}
				</Widget>

				<Widget icon="fa-box-open" title="Niedriger Lagerbestand" linkTo="/admin/lager" linkText="Lager verwalten">
					{renderWidgetContent(
						dashboardData?.lowStockItems,
						(item) => (
							<li key={item.id}>
								<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
								<span className="status-badge status-warn">{item.availableQuantity} / {item.maxQuantity}</span>
							</li>
						),
						'Alle Artikel sind ausreichend vorhanden.'
					)}
				</Widget>

				<Widget icon="fa-history" title="Letzte Aktionen" linkTo="/admin/log" linkText="Alle Logs anzeigen">
					{renderWidgetContent(
						dashboardData?.recentLogs,
						(log) => (
							<li key={log.id}>
								<span><strong>{log.adminUsername}</strong>: {log.actionType}</span>
								<small>{new Date(log.actionTimestamp).toLocaleString('de-DE')}</small>
							</li>
						),
						'Keine Aktionen protokolliert.'
					)}
				</Widget>
			</div>

			<div className="card" style={{ marginTop: '2rem' }}>
				<h2 className="card-title">Event-Trend (Letzte 12 Monate)</h2>
				<EventTrendChart trendData={dashboardData?.eventTrendData} />
			</div>
		</div>
	);
};

export default AdminDashboardPage;