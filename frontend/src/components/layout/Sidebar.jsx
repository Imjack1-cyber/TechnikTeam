import React, { useState, useMemo, useRef, useEffect, useCallback } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import ThemeSwitcher from '../ui/ThemeSwitcher';

const Sidebar = () => {
	const { user, navigationItems, logout, layout } = useAuthStore();
	const [searchTerm, setSearchTerm] = useState('');
	const navigate = useNavigate();
	const navRef = useRef(null);
	const [scrollState, setScrollState] = useState({
		canScrollLeft: false,
		canScrollRight: false,
		isOverflowing: false,
	});

	const isHorizontal = layout.sidebarPosition === 'top' || layout.sidebarPosition === 'bottom';

	const orderedNavItems = useMemo(() => {
		if (!navigationItems) return [];
		const userOrder = layout.navOrder || [];
		const itemMap = new Map(navigationItems.map(item => [item.label, item]));
		const ordered = userOrder.map(label => itemMap.get(label)).filter(Boolean);
		const remaining = navigationItems.filter(item => !userOrder.includes(item.label));
		return [...ordered, ...remaining];
	}, [navigationItems, layout.navOrder]);

	const checkScroll = useCallback(() => {
		if (navRef.current) {
			const { scrollWidth, clientWidth, scrollLeft } = navRef.current;
			const isOverflowing = scrollWidth > clientWidth;
			setScrollState({
				isOverflowing,
				canScrollLeft: isOverflowing && scrollLeft > 0,
				canScrollRight: isOverflowing && scrollLeft < scrollWidth - clientWidth - 1,
			});
		}
	}, []);

	useEffect(() => {
		const navElement = navRef.current;
		if (!navElement || !isHorizontal) return;

		checkScroll(); // Initial check

		const resizeObserver = new ResizeObserver(checkScroll);
		resizeObserver.observe(navElement);

		navElement.addEventListener('scroll', checkScroll);

		return () => {
			resizeObserver.disconnect();
			navElement.removeEventListener('scroll', checkScroll);
		};
	}, [orderedNavItems, isHorizontal, checkScroll]);

	if (!user || !navigationItems) {
		return null;
	}

	const handleSearchSubmit = (e) => {
		e.preventDefault();
		if (searchTerm.trim()) {
			navigate(`/suche?q=${encodeURIComponent(searchTerm.trim())}`);
			setSearchTerm('');
		}
	};

	const handleScroll = (direction) => {
		if (navRef.current) {
			const scrollAmount = navRef.current.clientWidth * 0.7;
			navRef.current.scrollBy({
				left: direction === 'left' ? -scrollAmount : scrollAmount,
				behavior: 'smooth',
			});
		}
	};

	const userNavItems = orderedNavItems.filter(item => item.requiredPermission === null);
	const adminNavItems = orderedNavItems.filter(item => item.requiredPermission !== null);

	const handleLogout = () => {
		logout();
	};

	const renderNavItem = (item) => {
		const hasBadge = item.url === '/notifications' && user.unseenNotificationsCount > 0;

		// Use a simple startsWith check as the full path might vary
		if (item.url.startsWith('/swagger-ui')) {
			// Construct the correct absolute path including the backend's context path
			const swaggerUrl = `/TechnikTeam/swagger-ui.html`;
			return (
				<a href={swaggerUrl} target="_blank" rel="noopener noreferrer">
					<i className={`fas ${item.icon} fa-fw`}></i> {item.label}
				</a>
			);
		}
		return (
			<NavLink to={item.url} className={({ isActive }) => (isActive ? 'active-nav-link' : '')}>
				<i className={`fas ${item.icon} fa-fw`}></i>
				{item.label}
				{hasBadge && <span className="cart-badge" style={{ position: 'static', marginLeft: 'auto', width: '20px', height: '20px', fontSize: '0.7rem' }}>{user.unseenNotificationsCount}</span>}
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
			<div style={{ padding: '0 1rem', marginBottom: '0.5rem' }}>
				<form onSubmit={handleSearchSubmit}>
					<div className="form-group" style={{ position: 'relative', marginBottom: 0 }}>
						<input
							type="search"
							placeholder="Suchen..."
							value={searchTerm}
							onChange={(e) => setSearchTerm(e.target.value)}
							style={{ paddingLeft: '2.5rem' }}
						/>
						<i className="fas fa-search" style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted-color)' }}></i>
					</div>
				</form>
			</div>
			<div className={`sidebar-nav-scroller ${scrollState.isOverflowing ? 'is-overflowing' : ''}`}>
				{isHorizontal && (
					<button className="scroll-btn left" onClick={() => handleScroll('left')} disabled={!scrollState.canScrollLeft}>
						<i className="fas fa-chevron-left"></i>
					</button>
				)}
				<nav className="sidebar-nav" ref={navRef}>
					<ul>
						{userNavItems.length > 0 && <li className="nav-section-title">Benutzerbereich</li>}
						{userNavItems.map(item => (
							<li key={`${item.label}-${item.url}`}>{renderNavItem(item)}</li>
						))}

						{adminNavItems.length > 0 && <li className="nav-section-title">Admin-Bereich</li>}
						{adminNavItems.map(item => (
							<li key={`${item.label}-${item.url}`}>{renderNavItem(item)}</li>
						))}
					</ul>
				</nav>
				{isHorizontal && (
					<button className="scroll-btn right" onClick={() => handleScroll('right')} disabled={!scrollState.canScrollRight}>
						<i className="fas fa-chevron-right"></i>
					</button>
				)}
			</div>
			<div className="user-actions">
				<div className="user-info">
					Angemeldet als: <strong>{user.username}</strong>
				</div>
				<div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
					<NavLink to="/profil" className="btn btn-secondary btn-small" style={{ flexGrow: 1 }}>Profil</NavLink>
					<button onClick={handleLogout} className="btn btn-danger-outline btn-small" style={{ flexGrow: 1 }}>Logout</button>
					{isHorizontal && (
						<NavLink to="/notifications" className="btn btn-secondary btn-small" title="Benachrichtigungen" style={{ position: 'relative' }}>
							<i className="fas fa-bell"></i>
							{user.unseenNotificationsCount > 0 && (
								<span className="cart-badge" style={{ position: 'absolute', top: '-5px', right: '-5px', width: '18px', height: '18px', fontSize: '0.6rem' }}>
									{user.unseenNotificationsCount}
								</span>
							)}
						</NavLink>
					)}
					<ThemeSwitcher />
				</div>
			</div>
		</aside>
	);
};

export default Sidebar;