import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminStorageIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const baseLinks = [
		{ to: '/admin/lager', label: 'Lager Verwalten', icon: 'fa-warehouse', perm: 'STORAGE_READ' },
		{ to: '/admin/lager/kits', label: 'Kit-Verwaltung', icon: 'fa-box-open', perm: 'KIT_READ' },
		{ to: '/admin/lager/defekte', label: 'Defekte Artikel', icon: 'fa-wrench', perm: 'STORAGE_READ' },
		{ to: '/admin/lager/damage-reports', label: 'Schadensmeldungen', icon: 'fa-tools', perm: 'DAMAGE_REPORT_MANAGE' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	const navLinks = baseLinks.map(link => {
		if (location.pathname === link.to) {
			return { to: '/admin/lager', label: 'Zur Lager-Übersicht', icon: 'fa-arrow-left', perm: link.perm };
		}
		return link;
	});

	return (
		<div>
			<h1><i className="fas fa-boxes"></i> Lager &amp; Material</h1>
			<p>Verwalten Sie hier das Inventar, Material-Sets und gemeldete Schäden.</p>
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

export default AdminStorageIndex;