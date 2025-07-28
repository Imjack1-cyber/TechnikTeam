import React, { useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const PackKitPage = () => {
	const { kitId } = useParams();
	const apiCall = useCallback(() => apiClient.get(`/kits/${kitId}`), [kitId]);
	const { data: kit, loading, error } = useApi(apiCall);

	if (loading) return <div>Lade Packliste...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!kit) return <div className="error-message">Kit nicht gefunden.</div>;

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
				{!kit.items || kit.items.length === 0 ? (
					<li>Dieses Kit hat keinen definierten Inhalt.</li>
				) : (
					kit.items.map(item => (
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