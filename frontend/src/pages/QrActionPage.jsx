import React, { useCallback, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const QrActionPage = () => {
	const { itemId } = useParams();
	const { addToast } = useToast();
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');

	const itemApiCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const eventsApiCall = useCallback(() => apiClient.get('/public/events'), []);

	const { data: item, loading: itemLoading, error: itemError, reload: reloadItem } = useApi(itemApiCall);
	const { data: activeEvents, loading: eventsLoading, error: eventsError } = useApi(eventsApiCall);

	const handleTransaction = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setError('');

		const formData = new FormData(e.target);
		const type = e.nativeEvent.submitter.value;
		const payload = {
			itemId: parseInt(itemId, 10),
			quantity: parseInt(formData.get('quantity'), 10),
			type: type,
			eventId: formData.get('eventId') ? parseInt(formData.get('eventId'), 10) : null,
			notes: formData.get('notes'),
		};

		try {
			const result = await apiClient.post('/public/storage/transactions', payload);
			if (result.success) {
				addToast(result.message, 'success');
				reloadItem(); // Reload item data to show updated quantities
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || `Aktion '${type}' fehlgeschlagen.`);
		} finally {
			setIsLoading(false);
		}
	};

	if (itemLoading || eventsLoading) return <div>Lade...</div>;
	if (itemError) return <div className="error-message">{itemError}</div>;
	if (eventsError) return <div className="error-message">{eventsError}</div>;
	if (!item) return <div className="error-message">Artikel nicht gefunden.</div>;

	return (
		<div className="qr-action-body">
			<div className="card qr-action-container">
				<h1>Aktion für:</h1>
				<h2 className="qr-action-item-name">{item.name}</h2>
				<p>Verfügbar: <strong>{item.availableQuantity}</strong> / {item.maxQuantity}</p>

				{error && <p className="error-message">{error}</p>}

				<form onSubmit={handleTransaction}>
					<div className="form-group">
						<label htmlFor="quantity">Anzahl</label>
						<input type="number" id="quantity" name="quantity" defaultValue="1" min="1" max={item.availableQuantity} required />
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
						<button type="submit" name="type" value="checkout" className="btn qr-action-btn btn-danger" disabled={isLoading || item.availableQuantity <= 0}>
							{isLoading ? '...' : 'Entnehmen'}
						</button>
						<button type="submit" name="type" value="checkin" className="btn qr-action-btn btn-success" disabled={isLoading}>
							{isLoading ? '...' : 'Einräumen'}
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