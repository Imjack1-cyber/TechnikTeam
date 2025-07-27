import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const Sidebar = () => {
	const { user, navigationItems, logout } = useAuthStore();

	if (!user || !navigationItems) {
		return null; // Or a loading spinner
	}

	const userNavItems = navigationItems.filter(item => item.requiredPermission === null);
	const adminNavItems = navigationItems.filter(item => item.requiredPermission !== null);

	const handleLogout = () => {
		// Here you might want a confirmation modal in a real app
		logout();
	};

	return (
		<aside className="sidebar">
			<header className="sidebar-header">
				<a href="/home" className="logo">
					<i className="fas fa-bolt"></i> TechnikTeam
				</a>
			</header>
			<nav className="sidebar-nav">
				<ul>
					{userNavItems.length > 0 && <li className="nav-section-title">Benutzerbereich</li>}
					{userNavItems.map(item => (
						<li key={item.label}>
							<NavLink to={item.url} className={({ isActive }) => isActive ? 'active-nav-link' : ''}>
								<i className={`fas ${item.icon} fa-fw`}></i> {item.label}
							</NavLink>
						</li>
					))}

					{adminNavItems.length > 0 && <li className="nav-section-title">Admin-Bereich</li>}
					{adminNavItems.map(item => (
						<li key={item.label}>
							<NavLink to={item.url} className={({ isActive }) => isActive ? 'active-nav-link' : ''}>
								<i className={`fas ${item.icon} fa-fw`}></i> {item.label}
							</NavLink>
						</li>
					))}
				</ul>
			</nav>
			<div className="user-actions">
				<div className="user-info">
					Angemeldet als: <strong>{user.username}</strong>
				</div>
				<div className="sidebar-controls">
					<NavLink to="/profil" className="btn btn-secondary btn-small" style={{ flexGrow: 1 }}>Profil</NavLink>
					<button onClick={handleLogout} className="btn btn-danger-outline btn-small" style={{ flexGrow: 1 }}>Logout</button>
				</div>
			</div>
		</aside>
	);
};

export default Sidebar;