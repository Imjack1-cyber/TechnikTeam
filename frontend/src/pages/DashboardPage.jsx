import React, { useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

const DashboardPage = () => {
	const { user, layout } = useAuthStore(state => ({
		user: state.user,
		layout: state.layout,
	}));
	const apiCall = useCallback(() => apiClient.get('/public/dashboard'), []);
	const { data: dashboardData, loading, error } = useApi(apiCall);

	const widgets = layout.dashboardWidgets || {
		recommendedEvents: true,
		assignedEvents: true,
		openTasks: true,
		upcomingEvents: true,
		recentConversations: true,
		upcomingMeetings: true,
		lowStockItems: false,
	};

	if (loading) {
		return (
			<div>
				<h1>Willkommen zurück, {user?.username}!</h1>
				<div className="dashboard-grid">
					<div className="card"><h2 className="card-title">Lade Dashboard...</h2><p>Lade Daten...</p></div>
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

				{widgets.recommendedEvents && dashboardData?.recommendedEvents?.length > 0 && (
					<div className="card" id="recommended-events-widget" style={{ gridColumn: '1 / -1', background: 'var(--primary-color-light)' }}>
						<h2 className="card-title"><i className="fas fa-star" style={{ color: 'var(--warning-color)' }}></i> Für Dich empfohlen</h2>
						<p>Hier sind einige anstehende Veranstaltungen, für die du qualifiziert bist und bei denen noch Unterstützung gebraucht wird:</p>
						<ul className="details-list">
							{dashboardData.recommendedEvents.map(event => (
								<li key={event.id}>
									<Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link>
									<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
								</li>
							))}
						</ul>
						<Link to="/veranstaltungen" className="btn btn-small" style={{ marginTop: '1rem' }}>Alle Veranstaltungen anzeigen</Link>
					</div>
				)}

				{widgets.assignedEvents && (
					<div className="card" id="assigned-events-widget">
						<h2 className="card-title">Meine nächsten Einsätze</h2>
						<div id="assigned-events-content">
							{dashboardData?.assignedEvents?.length > 0 ? (
								<ul className="details-list">
									{dashboardData.assignedEvents.map(event => (
										<li key={event.id}>
											<Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link>
											<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
										</li>
									))}
								</ul>
							) : (
								<p>Du bist derzeit für keine kommenden Events fest eingeteilt.</p>
							)}
						</div>
						<Link to="/veranstaltungen" className="btn btn-small" style={{ marginTop: '1rem' }}>Alle Veranstaltungen anzeigen</Link>
					</div>
				)}

				{widgets.openTasks && (
					<div className="card" id="open-tasks-widget">
						<h2 className="card-title">Meine offenen Aufgaben</h2>
						<div id="open-tasks-content">
							{dashboardData?.openTasks?.length > 0 ? (
								<ul className="details-list">
									{dashboardData.openTasks.map(task => (
										<li key={task.id}>
											<Link to={`/veranstaltungen/details/${task.eventId}`}>
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
				)}

				{widgets.upcomingMeetings && (
					<div className="card" id="upcoming-meetings-widget">
						<h2 className="card-title">Meine nächsten Lehrgänge</h2>
						{dashboardData?.upcomingMeetings?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.upcomingMeetings.map(meeting => (
									<li key={meeting.id}>
										<Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.parentCourseName}: {meeting.name}</Link>
										<small>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</small>
									</li>
								))}
							</ul>
						) : (
							<p>Du bist für keine anstehenden Lehrgänge angemeldet.</p>
						)}
						<Link to="/lehrgaenge" className="btn btn-small" style={{ marginTop: '1rem' }}>Alle Lehrgänge anzeigen</Link>
					</div>
				)}

				{widgets.recentConversations && (
					<div className="card" id="recent-conversations-widget">
						<h2 className="card-title">Letzte Gespräche</h2>
						{dashboardData?.recentConversations?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.recentConversations.map(conv => (
									<li key={conv.id}>
										<Link to={`/chat/${conv.id}`}>
											{conv.groupChat ? conv.name : conv.otherParticipantUsername}
											<small style={{ display: 'block', color: 'var(--text-muted-color)', fontStyle: 'italic' }}>
												{conv.lastMessage}
											</small>
										</Link>
									</li>
								))}
							</ul>
						) : (
							<p>Keine kürzlichen Gespräche.</p>
						)}
						<Link to="/chat" className="btn btn-small" style={{ marginTop: '1rem' }}>Zum Chat</Link>
					</div>
				)}

				{widgets.upcomingEvents && (
					<div className="card" id="upcoming-events-widget">
						<h2 className="card-title">Weitere anstehende Veranstaltungen</h2>
						<div id="upcoming-events-content">
							{dashboardData?.upcomingEvents?.length > 0 ? (
								<ul className="details-list">
									{dashboardData.upcomingEvents.map(event => (
										<li key={event.id}>
											<Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link>
											<small>{new Date(event.eventDateTime).toLocaleString('de-DE')}</small>
										</li>
									))}
								</ul>
							) : (
								<p>Keine weiteren anstehenden Veranstaltungen.</p>
							)}
						</div>
					</div>
				)}

				{widgets.lowStockItems && (
					<div className="card" id="low-stock-widget">
						<h2 className="card-title">Niedriger Lagerbestand</h2>
						{dashboardData?.lowStockItems?.length > 0 ? (
							<ul className="details-list">
								{dashboardData.lowStockItems.map(item => (
									<li key={item.id}>
										<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
										<span className="status-badge status-warn">{item.availableQuantity} / {item.maxQuantity}</span>
									</li>
								))}
							</ul>
						) : (
							<p>Alle Artikel sind ausreichend vorhanden.</p>
						)}
						<Link to="/lager" className="btn btn-small" style={{ marginTop: '1rem' }}>Zum Lager</Link>
					</div>
				)}

			</div>
		</div>
	);
};

export default DashboardPage;