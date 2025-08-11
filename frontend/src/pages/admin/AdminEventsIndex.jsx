import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminEventsIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const baseLinks = [
		{ to: '/admin/veranstaltungen/events', label: 'Events Verwalten', icon: 'fa-calendar-plus', perm: 'EVENT_READ' },
		{ to: '/admin/veranstaltungen/debriefings', label: 'Debriefing-Übersicht', icon: 'fa-clipboard-check', perm: 'EVENT_DEBRIEFING_VIEW' },
		{ to: '/admin/veranstaltungen/roles', label: 'Event-Rollen', icon: 'fa-user-tag', perm: 'EVENT_CREATE' },
		{ to: '/admin/veranstaltungen/venues', label: 'Veranstaltungsorte', icon: 'fa-map-marked-alt', perm: 'EVENT_CREATE' },
		{ to: '/admin/veranstaltungen/checklist-templates', label: 'Checklist-Vorlagen', icon: 'fa-tasks', perm: 'EVENT_MANAGE_TASKS' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	const isIndexPage = location.pathname === '/admin/veranstaltungen';

	return (
		<div>
			{!isIndexPage && (
				<Link to="/admin/veranstaltungen" className="btn btn-secondary" style={{ marginBottom: '1rem' }}>
					<i className="fas fa-arrow-left"></i> Zur Event-Übersicht
				</Link>
			)}
			<h1><i className="fas fa-calendar-alt"></i> Event Management</h1>
			<p>Zentrale Anlaufstelle für die Planung und Verwaltung aller Veranstaltungen.</p>

			{isIndexPage ? (
				<div className="dashboard-grid">
					{baseLinks.filter(link => can(link.perm)).map(link => (
						<Link to={link.to} className="card" key={link.to} style={{ textDecoration: 'none' }}>
							<div style={{ textAlign: 'center' }}>
								<i className={`fas ${link.icon}`} style={{ fontSize: '3rem', color: 'var(--primary-color)', marginBottom: '1rem' }}></i>
								<h2 className="card-title" style={{ border: 'none', padding: 0 }}>{link.label}</h2>
							</div>
						</Link>
					))}
				</div>
			) : (
				<>
					<hr style={{ margin: '2rem 0' }} />
					<Outlet />
				</>
			)}
		</div>
	);
};

export default AdminEventsIndex;