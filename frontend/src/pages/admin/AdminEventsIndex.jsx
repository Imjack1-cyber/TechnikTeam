import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminEventsIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const baseLinks = [
		{ to: '/admin/veranstaltungen', label: 'Events Verwalten', icon: 'fa-calendar-plus', perm: 'EVENT_READ' },
		{ to: '/admin/veranstaltungen/debriefings', label: 'Debriefing-Übersicht', icon: 'fa-clipboard-check', perm: 'EVENT_DEBRIEFING_VIEW' },
		{ to: '/admin/veranstaltungen/roles', label: 'Event-Rollen', icon: 'fa-user-tag', perm: 'EVENT_CREATE' },
		{ to: '/admin/veranstaltungen/venues', label: 'Veranstaltungsorte', icon: 'fa-map-marked-alt', perm: 'EVENT_CREATE' },
		{ to: '/admin/veranstaltungen/checklist-templates', label: 'Checklist-Vorlagen', icon: 'fa-tasks', perm: 'EVENT_MANAGE_TASKS' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	const navLinks = baseLinks.map(link => {
		if (location.pathname === link.to) {
			return { to: '/admin/veranstaltungen', label: 'Zur Event-Übersicht', icon: 'fa-arrow-left', perm: link.perm };
		}
		return link;
	});

	return (
		<div>
			<h1><i className="fas fa-calendar-alt"></i> Event Management</h1>
			<p>Zentrale Anlaufstelle für die Planung und Verwaltung aller Veranstaltungen.</p>
			<div className="dashboard-grid">
				{navLinks.filter(link => can(link.perm)).map(link => (
					<Link to={link.to} className="card" key={link.to} style={{ textDecoration: 'none' }}>
						<div style={{ textAlign: 'center' }}>
							<i className={`fas ${link.icon}`} style={{ fontSize: '3rem', color: 'var(--primary-color)', marginBottom: '1rem' }}></i>
							<h2 className="card-title" style={{ border: 'none', padding: 0 }}>{link.label}</h2>
						</div>
					</Link>
				))}
			</div>
			<hr style={{ margin: '2rem 0' }} />
			<Outlet />
		</div>
	);
};

export default AdminEventsIndex;