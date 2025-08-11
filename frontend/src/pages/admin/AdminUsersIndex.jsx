import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminUsersIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const baseLinks = [
		{ to: '/admin/mitglieder/users', label: 'Benutzer Verwalten', icon: 'fa-users-cog', perm: 'USER_READ' },
		{ to: '/admin/mitglieder/requests', label: 'Profilanträge', icon: 'fa-inbox', perm: 'USER_UPDATE' },
		{ to: '/admin/mitglieder/training-requests', label: 'Lehrgangsanfragen', icon: 'fa-question-circle', perm: 'COURSE_CREATE' },
		{ to: '/admin/mitglieder/achievements', label: 'Abzeichen', icon: 'fa-award', perm: 'ACHIEVEMENT_VIEW' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	const isIndexPage = location.pathname === '/admin/mitglieder' || location.pathname === '/admin/mitglieder/';


	return (
		<div>
			{!isIndexPage && (
				<Link to="/admin/mitglieder" className="btn btn-secondary" style={{ marginBottom: '1rem' }}>
					<i className="fas fa-arrow-left"></i> Zur Benutzer-Übersicht
				</Link>
			)}

			<h1><i className="fas fa-user-friends"></i> Benutzer &amp; Anträge</h1>
			<p>Verwalten Sie hier die Mitglieder des Teams und ihre Anträge.</p>

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

export default AdminUsersIndex;