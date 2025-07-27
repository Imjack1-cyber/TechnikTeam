import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

const DashboardPage = () => {
	const { user } = useAuthStore();
	const [dashboardData, setDashboardData] = useState(null);
	const [loading, setLoading] = useState(true);
	const [error, setError] = useState('');

	useEffect(() => {
		const fetchDashboardData = async () => {
			try {
				setLoading(true);
				const result = await apiClient.get('/public/dashboard');
				if (result.success) {
					setDashboardData(result.data);
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				setError(err.message || 'Fehler beim Laden der Dashboard-Daten.');
			} finally {
				setLoading(false);
			}
		};

		fetchDashboardData();
	}, []);

	if (loading) {
		return (
			<div>
				<h1>Willkommen zurück, {user?.username}!</h1>
				<div className="dashboard-grid">
					<div className="card"><h2 className="card-title">Meine nächsten Einsätze</h2><p>Lade Daten...</p></div>
					<div className="card"><h2 className="card-title">Meine offenen Aufgaben</h2><p>Lade Daten...</p></div>
					<div className="card"><h2 className="card-title">Weitere anstehende Veranstaltungen</h2><p>Lade Daten...</p></div>
				</div>
			</div>
		);
	}

	if (error) {
		return <div className="error-message">{error}</div>;
	}

	return (
		<div>
			<h1>Willkommen zurück, {user?.username}!</h1>
			<div className="dashboard-grid">
				<div className="card" id="assigned-events-widget">
					<h2 className="card-title">Meine nächsten Einsätze</h2>
					<div id="assigned-events-content">
						{dashboardData?.assignedEvents?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.assignedEvents.map(event => (
									<li key={event.id}>
										<Link to={`/events/details/${event.id}`}>{event.name}</Link>
										<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
									</li>
								))}
							</ul>
						) : (
							<p>Du bist derzeit für keine kommenden Events fest eingeteilt.</p>
						)}
					</div>
					<Link to="/events" className="btn btn-small" style={{ marginTop: '1rem' }}>Alle Veranstaltungen anzeigen</Link>
				</div>

				<div className="card" id="open-tasks-widget">
					<h2 className="card-title">Meine offenen Aufgaben</h2>
					<div id="open-tasks-content">
						{dashboardData?.openTasks?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.openTasks.map(task => (
									<li key={task.id}>
										<Link to={`/events/details/${task.eventId}`}>
											{task.description}
											<small style={{ display: 'block', color: 'var(--text-muted-color)' }}>
												Für Event: {task.eventName}
											</small>
										</Link>
									</li>
								))}
							</ul>
						) : (
							<p>Super! Du hast aktuell keine offenen Aufgaben.</p>
						)}
					</div>
				</div>

				<div className="card" id="upcoming-events-widget">
					<h2 className="card-title">Weitere anstehende Veranstaltungen</h2>
					<div id="upcoming-events-content">
						{dashboardData?.upcomingEvents?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.upcomingEvents.map(event => (
									<li key={event.id}>
										<Link to={`/events/details/${event.id}`}>{event.name}</Link>
										<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
									</li>
								))}
							</ul>
						) : (
							<p>Keine weiteren anstehenden Veranstaltungen.</p>
						)}
					</div>
				</div>
			</div>
		</div>
	);
};

export default DashboardPage;