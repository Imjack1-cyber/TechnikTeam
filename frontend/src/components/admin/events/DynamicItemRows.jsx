import React from 'react';

const DynamicItemRows = ({ rows, setRows, storageItems }) => {
	const handleRowChange = (index, field, value) => {
		const newRows = [...rows];
		newRows[index][field] = value;
		setRows(newRows);
	};

	const handleAddRow = () => {
		setRows([...rows, { itemId: '', quantity: 1 }]);
	};

	const handleRemoveRow = (index) => {
		setRows(rows.filter((_, i) => i !== index));
	};

	return (
		<div>
			{rows.map((row, index) => {
				const selectedItem = storageItems.find(item => item.id === parseInt(row.itemId));
				const maxQuantity = selectedItem ? selectedItem.availableQuantity : undefined;
				return (
					<div className="dynamic-row" key={index}>
						<select
							name="itemIds"
							value={row.itemId}
							onChange={(e) => handleRowChange(index, 'itemId', e.target.value)}
							className="form-group"
						>
							<option value="">-- Artikel auswählen --</option>
							{storageItems.map(item => <option key={item.id} value={item.id}>{item.name} ({item.availableQuantity} verfügbar)</option>)}
						</select>
						<input
							type="number"
							name="quantities"
							value={row.quantity}
							onChange={(e) => handleRowChange(index, 'quantity', e.target.value)}
							className="form-group"
							style={{ maxWidth: '100px' }}
							min="1"
							max={maxQuantity}
							title={maxQuantity !== undefined ? `Maximal verfügbar: ${maxQuantity}` : ''}
						/>
						<button type="button" className="btn btn-small btn-danger" onClick={() => handleRemoveRow(index)} title="Zeile entfernen">×</button>
					</div>
				);
			})}
			<button type="button" className="btn btn-small" onClick={handleAddRow}>
				<i className="fas fa-plus"></i> Artikel hinzufügen
			</button>
		</div>
	);
};

export default DynamicItemRows;