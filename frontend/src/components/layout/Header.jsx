import React from 'react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

const Header = ({ onNavToggle }) => {
	const user = useAuthStore(state => state.user);

	return (
		<header className="mobile-header">
			<button className="mobile-nav-toggle" aria-label="Navigation umschalten" onClick={onNavToggle}>
				<span className="line line-1"></span>
				<span className="line line-2"></span>
				<span className="line line-3"></span>
			</button>
			<Link to="/home" className="mobile-logo">TechnikTeam</Link>
			<div className="mobile-header-right">
				<Link to="/profil">
					{user?.profilePicturePath ? (
						<img
							src={`/api/v1/public/files/avatars/user/${user.id}`}
							alt="Avatar"
							style={{ width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover' }}
						/>
					) : (
						<i className="fas fa-user-circle" style={{ fontSize: '1.5rem', color: 'var(--text-color)' }}></i>
					)}
				</Link>
			</div>
		</header>
	);
};

export default Header;