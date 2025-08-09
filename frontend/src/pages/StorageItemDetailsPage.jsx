import React, { useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Lightbox from '../components/ui/Lightbox';
import DamageReportModal from '../components/storage/DamageReportModal';
import ReservationCalendar from '../components/storage/ReservationCalendar';
import { useToast } from '../context/ToastContext';

const RelatedItemsTab = ({ itemId }) => {
	const apiCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/relations`), [itemId]);
	const { data: relatedItems, loading, error } = useApi(apiCall);

	if (loading) return <p>Lade zugehörige Artikel...</p>;
	if (error) return <p className="error-message">{error}</p>;

	return (
		<div>
			<h4>Wird oft zusammen verwendet:</h4>
			{relatedItems && relatedItems.length > 0 ? (
				<ul className="details-list">
					{relatedItems.map(item => (
						<li key={item.id}>
							<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
							<span>{item.availableQuantity} / {item.maxQuantity} verfügbar</span>
						</li>
					))}
				</ul>
			) : (
				<p>Für diesen Artikel sind keine zugehörigen Artikel definiert.</p>
			)}
		</div>
	);
};

const StorageItemDetailsPage = () => {
	const { itemId } = useParams();
	const [activeTab, setActiveTab] = useState('history');
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [isReportModalOpen, setIsReportModalOpen] = useState(false);
	const { addToast } = useToast();

	const fetchItemCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const fetchHistoryCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/history`), [itemId]);
	const fetchReservationsCall = useCallback(() => {
		// Only fetch if the calendar tab is active
		if (activeTab !== 'calendar') return null;
		return apiClient.get(`/public/storage/${itemId}/reservations`);
	}, [itemId, activeTab]);


	const { data: itemData, loading: itemLoading, error: itemError, reload: reloadItem } = useApi(fetchItemCall);
	const { data: historyData, loading: historyLoading, error: historyError } = useApi(fetchHistoryCall);
	const { data: reservations, loading: reservationsLoading, error: reservationsError } = useApi(fetchReservationsCall);


	const handleReportSuccess = () => {
		setIsReportModalOpen(false);
		addToast('Schadensmeldung erfolgreich übermittelt.', 'success');
		// No need to reload data, as the report is handled by admins
	};

	const getImagePath = (path) => {
		// The API endpoint for images doesn't include the subfolder in the URL parameter.
		// It expects only the filename. The backend service adds the subfolder.
		const filename = path.split('/').pop();
		return `/api/v1/public/files/images/${filename}`;
	};

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
							src={getImagePath(item.imagePath)}
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
					<button className="btn btn-warning" style={{ marginTop: '1.5rem' }} onClick={() => setIsReportModalOpen(true)}>
						<i className="fas fa-tools"></i> Schaden melden
					</button>
				</div>

				<div className="card">
					<div className="modal-tabs">
						<button className={`modal-tab-button ${activeTab === 'history' ? 'active' : ''}`} onClick={() => setActiveTab('history')}>Verlauf</button>
						<button className={`modal-tab-button ${activeTab === 'maintenance' ? 'active' : ''}`} onClick={() => setActiveTab('maintenance')}>Wartungshistorie</button>
						<button className={`modal-tab-button ${activeTab === 'calendar' ? 'active' : ''}`} onClick={() => setActiveTab('calendar')}>Verfügbarkeit</button>
						<button className={`modal-tab-button ${activeTab === 'related' ? 'active' : ''}`} onClick={() => setActiveTab('related')}>Zubehör</button>
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
					{activeTab === 'calendar' && (
						<div className="modal-tab-content active">
							{reservationsLoading && <p>Lade Reservierungskalender...</p>}
							{reservationsError && <p className="error-message">{reservationsError}</p>}
							{reservations && <ReservationCalendar reservations={reservations} />}
						</div>
					)}
					{activeTab === 'related' && (
						<div className="modal-tab-content active">
							<RelatedItemsTab itemId={itemId} />
						</div>
					)}
				</div>
			</div>
			<div style={{ marginTop: '1rem' }}>
				<Link to="/lager" className="btn btn-secondary"><i className="fas fa-arrow-left"></i> Zur Lagerübersicht</Link>
			</div>

			{isLightboxOpen && <Lightbox src={getImagePath(item.imagePath)} onClose={() => setIsLightboxOpen(false)} />}

			<DamageReportModal
				isOpen={isReportModalOpen}
				onClose={() => setIsReportModalOpen(false)}
				onSuccess={handleReportSuccess}
				item={item}
			/>
		</>
	);
};

export default StorageItemDetailsPage;