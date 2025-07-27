import React from 'react';
import { Link } from 'react-router-dom';

const Header = ({ onNavToggle }) => {
	return (
		<header className="mobile-header">
			<button className="mobile-nav-toggle" aria-label="Navigation umschalten" onClick={onNavToggle}>
				<span className="line line-1"></span>
				<span className="line line-2"></span>
				<span className="line line-3"></span>
			</button>
			<Link to="/home" className="mobile-logo">TechnikTeam</Link>
			<div className="mobile-header-right">
				{/* Theme switcher can go here if needed in the future */}
				<Link to="/profil">
					<i className="fas fa-user-circle" style={{ fontSize: '1.5rem' }}></i>
				</Link>
			</div>
		</header>
	);
};

export default Header;