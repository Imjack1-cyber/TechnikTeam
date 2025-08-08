import React, { useState, useCallback, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminChecklistTemplatesPage = () => {
	const templatesApiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);
	const storageItemsApiCall = useCallback(() => apiClient.get('/storage'), []);

	const { data: templates, loading, error, reload } = useApi(templatesApiCall);
	const { data: allStorageItems, loading: itemsLoading } = useApi(storageItemsApiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingTemplate, setEditingTemplate] = useState(null);
	const { addToast } = useToast();

	const openModal = (template = null) => {
		setEditingTemplate(template);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingTemplate(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (template) => {
		if (window.confirm(`Vorlage "${template.name}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/checklist-templates/${template.id}`);
				if (result.success) {
					addToast('Vorlage gelöscht', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler: ${err.message}`, 'error');
			}
		}
	};

	return (
		<div>
			<h1><i className="fas fa-tasks"></i> Pre-Flight Checklisten-Vorlagen</h1>
			<p>Verwalten Sie hier Vorlagen für wiederverwendbare Checklisten (z.B. für Standard-Setups).</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neue Vorlage erstellen
				</button>
			</div>

			{loading && <p>Lade Vorlagen...</p>}
			{error && <p className="error-message">{error}</p>}

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name</th>
							<th>Beschreibung</th>
							<th>Anzahl Items</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{templates?.map(template => (
							<tr key={template.id}>
								<td>{template.name}</td>
								<td>{template.description}</td>
								<td>{template.items?.length || 0}</td>
								<td>
									<button onClick={() => openModal(template)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleDelete(template)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			<div className="mobile-card-list">
				{templates?.map(template => (
					<div className="list-item-card" key={template.id}>
						<h3 className="card-title">{template.name}</h3>
						<div className="card-row"><strong>Items:</strong> <span>{template.items?.length || 0}</span></div>
						<p style={{ marginTop: '0.5rem' }}>{template.description}</p>
						<div className="card-actions">
							<button onClick={() => openModal(template)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleDelete(template)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>
			{isModalOpen && !itemsLoading && (
				<TemplateModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					template={editingTemplate}
					allStorageItems={allStorageItems || []}
				/>
			)}
		</div>
	);
};

const TemplateModal = ({ isOpen, onClose, onSuccess, template, allStorageItems }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [items, setItems] = useState([]);
	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
			const initialItems = template?.items?.map(i => ({
				itemText: i.itemText,
				storageItemId: i.storageItemId,
				quantity: i.quantity || 1
			})) || [{ itemText: '', storageItemId: null, quantity: 1 }];

			if (initialItems.length === 0) {
				setItems([{ itemText: '', storageItemId: null, quantity: 1 }]);
			} else {
				setItems(initialItems);
			}
		}
	}, [isOpen, template]);

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		const currentItem = { ...newItems[index], [field]: value };

		if (field === 'storageItemId') {
			const selectedStorageItem = allStorageItems.find(si => si.id === parseInt(value));
			if (selectedStorageItem && currentItem.quantity > selectedStorageItem.availableQuantity) {
				currentItem.quantity = selectedStorageItem.availableQuantity;
			}
		}
		newItems[index] = currentItem;
		setItems(newItems);
	};

	const handleAddTextItem = () => setItems([...items, { itemText: '', storageItemId: null, quantity: 1 }]);
	const handleAddStorageItem = () => setItems([...items, { itemText: null, storageItemId: '', quantity: 1 }]);
	const handleRemoveItem = (index) => setItems(items.filter((_, i) => i !== index));

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const formData = new FormData(e.target);

		const finalItems = items
			.filter(item => (item.itemText && item.itemText.trim() !== '') || (item.storageItemId))
			.map(item => ({
				itemText: item.storageItemId ? null : item.itemText,
				storageItemId: item.storageItemId ? parseInt(item.storageItemId, 10) : null,
				quantity: item.storageItemId ? parseInt(item.quantity, 10) : null
			}));

		const data = {
			name: formData.get('name'),
			description: formData.get('description'),
			items: finalItems
		};

		try {
			const result = template
				? await apiClient.put(`/admin/checklist-templates/${template.id}`, data)
				: await apiClient.post('/admin/checklist-templates', data);
			if (result.success) {
				addToast(`Vorlage erfolgreich ${template ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			}
			else throw new Error(result.message);
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={template ? 'Vorlage bearbeiten' : 'Neue Vorlage erstellen'}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Name der Vorlage</label>
					<input name="name" defaultValue={template?.name} required />
				</div>
				<div className="form-group">
					<label>Beschreibung</label>
					<textarea name="description" defaultValue={template?.description} rows="2"></textarea>
				</div>
				<div className="form-group">
					<label>Checklisten-Punkte</label>
					{items.map((item, index) => {
						const isStorageItem = item.storageItemId !== null;
						const selectedStorageItem = isStorageItem ? allStorageItems.find(si => si.id === parseInt(item.storageItemId)) : null;

						return (
							<div className="dynamic-row" key={index}>
								{isStorageItem ? (
									<>
										<select
											value={item.storageItemId}
											onChange={e => handleItemChange(index, 'storageItemId', e.target.value)}
											className="form-group"
										>
											<option value="">-- Lagerartikel auswählen --</option>
											{allStorageItems.map(si => <option key={si.id} value={si.id}>{si.name}</option>)}
										</select>
										<input
											type="number"
											value={item.quantity || 1}
											onChange={e => handleItemChange(index, 'quantity', e.target.value)}
											min="1"
											max={selectedStorageItem?.availableQuantity}
											title={`Verfügbar: ${selectedStorageItem?.availableQuantity || 'N/A'}`}
											className="form-group"
											style={{ maxWidth: '100px' }}
										/>
									</>
								) : (
									<input
										value={item.itemText || ''}
										onChange={e => handleItemChange(index, 'itemText', e.target.value)}
										placeholder={`Text-Punkt #${index + 1}`}
										className="form-group"
									/>
								)}
								<button type="button" onClick={() => handleRemoveItem(index)} className="btn btn-small btn-danger">×</button>
							</div>
						);
					})}
					<div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
						<button type="button" onClick={handleAddTextItem} className="btn btn-small btn-secondary">Textpunkt hinzufügen</button>
						<button type="button" onClick={handleAddStorageItem} className="btn btn-small btn-secondary">Lagerartikel hinzufügen</button>
					</div>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AdminChecklistTemplatesPage;