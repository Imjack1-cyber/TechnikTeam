import React, { useState, useEffect } from 'react';
import apiClient from '../../../services/apiClient';

/**
 * KitItemsForm
 *
 * - Only disable selects while storage items are still loading (allStorageItems === null).
 * - If storage finished loading but is empty ([]), the select stays enabled so the user can
 *   open it and see the "Keine Artikel verfügbar" placeholder. This avoids the greyed-out control.
 * - Keep local itemId state as string so it binds cleanly to <select>.
 */

const KitItemsForm = ({ kit, allStorageItems, onUpdateSuccess }) => {
	const [items, setItems] = useState([]);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	// Initialize items from kit.items whenever kit.items changes.
	useEffect(() => {
		const initialItems = (kit.items && kit.items.length > 0)
			? kit.items.map(it => ({ itemId: String(it.itemId), quantity: it.quantity })) // string for select
			: [{ itemId: '', quantity: 1 }];
		setItems(initialItems);
	}, [kit.items]);

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		newItems[index] = { ...newItems[index], [field]: value };
		setItems(newItems);
	};

	const handleAddItem = () => {
		setItems(prev => [...prev, { itemId: '', quantity: 1 }]);
	};

	const handleRemoveItem = (index) => {
		setItems(prev => prev.filter((_, i) => i !== index));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		// Validate and convert to numbers
		const validItems = items
			.filter(item => item.itemId && parseInt(item.quantity, 10) > 0)
			.map(item => ({ itemId: parseInt(item.itemId, 10), quantity: parseInt(item.quantity, 10) }));

		try {
			const result = await apiClient.put(`/kits/${kit.id}/items`, validItems);
			if (result.success) {
				onUpdateSuccess();
			} else {
				throw new Error(result.message || 'Unbekannter Fehler');
			}
		} catch (err) {
			setError(err.message || 'Inhalt konnte nicht gespeichert werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	// Determine select disabled state and placeholder text
	const storageLoading = allStorageItems === null;
	const hasOptions = Array.isArray(allStorageItems) && allStorageItems.length > 0;

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
							// ONLY disable while storage is still loading — allow interaction when loaded-empty
							disabled={storageLoading}
						>
							{storageLoading ? (
								<option value="">...Lade Artikel</option>
							) : (
								<>
									<option value="">-- Artikel auswählen --</option>
									{!hasOptions && <option value="" disabled>(Keine Artikel verfügbar)</option>}
									{(allStorageItems || []).map(i => (
										<option key={i.id} value={i.id}>{i.name}</option>
									))}
								</>
							)}
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
				{/* allow adding new empty rows while storage is loaded or loading; disable only while loading to avoid confusion */}
				<button type="button" className="btn btn-small" onClick={handleAddItem} disabled={storageLoading}>
					<i className="fas fa-plus"></i> Zeile hinzufügen
				</button>
				<button type="submit" className="btn btn-success" disabled={isSubmitting || storageLoading}>
					{isSubmitting ? 'Speichern...' : <><i className="fas fa-save"></i> Kit-Inhalt speichern</>}
				</button>
			</div>
		</form>
	);
};

export default KitItemsForm;
