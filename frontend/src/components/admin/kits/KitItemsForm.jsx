import React, { useState } from 'react';
import apiClient from '../../../services/apiClient';

const KitItemsForm = ({ kit, allStorageItems, onUpdateSuccess }) => {
	const [items, setItems] = useState(kit.items || []);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		newItems[index] = { ...newItems[index], [field]: value };
		setItems(newItems);
	};

	const handleAddItem = () => {
		setItems([...items, { itemId: '', quantity: 1 }]);
	};

	const handleRemoveItem = (index) => {
		const newItems = items.filter((_, i) => i !== index);
		setItems(newItems);
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const validItems = items.filter(item => item.itemId && item.quantity > 0)
			.map(item => ({ itemId: parseInt(item.itemId), quantity: parseInt(item.quantity) }));

		try {
			const result = await apiClient.put(`/kits/${kit.id}/items`, validItems);
			if (result.success) {
				onUpdateSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Inhalt konnte nicht gespeichert werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<form onSubmit={handleSubmit}>
			<h4>Inhalt bearbeiten</h4>
			{error && <p className="error-message">{error}</p>}
			<div id={`kit-items-container-${kit.id}`} className="kit-items-container">
				{items.length === 0 && <p className="no-items-message">Dieses Kit ist leer. Fügen Sie einen Artikel hinzu.</p>}
				{items.map((item, index) => (
					<div className="dynamic-row" key={index}>
						<select
							name="itemIds"
							className="form-group"
							value={item.itemId}
							onChange={(e) => handleItemChange(index, 'itemId', e.target.value)}
							required
						>
							<option value="">-- Artikel auswählen --</option>
							{allStorageItems.map(i => <option key={i.id} value={i.id}>{i.name}</option>)}
						</select>
						<input
							type="number"
							name="quantities"
							value={item.quantity}
							onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
							min="1"
							className="form-group"
							style={{ maxWidth: '100px' }}
							required
						/>
						<button type="button" className="btn btn-small btn-danger" onClick={() => handleRemoveItem(index)} title="Zeile entfernen">×</button>
					</div>
				))}
			</div>
			<div style={{ marginTop: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
				<button type="button" className="btn btn-small" onClick={handleAddItem}>
					<i className="fas fa-plus"></i> Zeile hinzufügen
				</button>
				<button type="submit" className="btn btn-success" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : <><i className="fas fa-save"></i> Kit-Inhalt speichern</>}
				</button>
			</div>
		</form>
	);
};

export default KitItemsForm;