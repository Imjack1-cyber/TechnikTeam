import React from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const PackKitPage = () => {
	const { kitId } = useParams();

	// NOTE: This assumes a public API endpoint at `/api/v1/public/kits/${kitId}` will be created.
	// This endpoint should return the kit's details and its list of items.
	const { data: kitData, loading, error } = useApi(() => apiClient.get(`/kits/${kitId}`));

	if (loading) return <div>Lade Packliste...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!kitData) return <div className="error-message">Kit nicht gefunden.</div>;

	const { kit, items } = kitData; // Assuming API returns { kit: {...}, items: [...] }

	return (
		<div className="card">
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
				<div>
					<h1>Packliste: {kit.name}</h1>
					<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
						{kit.description}
					</p>
				</div>
				<button className="btn no-print" onClick={() => window.print()}>
					<i className="fas fa-print"></i> Drucken
				</button>
			</div>

			{kit.location && (
				<div className="card" style={{ backgroundColor: 'var(--bg-color)' }}>
					<h3 className="card-title" style={{ border: 'none', padding: 0 }}>Standort</h3>
					<p style={{ fontSize: '1.2rem', fontWeight: '500' }}>{kit.location}</p>
				</div>
			)}

			<h3 style={{ marginTop: '2rem' }}>Inhalt zum Einpacken</h3>
			<ul className="details-list">
				{!items || items.length === 0 ? (
					<li>Dieses Kit hat keinen definierten Inhalt.</li>
				) : (
					items.map(item => (
						<li key={item.itemId}>
							<label style={{ display: 'flex', alignItems: 'center', gap: '1rem', cursor: 'pointer', width: '100%' }}>
								<input type="checkbox" style={{ width: '1.5rem', height: '1.5rem', flexShrink: 0 }} />
								<span>
									<strong>{item.quantity}x</strong> {item.itemName}
								</span>
							</label>
						</li>
					))
				)}
			</ul>
			<div className="no-print" style={{ marginTop: '2rem', textAlign: 'center' }}>
				<Link to="/lager" className="btn btn-secondary">Zurück zur Lagerübersicht</Link>
			</div>
		</div>
	);
};

export default PackKitPage;