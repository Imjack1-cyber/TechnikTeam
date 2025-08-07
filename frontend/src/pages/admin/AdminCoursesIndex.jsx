import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import AdminCoursesPage from './AdminCoursesPage';

const AdminCoursesIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const navLinks = [
		{ to: '/admin/lehrgaenge', label: 'Lehrgangs-Vorlagen', icon: 'fa-book', perm: 'COURSE_READ' },
		{ to: '/admin/lehrgaenge/matrix', label: 'Qualifikations-Matrix', icon: 'fa-th-list', perm: 'QUALIFICATION_READ' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	return (
		<div>
			<h1><i className="fas fa-graduation-cap"></i> Lehrgänge &amp; Skills</h1>
			<p>Verwalten Sie hier die Ausbildungs-Vorlagen und den Qualifikationsstatus der Mitglieder.</p>
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

export default AdminCoursesIndex;