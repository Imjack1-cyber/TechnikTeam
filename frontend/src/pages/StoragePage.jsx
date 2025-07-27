import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import Lightbox from '../components/ui/Lightbox';

const StoragePage = () => {
	const { data, loading, error, reload } = useApi(() => apiClient.get('/public/storage'));
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [selectedItem, setSelectedItem] = useState(null);
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [lightboxSrc, setLightboxSrc] = useState('');
	const [transactionError, setTransactionError] = useState('');

	const handleActionClick = (item) => {
		setSelectedItem(item);
		setTransactionError('');
		setIsModalOpen(true);
	};

	const handleImageClick = (imagePath) => {
		setLightboxSrc(`/api/v1/public/files/images/${imagePath}`);
		setIsLightboxOpen(true);
	};

	const handleTransactionSubmit = async (e) => {
		e.preventDefault();
		const submitter = e.nativeEvent.submitter;
		const formData = new FormData(e.target);
		const transactionData = {
			itemId: selectedItem.id,
			quantity: parseInt(formData.get('quantity'), 10),
			type: submitter.value, // 'checkout' or 'checkin'
			notes: formData.get('notes'),
			eventId: formData.get('eventId') ? parseInt(formData.get('eventId'), 10) : null,
		};

		try {
			const result = await apiClient.post('/public/storage/transactions', transactionData);
			if (result.success) {
				setIsModalOpen(false);
				reload(); // Reload the storage data
				// Ideally, a global toast notification would be shown here
				console.log('Transaction successful:', result.message);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setTransactionError(err.message || 'Transaction failed.');
		}
	};

	if (loading) return <div>Lade Lagerdaten...</div>;
	if (error) return <div className="error-message">{error}</div>;

	const { storageData, activeEvents } = data;

	return (
		<>
			<h1><i className="fas fa-boxes"></i> Lagerübersicht</h1>
			<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager. Klicken Sie auf einen Artikelnamen für Details und Verlauf.</p>

			{Object.entries(storageData).map(([location, items]) => (
				<div className="card" key={location}>
					<h2><i className="fas fa-map-marker-alt"></i> {location}</h2>
					<div className="desktop-table-wrapper">
						<table className="data-table">
							<thead>
								<tr>
									<th>Gerät</th>
									<th>Schrank</th>
									<th>Fach</th>
									<th>Status</th>
									<th>Bestand</th>
									<th>Aktion</th>
								</tr>
							</thead>
							<tbody>
								{items.map(item => (
									<tr key={item.id}>
										<td className="item-name-cell">
											<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
											{item.imagePath && (
												<button className="camera-btn" title="Bild anzeigen" onClick={() => handleImageClick(item.imagePath)}>
													<i className="fas fa-camera"></i>
												</button>
											)}
										</td>
										<td>{item.cabinet || '-'}</td>
										<td>{item.compartment || '-'}</td>
										<td>
											<span className={`status-badge ${item.availabilityStatusCssClass}`}>
												{item.availabilityStatus}
											</span>
										</td>
										<td>
											<span className="inventory-details">{item.availableQuantity} / {item.maxQuantity}</span>
											{item.defectiveQuantity > 0 && <span className="inventory-details text-danger">({item.defectiveQuantity} defekt)</span>}
										</td>
										<td>
											<button className="btn btn-small" onClick={() => handleActionClick(item)}>Aktion</button>
										</td>
									</tr>
								))}
							</tbody>
						</table>
					</div>
				</div>
			))}

			{selectedItem && (
				<Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={`${selectedItem.name}: Entnehmen / Einräumen`}>
					<form onSubmit={handleTransactionSubmit}>
						{transactionError && <p className="error-message">{transactionError}</p>}
						<div className="form-group">
							<label htmlFor="transaction-quantity">Anzahl</label>
							<input type="number" name="quantity" id="transaction-quantity" defaultValue="1" min="1" max={selectedItem.availableQuantity} required />
						</div>
						<div className="form-group">
							<label htmlFor="transaction-notes">Notiz (optional)</label>
							<input type="text" name="notes" id="transaction-notes" placeholder="z.B. für Event XYZ" />
						</div>
						<div className="form-group">
							<label htmlFor="transaction-eventId">Zuweisen zu Event (optional)</label>
							<select name="eventId" id="transaction-eventId">
								<option value="">Kein Event</option>
								{activeEvents.map(event => (
									<option key={event.id} value={event.id}>{event.name}</option>
								))}
							</select>
						</div>
						<div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem' }}>
							<button type="submit" name="type" value="checkout" className="btn btn-danger" style={{ flexGrow: 1 }} disabled={selectedItem.availableQuantity <= 0}>Entnehmen</button>
							<button type="submit" name="type" value="checkin" className="btn btn-success" style={{ flexGrow: 1 }} disabled={selectedItem.maxQuantity > 0 && selectedItem.quantity >= selectedItem.maxQuantity}>Einräumen</button>
						</div>
					</form>
				</Modal>
			)}

			{isLightboxOpen && <Lightbox src={lightboxSrc} onClose={() => setIsLightboxOpen(false)} />}
		</>
	);
};

export default StoragePage;