import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import AdminStoragePage from './AdminStoragePage';

const AdminStorageIndex = () => {
	const { user } = useAuthStore();
	const location = useLocation();
	const can = (permission) => user?.permissions.includes(permission) || user?.isAdmin;

	const navLinks = [
		{ to: '/admin/lager/main', label: 'Lager Verwalten', icon: 'fa-warehouse', perm: 'STORAGE_READ' },
		{ to: '/admin/lager/kits', label: 'Kit-Verwaltung', icon: 'fa-box-open', perm: 'KIT_READ' },
		{ to: '/admin/lager/defekte', label: 'Defekte Artikel', icon: 'fa-wrench', perm: 'STORAGE_READ' },
		{ to: '/admin/lager/damage-reports', label: 'Schadensmeldungen', icon: 'fa-tools', perm: 'DAMAGE_REPORT_MANAGE' },
	];

	const isIndexPage = location.pathname === '/admin/lager';

	return (
		<div>
			<h1><i className="fas fa-boxes"></i> Lager & Material</h1>
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
			{!isIndexPage && <hr style={{ margin: '2rem 0' }} />}
			<Outlet />
		</div>
	);
};

export default AdminStorageIndex;