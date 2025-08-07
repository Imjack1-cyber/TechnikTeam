import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const AdminReportsIndex = () => {
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const location = useLocation();

	const navLinks = [
		{ to: '/admin/reports', label: 'Berichte & Analysen', icon: 'fa-chart-pie', perm: 'REPORT_READ' },
		{ to: '/admin/reports/log', label: 'Aktions-Log', icon: 'fa-clipboard-list', perm: 'LOG_READ' },
		{ to: '/admin/reports/system', label: 'System-Status', icon: 'fa-server', perm: 'SYSTEM_READ' },
	];

	const can = (permission) => {
		return isAdmin || user?.permissions.includes(permission);
	};

	return (
		<div>
			<h1><i className="fas fa-chart-line"></i> Berichte &amp; Logs</h1>
			<p>Analysieren Sie die Anwendungsnutzung und überwachen Sie Systemaktivitäten.</p>
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

export default AdminReportsIndex;