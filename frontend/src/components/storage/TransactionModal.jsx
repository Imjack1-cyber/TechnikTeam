import React, { useState } from 'react';
import Modal from '../ui/Modal';
import { useAuthStore } from '../../store/authStore';

const TransactionModal = ({ isOpen, onClose, item, activeEvents, onSuccess }) => {
	const user = useAuthStore(state => state.user);
	const [type, setType] = useState('checkout');
	const [quantity, setQuantity] = useState(1);
	const [eventId, setEventId] = useState('');
	const [notes, setNotes] = useState('');
	const [error, setError] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);

	if (!item) return null;

	const maxCheckout = item.availableQuantity;
	const maxCheckin = item.maxQuantity > 0 ? item.maxQuantity - item.quantity : Infinity;
	const maxQuantity = type === 'checkout' ? maxCheckout : maxCheckin;

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			itemId: item.id,
			quantity: parseInt(quantity, 10),
			type,
			eventId: eventId ? parseInt(eventId, 10) : null,
			notes
		};

		try {
			// This call is now handled by StorageService in the backend
			const result = await apiClient.post('/public/storage/transactions', payload);
			if (result.success) {
				onSuccess(result.message);
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Transaktion fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Transaktion für: ${item.name}`}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Aktion</label>
					<select value={type} onChange={e => setType(e.target.value)}>
						<option value="checkout">Entnehmen</option>
						<option value="checkin">Einräumen</option>
					</select>
				</div>
				<div className="form-group">
					<label htmlFor="quantity">Anzahl</label>
					<input
						type="number"
						id="quantity"
						name="quantity"
						value={quantity}
						onChange={e => setQuantity(e.target.value)}
						min="1"
						max={maxQuantity === Infinity ? undefined : maxQuantity}
						required
					/>
					<small>
						{type === 'checkout' ? `Maximal ${maxCheckout} entnehmbar.` : (maxCheckin !== Infinity ? `Maximal ${maxCheckin} einräumbar.` : 'Kein Limit.')}
					</small>
				</div>
				<div className="form-group">
					<label htmlFor="eventId">Event (optional)</label>
					<select id="eventId" name="eventId" value={eventId} onChange={e => setEventId(e.target.value)}>
						<option value="">Kein Event</option>
						{activeEvents.map(event => (
							<option key={event.id} value={event.id}>{event.name}</option>
						))}
					</select>
				</div>
				<div className="form-group">
					<label htmlFor="notes">Notiz (optional)</label>
					<input type="text" id="notes" name="notes" value={notes} onChange={e => setNotes(e.target.value)} />
				</div>
				<button type="submit" className="btn" disabled={isSubmitting || maxQuantity <= 0}>
					{isSubmitting ? 'Wird verbucht...' : 'Transaktion ausführen'}
				</button>
			</form>
		</Modal>
	);
};

// NOTE: This component is not directly used in the provided codebase, but was created based on the user's request.
// If it were to be integrated, you would import it in StoragePage.jsx and manage its state.
// For now, it remains a standalone, corrected component.
export default TransactionModal;