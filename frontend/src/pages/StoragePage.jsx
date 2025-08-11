import React, { useState, useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import Lightbox from '../components/ui/Lightbox';
import { useToast } from '../context/ToastContext';
import CartModal from '../components/storage/CartModal';

const AvailabilityBar = ({ available, max }) => {
	if (max === 0) {
		return <div title={`Verfügbar: ${available}`} style={{ height: '8px', background: 'var(--success-color)', borderRadius: '4px' }} />;
	}
	const percentage = Math.max(0, (available / max) * 100);
	let color = 'var(--success-color)';
	if (percentage <= 25) color = 'var(--danger-color)';
	else if (percentage <= 50) color = 'var(--warning-color)';

	return (
		<div style={{ height: '8px', background: 'var(--bg-color)', borderRadius: '4px', overflow: 'hidden' }}>
			<div style={{ width: `${percentage}%`, height: '100%', background: color, transition: 'width 0.3s' }} />
		</div>
	);
};

const StoragePage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/storage'), []);
	const { data, loading, error, reload } = useApi(apiCall);
	const [isCartModalOpen, setIsCartModalOpen] = useState(false);
	const [cart, setCart] = useState([]);
	const [quickAddQuantities, setQuickAddQuantities] = useState({});
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [lightboxSrc, setLightboxSrc] = useState('');
	const [transactionError, setTransactionError] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const { addToast } = useToast();

	// Filters
	const [searchTerm, setSearchTerm] = useState('');
	const [categoryFilter, setCategoryFilter] = useState('');
	const [statusFilter, setStatusFilter] = useState('');
	const [showOnlyAvailable, setShowOnlyAvailable] = useState(false);

	const handleAddToCart = (item, type) => {
		const existingItem = cart.find(cartItem => cartItem.id === item.id && cartItem.type === type);
		if (existingItem) {
			addToast(`${item.name} ist bereits im Warenkorb für diese Aktion.`, 'info');
			return;
		}
		const quantity = quickAddQuantities[item.id] || 1;
		setCart(prevCart => [...prevCart, { ...item, cartQuantity: quantity, type }]);
		addToast(`${item.name} (${quantity}x) zum Warenkorb hinzugefügt.`, 'success');
	};


	const handleUpdateCartItemQuantity = (itemId, type, newQuantity) => {
		setCart(prevCart => prevCart.map(cartItem => {
			if (cartItem.id === itemId && cartItem.type === type) {
				const originalItem = allItems.find(i => i.id === itemId);
				let validatedQuantity = Math.max(1, newQuantity || 1);

				if (type === 'checkout') {
					validatedQuantity = Math.min(validatedQuantity, originalItem.availableQuantity);
				} else { // checkin
					const maxCheckin = originalItem.maxQuantity > 0 ? originalItem.maxQuantity - originalItem.quantity : Infinity;
					if (maxCheckin !== Infinity) {
						validatedQuantity = Math.min(validatedQuantity, maxCheckin);
					}
				}
				return { ...cartItem, cartQuantity: validatedQuantity };
			}
			return cartItem;
		}));
	};

	const handleRemoveFromCart = (itemId, type) => {
		setCart(prevCart => prevCart.filter(item => !(item.id === itemId && item.type === type)));
	};

	const handleSwitchCartItemType = (itemId, currentType) => {
		setCart(prevCart => prevCart.map(item => {
			if (item.id === itemId && item.type === currentType) {
				const existsInOtherCategory = prevCart.some(i => i.id === itemId && i.type !== currentType);
				if (existsInOtherCategory) {
					addToast(`${item.name} ist bereits in der anderen Kategorie im Warenkorb.`, 'info');
					return item;
				}
				return { ...item, type: currentType === 'checkout' ? 'checkin' : 'checkout' };
			}
			return item;
		}));
	};

	const getImagePath = (path) => {
		const filename = path.split('/').pop();
		return `/api/v1/public/files/images/${filename}`;
	};

	const handleImageClick = (imagePath) => {
		setLightboxSrc(getImagePath(imagePath));
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
				quantity: item.cartQuantity, // Use the correct quantity field
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
			if (showOnlyAvailable && item.availableQuantity === 0) return false;
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

	}, [data, searchTerm, categoryFilter, statusFilter, allItems, showOnlyAvailable]);


	if (loading) return <div>Lade Lagerdaten...</div>;
	if (error) return <div className="error-message">{error}</div>;

	const { activeEvents } = data;

	const renderItemsForLocation = (items) => (
		<>
			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th style={{ width: '40%' }}>Gerät</th>
							<th>Bestand</th>
							<th style={{ width: '30%' }}>Aktion</th>
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
									{item.nextReservationDate && (
										<i className="fas fa-calendar-alt" style={{ marginLeft: '0.5rem', color: 'var(--primary-color)' }} title={`Nächste Reservierung am ${new Date(item.nextReservationDate).toLocaleDateString('de-DE')}`}></i>
									)}
									<div style={{ marginTop: '0.25rem' }}>
										<AvailabilityBar available={item.availableQuantity} max={item.maxQuantity} />
									</div>
								</td>
								<td>
									<span>{item.availableQuantity} / {item.maxQuantity}</span>
									{item.defectiveQuantity > 0 && <span className="text-danger"> ({item.defectiveQuantity} defekt)</span>}
								</td>
								<td style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
									<input type="number" defaultValue="1" min="1" max={item.availableQuantity} onChange={e => setQuickAddQuantities(prev => ({ ...prev, [item.id]: parseInt(e.target.value, 10) || 1 }))} style={{ width: '60px', textAlign: 'center' }} className="form-group" />
									<button className="btn btn-small btn-danger-outline" onClick={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}><i className="fas fa-minus"></i> Entnehmen</button>
									<button className="btn btn-small btn-success" onClick={() => handleAddToCart(item, 'checkin')}><i className="fas fa-plus"></i> Einräumen</button>
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
							{item.nextReservationDate && (
								<i className="fas fa-calendar-alt" style={{ marginLeft: '0.5rem', color: 'var(--primary-color)' }} title={`Nächste Reservierung am ${new Date(item.nextReservationDate).toLocaleDateString('de-DE')}`}></i>
							)}
						</h3>
						<AvailabilityBar available={item.availableQuantity} max={item.maxQuantity} />
						<div className="card-row"><strong>Bestand:</strong> <span>{item.availableQuantity} / {item.maxQuantity}{item.defectiveQuantity > 0 && <span className="text-danger"> ({item.defectiveQuantity} def.)</span>}</span></div>
						<div className="card-actions" style={{ alignItems: 'center' }}>
							<input type="number" defaultValue="1" min="1" max={item.availableQuantity} onChange={e => setQuickAddQuantities(prev => ({ ...prev, [item.id]: parseInt(e.target.value, 10) || 1 }))} style={{ width: '60px', textAlign: 'center' }} className="form-group" />
							<button className="btn btn-small btn-danger-outline" onClick={() => handleAddToCart(item, 'checkout')} disabled={item.availableQuantity <= 0}><i className="fas fa-minus"></i></button>
							<button className="btn btn-small btn-success" onClick={() => handleAddToCart(item, 'checkin')}><i className="fas fa-plus"></i></button>
						</div>
					</div>
				))}
			</div>
		</>
	);

	return (
		<>
			<button className="cart-fab" onClick={() => setIsCartModalOpen(true)} style={{ opacity: cart.length > 0 ? 1 : 0.5 }}>
				<i className="fas fa-shopping-cart"></i>
				<span className="cart-badge">{cart.length}</span>
			</button>

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
					<div className="form-group" style={{ alignSelf: 'flex-end' }}>
						<label>
							<input type="checkbox" checked={showOnlyAvailable} onChange={e => setShowOnlyAvailable(e.target.checked)} />
							Nur verfügbare anzeigen
						</label>
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
				onSwitchType={handleSwitchCartItemType}
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