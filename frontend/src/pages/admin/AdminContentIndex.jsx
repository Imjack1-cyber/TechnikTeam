import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminContentIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const navLinks = [
		{ to: '/admin/content/announcements', label: 'Anschlagbrett', icon: 'fa-thumbtack', perm: 'USER_UPDATE' }, // Simplified perm
		{ to: '/admin/content/dateien', label: 'Dateien', icon: 'fa-file-upload', perm: 'FILE_MANAGE' },
		{ to: '/admin/content/feedback', label: 'Feedback', icon: 'fa-inbox', perm: 'ADMIN_DASHBOARD_ACCESS' }, // Generic admin perm
		{ to: '/admin/content/changelogs', label: 'Changelogs', icon: 'fa-history', perm: 'ACCESS_ADMIN_PANEL' },
		{ to: '/admin/content/documentation', label: 'Seiten-Doku', icon: 'fa-book-open', perm: 'DOCUMENTATION_MANAGE' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};


	return (
		<div>
			<h1><i className="fas fa-desktop"></i> Inhalte &amp; System</h1>
			<p>Verwalten Sie hier die redaktionellen Inhalte und Kommunikationskanäle der Anwendung.</p>
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

export default AdminContentIndex;