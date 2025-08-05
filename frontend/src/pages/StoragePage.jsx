import React, { useState, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import Lightbox from '../components/ui/Lightbox';
import { useToast } from '../context/ToastContext';

const CartModal = ({ isOpen, onClose, cart, onUpdateQuantity, onRemove, onSubmit, activeEvents, transactionError, isSubmitting }) => {
	const checkouts = cart.filter(item => item.type === 'checkout');
	const checkins = cart.filter(item => item.type === 'checkin');

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Warenkorb (${cart.length} Artikel)`}>
			<form onSubmit={onSubmit}>
				{transactionError && <p className="error-message">{transactionError}</p>}

				{checkouts.length > 0 && (
					<div style={{ marginBottom: '1.5rem' }}>
						<h4>Zu Entnehmen</h4>
						{checkouts.map(item => (
							<div className="dynamic-row" key={`${item.id}-checkout`}>
								<span style={{ flexGrow: 1 }}>{item.name}</span>
								<input type="number" value={item.quantity} onChange={e => onUpdateQuantity(item.id, 'checkout', parseInt(e.target.value, 10))} min="1" max={item.availableQuantity} className="form-group" style={{ maxWidth: '80px' }} />
								<button type="button" className="btn btn-small btn-danger" onClick={() => onRemove(item.id, 'checkout')}>×</button>
							</div>
						))}
					</div>
				)}

				{checkins.length > 0 && (
					<div>
						<h4>Einzuräumen</h4>
						{checkins.map(item => (
							<div className="dynamic-row" key={`${item.id}-checkin`}>
								<span style={{ flexGrow: 1 }}>{item.name}</span>
								<input type="number" value={item.quantity} onChange={e => onUpdateQuantity(item.id, 'checkin', parseInt(e.target.value, 10))} min="1" className="form-group" style={{ maxWidth: '80px' }} />
								<button type="button" className="btn btn-small btn-danger" onClick={() => onRemove(item.id, 'checkin')}>×</button>
							</div>
						))}
					</div>
				)}


				<div className="form-group" style={{ marginTop: '1.5rem' }}>
					<label htmlFor="transaction-notes">Notiz (optional, gilt für alle Artikel)</label>
					<input type="text" name="notes" id="transaction-notes" placeholder="z.B. für Event XYZ" />
				</div>
				<div className="form-group">
					<label htmlFor="transaction-eventId">Zuweisen zu Event (optional, gilt für alle Artikel)</label>
					<select name="eventId" id="transaction-eventId">
						<option value="">Kein Event</option>
						{activeEvents.map(event => (
							<option key={event.id} value={event.id}>{event.name}</option>
						))}
					</select>
				</div>

				<button type="submit" className="btn btn-success" style={{ width: '100%' }} disabled={isSubmitting || cart.length === 0}>
					{isSubmitting ? 'Wird verbucht...' : `Alle ${cart.length} Transaktionen ausführen`}
				</button>
			</form>
		</Modal>
	);
};


const StoragePage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/storage'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const [isCartModalOpen, setIsCartModalOpen] = useState(false);
	const [cart, setCart] = useState([]);
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [lightboxSrc, setLightboxSrc] = useState('');
	const [transactionError, setTransactionError] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const { addToast } = useToast();

	// Filters
	const [searchTerm, setSearchTerm] = useState('');
	const [categoryFilter, setCategoryFilter] = useState('');
	const [statusFilter, setStatusFilter] = useState('');

	const handleAddToCart = (item, type) => {
		const existingItem = cart.find(cartItem => cartItem.id === item.id && cartItem.type === type);
		if (existingItem) {
			addToast(`${item.name} ist bereits im Warenkorb für diese Aktion.`, 'info');
			return;
		}
		setCart(prevCart => [...prevCart, { ...item, quantity: 1, type }]);
		addToast(`${item.name} zum Warenkorb hinzugefügt.`, 'success');
	};

	const handleUpdateCartItemQuantity = (itemId, type, newQuantity) => {
		const numQuantity = Math.max(1, newQuantity || 1);
		setCart(prevCart => prevCart.map(item =>
			item.id === itemId && item.type === type ? { ...item, quantity: numQuantity } : item
		));
	};

	const handleRemoveFromCart = (itemId, type) => {
		setCart(prevCart => prevCart.filter(item => !(item.id === itemId && item.type === type)));
	};


	const handleImageClick = (imagePath) => {
		setLightboxSrc(`/api/v1/public/files/images/${imagePath}`);
		setIsLightboxOpen(true);
	};

	const handleBulkTransactionSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setTransactionError('');

		const formData = new FormData(e.target);
		const notes = formData.get('notes');
		const eventId = formData.get('eventId') ? parseInt(formData.get('eventId'), 10) : null;

		const transactionPromises = cart.map(item => {
			const transactionData = {
				itemId: item.id,
				quantity: item.quantity,
				type: item.type,
				notes,
				eventId,
			};
			return apiClient.post('/public/storage/transactions', transactionData);
		});

		try {
			const results = await Promise.all(transactionPromises);

			const failedResults = results.filter(r => !r.success);
			if (failedResults.length > 0) {
				const errorMessages = failedResults.map(r => r.message).join('; ');
				throw new Error(errorMessages);
			}

			addToast('Alle Transaktionen erfolgreich verbucht.', 'success');
			setIsCartModalOpen(false);
			setCart([]);
			reload();

		} catch (err) {
			setTransactionError(err.message || 'Einige Transaktionen sind fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const allItems = useMemo(() => {
		if (!data?.storageData) return [];
		return Object.values(data.storageData).flat();
	}, [data]);

	const categories = useMemo(() => {
		const cats = new Set(allItems.map(item => item.category).filter(Boolean));
		return Array.from(cats).sort();
	}, [allItems]);

	const filteredData = useMemo(() => {
		if (!data?.storageData) return {};

		const lowerCaseSearchTerm = searchTerm.toLowerCase();

		const filteredItems = allItems.filter(item => {
			const matchesSearch = item.name.toLowerCase().includes(lowerCaseSearchTerm) || (item.location && item.location.toLowerCase().includes(lowerCaseSearchTerm));
			const matchesCategory = !categoryFilter || item.category === categoryFilter;
			const matchesStatus = !statusFilter || (statusFilter === 'LOW_STOCK' && (item.maxQuantity > 0 && item.availableQuantity / item.maxQuantity <= 0.25)) || item.status === statusFilter;
			return matchesSearch && matchesCategory && matchesStatus;
		});

		// Regroup by location after filtering
		return filteredItems.reduce((acc, item) => {
			const location = item.location || 'Unbekannt';
			if (!acc[location]) {
				acc[location] = [];
			}
			acc[location].push(item);
			return acc;
		}, {});

	}, [data, searchTerm, categoryFilter, statusFilter, allItems]);


	if (loading) return <div>Lade Lagerdaten...</div>;
	if (error) return <div className="error-message">{error}</div>;

	const { activeEvents } = data;

	const renderItemsForLocation = (items) => (
		<>
			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Gerät</th>
							<th>Kategorie</th>
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
										<button className="btn btn-small btn-secondary" style={{ marginLeft: '0.5rem', padding: '0.2rem 0.5rem' }} title="Bild anzeigen" onClick={() => handleImageClick(item.imagePath)}>
											<i className="fas fa-camera"></i>
										</button>
									)}
								</td>
								<td>{item.category || '-'}</td>
								<td>
									<span>{item.availableQuantity} / {item.maxQuantity}</span>
									{item.defectiveQuantity > 0 && <span className="text-danger"> ({item.defectiveQuantity} defekt)</span>}
								</td>
								<td style={{ display: 'flex', gap: '0.5rem' }}>
									<button className="btn btn-small btn-danger-outline" onClick={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}>Entnehmen</button>
									<button className="btn btn-small btn-success" onClick={() => handleAddToCart(item, 'checkin')}>Einräumen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			<div className="mobile-card-list">
				{items.map(item => (
					<div className="list-item-card" key={item.id}>
						<h3 className="card-title">
							<Link to={`/lager/details/${item.id}`}>{item.name}</Link>
							{item.imagePath && (
								<button className="btn btn-small btn-secondary" style={{ marginLeft: '0.5rem', padding: '0.2rem 0.5rem' }} title="Bild anzeigen" onClick={() => handleImageClick(item.imagePath)}>
									<i className="fas fa-camera"></i>
								</button>
							)}
						</h3>
						<div className="card-row"><strong>Kategorie:</strong> <span>{item.category || '-'}</span></div>
						<div className="card-row"><strong>Bestand:</strong> <span>{item.availableQuantity} / {item.maxQuantity}{item.defectiveQuantity > 0 && <span className="text-danger"> ({item.defectiveQuantity} def.)</span>}</span></div>
						<div className="card-actions">
							<button className="btn btn-small btn-danger-outline" onClick={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}>Entnehmen</button>
							<button className="btn btn-small btn-success" onClick={() => handleAddToCart(item, 'checkin')}>Einräumen</button>
						</div>
					</div>
				))}
			</div>
		</>
	);

	return (
		<>
			{cart.length > 0 && (
				<button className="cart-fab" onClick={() => setIsCartModalOpen(true)}>
					<i className="fas fa-shopping-cart"></i>
					<span className="cart-badge">{cart.length}</span>
				</button>
			)}

			<h1><i className="fas fa-boxes"></i> Lagerübersicht</h1>
			<p>Hier finden Sie eine Übersicht aller erfassten Artikel im Lager. Klicken Sie auf einen Artikelnamen für Details und Verlauf.</p>

			<div className="card" style={{ marginBottom: '1.5rem' }}>
				<div className="table-controls">
					<div className="form-group" style={{ flexGrow: 1 }}>
						<label htmlFor="search-term">Suchen</label>
						<input type="text" id="search-term" value={searchTerm} onChange={e => setSearchTerm(e.target.value)} placeholder="Name, Ort..." />
					</div>
					<div className="form-group">
						<label htmlFor="category-filter">Kategorie</label>
						<select id="category-filter" value={categoryFilter} onChange={e => setCategoryFilter(e.target.value)}>
							<option value="">Alle</option>
							{categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
						</select>
					</div>
					<div className="form-group">
						<label htmlFor="status-filter">Status</label>
						<select id="status-filter" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
							<option value="">Alle</option>
							<option value="IN_STORAGE">Auf Lager</option>
							<option value="CHECKED_OUT">Entnommen</option>
							<option value="LOW_STOCK">Niedriger Bestand</option>
						</select>
					</div>
				</div>
			</div>


			{Object.entries(filteredData).map(([location, items]) => (
				<div className="card" key={location}>
					<h2><i className="fas fa-map-marker-alt"></i> {location}</h2>
					{renderItemsForLocation(items)}
				</div>
			))}
			{Object.keys(filteredData).length === 0 && !loading && (
				<div className="card"><p>Keine Artikel entsprechen Ihren Filterkriterien.</p></div>
			)}


			<CartModal
				isOpen={isCartModalOpen}
				onClose={() => setIsCartModalOpen(false)}
				cart={cart}
				onUpdateQuantity={handleUpdateCartItemQuantity}
				onRemove={handleRemoveFromCart}
				onSubmit={handleBulkTransactionSubmit}
				activeEvents={activeEvents}
				transactionError={transactionError}
				isSubmitting={isSubmitting}
			/>

			{isLightboxOpen && <Lightbox src={lightboxSrc} onClose={() => setIsLightboxOpen(false)} />}
		</>
	);
};

export default StoragePage;