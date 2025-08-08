import React, { useCallback, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';

const QrActionPage = () => {
	const { itemId } = useParams();
	const { addToast } = useToast();
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const [quantity, setQuantity] = useState(1);
	const [activeAction, setActiveAction] = useState(null); // 'checkout' or 'checkin'

	const itemApiCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const eventsApiCall = useCallback(() => apiClient.get('/public/events'), []);

	const { data: item, loading: itemLoading, error: itemError, reload: reloadItem } = useApi(itemApiCall);
	const { data: activeEvents, loading: eventsLoading, error: eventsError } = useApi(eventsApiCall);

	const handleQuantityChange = (e) => {
		let value = parseInt(e.target.value, 10) || 1;
		setQuantity(value);
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		if (!activeAction) return;

		setIsLoading(true);
		setError('');

		const formData = new FormData(e.target);
		const payload = {
			itemId: parseInt(itemId, 10),
			quantity: quantity,
			type: activeAction,
			eventId: formData.get('eventId') ? parseInt(formData.get('eventId'), 10) : null,
			notes: formData.get('notes'),
		};

		try {
			const result = await apiClient.post('/public/storage/transactions', payload);
			if (result.success) {
				addToast(result.message, 'success');
				setQuantity(1); // Reset quantity
				setActiveAction(null);
				reloadItem(); // Reload item data to show updated quantities
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || `Aktion '${activeAction}' fehlgeschlagen.`);
		} finally {
			setIsLoading(false);
		}
	};


	if (itemLoading || eventsLoading) return <div>Lade...</div>;
	if (itemError) return <div className="error-message">{itemError}</div>;
	if (eventsError) return <div className="error-message">{eventsError}</div>;
	if (!item) return <div className="error-message">Artikel nicht gefunden.</div>;

	const maxCheckout = item.availableQuantity;
	const maxCheckin = item.maxQuantity > 0 ? item.maxQuantity - item.quantity : Infinity;


	return (
		<div className="qr-action-body">
			<div className="card qr-action-container">
				<h1>Aktion für:</h1>
				<h2 className="qr-action-item-name">{item.name}</h2>
				<p>Verfügbar: <strong>{item.availableQuantity}</strong> / {item.maxQuantity}</p>

				{error && <p className="error-message">{error}</p>}

				<form onSubmit={handleSubmit}>
					<div className="form-group">
						<label htmlFor="quantity">Anzahl</label>
						<input
							type="number"
							id="quantity"
							name="quantity"
							value={quantity}
							onChange={handleQuantityChange}
							min="1"
							max={activeAction === 'checkout' ? maxCheckout : (maxCheckin === Infinity ? undefined : maxCheckin)}
							required
						/>
						{activeAction === 'checkout' && <small>Maximal {maxCheckout} entnehmbar.</small>}
						{activeAction === 'checkin' && maxCheckin !== Infinity && <small>Maximal {maxCheckin} einräumbar.</small>}
					</div>
					<div className="form-group">
						<label htmlFor="eventId">Event (optional)</label>
						<select id="eventId" name="eventId">
							<option value="">Kein Event</option>
							{activeEvents?.map(event => (
								<option key={event.id} value={event.id}>{event.name}</option>
							))}
						</select>
					</div>
					<div className="form-group">
						<label htmlFor="notes">Notiz (optional)</label>
						<input type="text" id="notes" name="notes" />
					</div>
					<div className="qr-action-buttons">
						<button
							type="submit"
							name="type"
							value="checkout"
							className="btn qr-action-btn btn-danger"
							disabled={isLoading || maxCheckout <= 0}
							onClick={() => setActiveAction('checkout')}
						>
							{isLoading && activeAction === 'checkout' ? '...' : 'Entnehmen'}
						</button>
						<button
							type="submit"
							name="type"
							value="checkin"
							className="btn qr-action-btn btn-success"
							disabled={isLoading || maxCheckin <= 0}
							onClick={() => setActiveAction('checkin')}
						>
							{isLoading && activeAction === 'checkin' ? '...' : 'Einräumen'}
						</button>
					</div>
				</form>

				<div style={{ marginTop: '2rem' }}>
					<Link to="/lager">Zurück zur Lagerübersicht</Link>
				</div>
			</div>
		</div>
	);
};

export default QrActionPage;