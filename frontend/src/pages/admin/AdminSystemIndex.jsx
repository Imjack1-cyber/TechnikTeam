import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminSystemIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const baseLinks = [
		{ to: '/admin/system/status', label: 'System-Status', icon: 'fa-server', perm: 'SYSTEM_READ' },
		{ to: '/admin/system/auth-log', label: 'Auth Log', icon: 'fa-history', perm: 'LOG_READ' },
		{ to: '/admin/system/wiki', label: 'Technische Wiki', icon: 'fa-book-reader', perm: 'ACCESS_ADMIN_PANEL' },
		{ to: '/TechnikTeam/swagger-ui.html', label: 'API Docs (Swagger)', icon: 'fa-code', perm: 'ACCESS_ADMIN_PANEL', isExternal: true },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	const isIndexPage = location.pathname === '/admin/system';

	const renderLinkCard = (link) => {
		const cardContent = (
			<div style={{ textAlign: 'center' }}>
				<i className={`fas ${link.icon}`} style={{ fontSize: '3rem', color: 'var(--primary-color)', marginBottom: '1rem' }}></i>
				<h2 className="card-title" style={{ border: 'none', padding: 0 }}>{link.label}</h2>
			</div>
		);

		if (link.isExternal) {
			return (
				<a href={link.to} className="card" key={link.to} style={{ textDecoration: 'none' }} target="_blank" rel="noopener noreferrer">
					{cardContent}
				</a>
			);
		}

		return (
			<Link to={link.to} className="card" key={link.to} style={{ textDecoration: 'none' }}>
				{cardContent}
			</Link>
		);
	}

	return (
		<div>
			{!isIndexPage && (
				<Link to="/admin/system" className="btn btn-secondary" style={{ marginBottom: '1rem' }}>
					<i className="fas fa-arrow-left"></i> Zur System-Übersicht
				</Link>
			)}
			<h1><i className="fas fa-cogs"></i> System &amp; Entwicklung</h1>
			<p>Technische Verwaltung, Dokumentation und Systemüberwachung.</p>

			{isIndexPage ? (
				<div className="dashboard-grid">
					{baseLinks.filter(link => can(link.perm)).map(link => renderLinkCard(link))}
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

export default AdminSystemIndex;