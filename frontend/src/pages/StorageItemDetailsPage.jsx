import React, { useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Lightbox from '../components/ui/Lightbox';

const StorageItemDetailsPage = () => {
	const { itemId } = useParams();
	const [activeTab, setActiveTab] = useState('history');
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);

	const { data: itemData, loading: itemLoading, error: itemError } = useApi(() => apiClient.get(`/storage/${itemId}`));
	const { data: historyData, loading: historyLoading, error: historyError } = useApi(() => apiClient.get(`/public/storage/${itemId}/history`));

	if (itemLoading || historyLoading) return <div>Lade Artikeldetails...</div>;
	if (itemError) return <div className="error-message">{itemError}</div>;
	if (!itemData) return <div className="error-message">Artikel nicht gefunden.</div>;

	const item = itemData;
	const { transactions, maintenance } = historyData || { transactions: [], maintenance: [] };

	return (
		<>
			<h1><i className="fas fa-cube"></i> Artikeldetails</h1>
			<div className="responsive-dashboard-grid" style={{ alignItems: 'flex-start' }}>
				<div className="card">
					<h2 className="card-title">{item.name}</h2>
					{item.imagePath && (
						<img
							src={`/api/v1/public/files/images/${item.imagePath}`}
							alt={item.name}
							style={{ width: '100%', borderRadius: 'var(--border-radius)', marginBottom: '1rem', cursor: 'zoom-in' }}
							onClick={() => setIsLightboxOpen(true)}
						/>
					)}
					<ul className="details-list">
						<li><strong>Allg. Status:</strong> <span className={`status-badge ${item.availabilityStatusCssClass}`}>{item.availabilityStatus}</span></li>
						<li><strong>Verfügbar / Gesamt:</strong> <span>{item.availableQuantity} / {item.quantity}</span></li>
						<li><strong>Defekt:</strong> <span>{item.defectiveQuantity}</span></li>
						<li><strong>Tracking-Status:</strong> <span>{item.status}</span></li>
						{item.currentHolderUsername && <li><strong>Aktueller Inhaber:</strong> <span>{item.currentHolderUsername}</span></li>}
						<li><strong>Ort:</strong> <span>{item.location}</span></li>
						<li><strong>Schrank:</strong> <span>{item.cabinet || 'N/A'}</span></li>
						<li><strong>Fach:</strong> <span>{item.compartment || 'N/A'}</span></li>
					</ul>
				</div>

				<div className="card">
					<div className="modal-tabs">
						<button className={`modal-tab-button ${activeTab === 'history' ? 'active' : ''}`} onClick={() => setActiveTab('history')}>Verlauf</button>
						<button className={`modal-tab-button ${activeTab === 'maintenance' ? 'active' : ''}`} onClick={() => setActiveTab('maintenance')}>Wartungshistorie</button>
					</div>

					{activeTab === 'history' && (
						<div className="modal-tab-content active">
							{historyError && <p className="error-message">{historyError}</p>}
							{!historyLoading && transactions.length === 0 && <p>Kein Verlauf für diesen Artikel vorhanden.</p>}
							{transactions.length > 0 && (
								<div className="table-wrapper" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
									<table className="data-table">
										<thead><tr><th>Wann</th><th>Aktion</th><th>Wer</th><th>Notiz</th></tr></thead>
										<tbody>
											{transactions.map(entry => (
												<tr key={entry.id}>
													<td>{new Date(entry.transactionTimestamp).toLocaleString('de-DE')}</td>
													<td><span className={`status-badge ${entry.quantityChange > 0 ? 'status-ok' : 'status-danger'}`}>{entry.quantityChange > 0 ? '+' : ''}{entry.quantityChange}</span></td>
													<td>{entry.username}</td>
													<td>{entry.notes || '-'}</td>
												</tr>
											))}
										</tbody>
									</table>
								</div>
							)}
						</div>
					)}

					{activeTab === 'maintenance' && (
						<div className="modal-tab-content active">
							{historyError && <p className="error-message">{historyError}</p>}
							{!historyLoading && maintenance.length === 0 && <p>Keine Wartungseinträge für diesen Artikel vorhanden.</p>}
							{maintenance.length > 0 && (
								<div className="table-wrapper" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
									<table className="data-table">
										<thead><tr><th>Datum</th><th>Aktion</th><th>Bearbeiter</th><th>Notiz</th></tr></thead>
										<tbody>
											{maintenance.map(entry => (
												<tr key={entry.id}>
													<td>{new Date(entry.logDate).toLocaleString('de-DE')}</td>
													<td>{entry.action}</td>
													<td>{entry.username}</td>
													<td>{entry.notes || '-'}</td>
												</tr>
											))}
										</tbody>
									</table>
								</div>
							)}
						</div>
					)}
				</div>
			</div>
			<div style={{ marginTop: '1rem' }}>
				<Link to="/lager" className="btn btn-secondary"><i className="fas fa-arrow-left"></i> Zur Lagerübersicht</Link>
			</div>

			{isLightboxOpen && <Lightbox src={`/api/v1/public/files/images/${item.imagePath}`} onClose={() => setIsLightboxOpen(false)} />}
		</>
	);
};

export default StorageItemDetailsPage;