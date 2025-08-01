import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import ThemeSwitcher from '../ui/ThemeSwitcher';

const Sidebar = () => {
	const { user, navigationItems, logout } = useAuthStore();

	if (!user || !navigationItems) {
		return null;
	}

	const userNavItems = navigationItems.filter(item => item.requiredPermission === null);
	const adminNavItems = navigationItems.filter(item => item.requiredPermission !== null);

	const handleLogout = () => {
		logout();
	};

	const renderNavItem = (item) => {
		if (item.url.startsWith('/swagger-ui.html')) {
			return (
				<a href={item.url} target="_blank" rel="noopener noreferrer">
					<i className={`fas ${item.icon} fa-fw`}></i> {item.label}
				</a>
			);
		}
		return (
			<NavLink to={item.url} className={({ isActive }) => isActive ? 'active-nav-link' : ''}>
				<i className={`fas ${item.icon} fa-fw`}></i> {item.label}
			</NavLink>
		);
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
						<li key={`${item.label}-${item.url}`}>
							{renderNavItem(item)}
						</li>
					))}

					{adminNavItems.length > 0 && <li className="nav-section-title">Admin-Bereich</li>}
					{adminNavItems.map(item => (
						<li key={`${item.label}-${item.url}`}>
							{renderNavItem(item)}
						</li>
					))}
				</ul>
			</nav>
			<div className="user-actions">
				<div className="user-info">
					Angemeldet als: <strong>{user.username}</strong>
				</div>
				<div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
					<NavLink to="/profil" className="btn btn-secondary btn-small" style={{ flexGrow: 1 }}>Profil</NavLink>
					<button onClick={handleLogout} className="btn btn-danger-outline btn-small" style={{ flexGrow: 1 }}>Logout</button>
					<ThemeSwitcher />
				</div>
			</div>
		</aside>
	);
};

export default Sidebar;