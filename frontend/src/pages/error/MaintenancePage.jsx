import React from 'react';
import { Link } from 'react-router-dom';

const MaintenancePage = ({ message }) => {
	const displayMessage = message || "Wir führen gerade einige Wartungsarbeiten durch. Die Anwendung ist in Kürze wieder für Sie verfügbar.";

	return (
		<div style={{ textAlign: 'center', color: 'var(--text-color)', margin: 'auto' }}>
			<i className="fas fa-tools" style={{ fontSize: '5rem', color: 'var(--primary-color)', marginBottom: '1.5rem' }}></i>
			<h1 style={{ fontSize: '2.5rem' }}>Anwendung im Wartungsmodus</h1>
			<p style={{ fontSize: '1.2rem', color: 'var(--text-muted-color)' }}>
				{displayMessage}
			</p>
			<p>Vielen Dank für Ihre Geduld!</p>
			<div style={{ marginTop: '2rem' }}>
				<p style={{ fontSize: '0.9rem', color: 'var(--text-muted-color)' }}>
					Administratoren können sich weiterhin anmelden, um den Wartungsmodus zu deaktivieren.
				</p>
				<Link to="/login" className="btn btn-secondary">
					Zur Login-Seite
				</Link>
			</div>
		</div>
	);
};

export default MaintenancePage;