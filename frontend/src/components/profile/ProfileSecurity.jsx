import React from 'react';
import { Link } from 'react-router-dom';
// REMOVED unused imports

const ProfileSecurity = ({ onUpdate }) => {
	// REMOVED all passkey-related state and handlers

	return (
		<div className="card" id="profile-security-container">
			<h2 className="card-title">Sicherheit</h2>
			<ul className="details-list">
				<li>
					<Link to="/passwort" className="btn btn-secondary">Passwort ändern</Link>
				</li>
			</ul>
			<h3 style={{ marginTop: '1.5rem', fontSize: '1.1rem' }}>Passkeys (Passwortloser Login)</h3>
			{/* REPLACED passkey logic with placeholder */}
			<p className="text-muted">
				Dieses Feature wird zurzeit überarbeitet und ist in Kürze wieder verfügbar.
			</p>
			<button className="btn btn-success" disabled={true} style={{ marginBottom: '1rem' }}>
				<i className="fas fa-plus-circle"></i> Neues Gerät registrieren
			</button>
		</div>
	);
};

export default ProfileSecurity;