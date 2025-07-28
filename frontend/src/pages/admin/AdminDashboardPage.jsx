import React from 'react';
import { Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import { useAuthStore } from '@/store/authStore';
import Widget from '@/components/admin/dashboard/Widget';
import EventTrendChart from '@/components/admin/dashboard/EventTrendChart';

const AdminDashboardPage = () => {
	const { user } = useAuthStore();
	const { data: dashboardData, loading, error } = useApi(() => apiClient.get('/reports/dashboard'));

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
				<Widget icon="fa-calendar-check" title="Nächste Einsätze" linkTo="/veranstaltungen" linkText="Alle Veranstaltungen anzeigen">
					{renderWidgetContent(
						dashboardData?.upcomingEvents,
						(event) => (
							<li key={event.id}>
								<Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link>
								<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
							</li>
						),
						'Keine anstehenden Einsätze gefunden.'
					)}
				</Widget>

				<Widget icon="fa-battery-quarter" title="Niedriger Lagerbestand" linkTo="/lager" linkText="Lagerübersicht anzeigen">
					{renderWidgetContent(
						dashboardData?.lowStockItems,
						(item) => (
							<li key={item.id}>
								<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
								<span className="status-badge status-warn">
									{item.maxQuantity > 0 ? `${Math.round((item.availableQuantity / item.maxQuantity) * 100)}%` : 'N/A'}
								</span>
							</li>
						),
						'Alle Artikel sind ausreichend vorhanden.'
					)}
				</Widget>

				<Widget icon="fa-history" title="Letzte Aktivitäten" linkTo="/admin/log" linkText="Vollständiges Log anzeigen">
					{renderWidgetContent(
						dashboardData?.recentLogs,
						(log) => (
							<li key={log.id}>
								<div>
									<strong>{log.actionType}</strong> von <em>{log.adminUsername}</em>
									<small style={{ display: 'block', color: 'var(--text-muted-color)' }}>{log.details}</small>
								</div>
								<small>{new Date(log.actionTimestamp).toLocaleString('de-DE')}</small>
							</li>
						),
						'Keine aktuellen Aktivitäten protokolliert.'
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