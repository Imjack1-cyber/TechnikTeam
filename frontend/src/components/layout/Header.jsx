import React from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const Header = ({ onNavToggle }) => {
	const user = useAuthStore(state => state.user);
	const unseenCount = user?.unseenNotificationsCount || 0;

	return (
		<header className="mobile-header">
			<button className="mobile-nav-toggle" aria-label="Navigation umschalten" onClick={onNavToggle}>
				<span className="line line-1"></span>
				<span className="line line-2"></span>
				<span className="line line-3"></span>
			</button>
			<Link to="/home" className="mobile-logo">TechnikTeam</Link>
			<div className="mobile-header-right">
				<Link to="/notifications" style={{ position: 'relative' }}>
					<i className="fas fa-bell" style={{ fontSize: '1.5rem', color: 'var(--text-color)' }}></i>
					{unseenCount > 0 && (
						<span className="cart-badge" style={{ position: 'absolute', top: '-5px', right: '-8px', width: '20px', height: '20px', fontSize: '0.7rem' }}>
							{unseenCount}
						</span>
					)}
				</Link>
				<Link to="/profil">
					<i className={`fas ${user?.profileIconClass || 'fa-user-circle'}`} style={{ fontSize: '1.5rem', color: 'var(--text-color)' }}></i>
				</Link>
			</div>
		</header>
	);
};

export default Header;