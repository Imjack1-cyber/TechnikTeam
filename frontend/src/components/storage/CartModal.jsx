import React from 'react';
import Modal from '../ui/Modal';

const CartModal = ({ isOpen, onClose, cart, onUpdateQuantity, onRemove, onSwitchType, onSubmit, activeEvents, transactionError, isSubmitting }) => {

	const renderCartSection = (title, items, type) => {
		if (items.length === 0) return null;
		return (
			<div style={{ marginBottom: '1.5rem' }}>
				<h4>{title}</h4>
				{items.map(item => {
					// Use original item properties (quantity, maxQuantity) for calculation
					const maxQuantity = type === 'checkout'
						? item.availableQuantity
						: (item.maxQuantity > 0 ? item.maxQuantity - item.quantity : Infinity);

					return (
						<div className="dynamic-row" key={`${item.id}-${type}`}>
							<button
								type="button"
								onClick={() => onSwitchType(item.id, type)}
								className={`btn btn-small ${type === 'checkout' ? 'btn-danger-outline' : 'btn-success'}`}
								title={`Zu '${type === 'checkout' ? 'Einräumen' : 'Entnehmen'}' wechseln`}
								style={{ minWidth: '40px' }}
							>
								<i className={`fas ${type === 'checkout' ? 'fa-arrow-down' : 'fa-arrow-up'}`}></i>
							</button>
							<span style={{ flexGrow: 1 }}>{item.name}</span>
							<input
								type="number"
								value={item.cartQuantity} // Use the specific cart quantity state
								onChange={e => onUpdateQuantity(item.id, type, parseInt(e.target.value, 10))}
								min="1"
								max={maxQuantity === Infinity ? undefined : maxQuantity}
								title={maxQuantity !== Infinity ? `Maximal: ${maxQuantity}` : ''}
								className="form-group"
								style={{ maxWidth: '80px' }}
							/>
							<button type="button" className="btn btn-small btn-danger" onClick={() => onRemove(item.id, type)}>×</button>
						</div>
					);
				})}
			</div>
		);
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Warenkorb (${cart.length} Artikel)`}>
			<form onSubmit={onSubmit}>
				{transactionError && <p className="error-message">{transactionError}</p>}

				{renderCartSection('Zu Entnehmen', cart.filter(i => i.type === 'checkout'), 'checkout')}
				{renderCartSection('Einzuräumen', cart.filter(i => i.type === 'checkin'), 'checkin')}

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

export default CartModal;