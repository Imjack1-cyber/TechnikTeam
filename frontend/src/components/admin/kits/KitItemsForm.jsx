import React, { useState, useEffect } from 'react';
import apiClient from '../../../services/apiClient';

const KitItemsForm = ({ kit, allStorageItems, onUpdateSuccess }) => {
	const [items, setItems] = useState([]);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	useEffect(() => {
		const initialItems = (kit.items && kit.items.length > 0)
			? kit.items.map(it => ({ itemId: String(it.itemId), quantity: it.quantity }))
			: [{ itemId: '', quantity: 1 }];
		setItems(initialItems);
	}, [kit.items]);

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		const currentItem = { ...newItems[index], [field]: value };

		if (field === 'itemId') {
			const selectedStorageItem = allStorageItems.find(si => si.id === parseInt(value));
			if (selectedStorageItem && currentItem.quantity > selectedStorageItem.maxQuantity) {
				currentItem.quantity = selectedStorageItem.maxQuantity;
			}
		}
		newItems[index] = currentItem;
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
				{items.map((item, index) => {
					const selectedStorageItem = allStorageItems.find(si => si.id === parseInt(item.itemId));
					return (
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
								max={selectedStorageItem?.maxQuantity}
								title={`Maximal im Bestand: ${selectedStorageItem?.maxQuantity || 'N/A'}`}
								className="form-group"
								style={{ maxWidth: '100px' }}
								required
							/>
							<button type="button" className="btn btn-small btn-danger" onClick={() => handleRemoveItem(index)} title="Zeile entfernen">×</button>
						</div>
					);
				})}
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