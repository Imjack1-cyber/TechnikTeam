import React, { useState, useEffect, useCallback } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import useApi from '../../../hooks/useApi';
import RelatedItemsManager from './RelatedItemsManager';

const StorageItemModal = ({ isOpen, onClose, onSuccess, item, initialMode = 'edit' }) => {
	const { data: allItems } = useApi(useCallback(() => apiClient.get('/storage'), []));
	const [mode, setMode] = useState(initialMode);
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		setMode(initialMode);
		if (!isOpen) return; // Prevent re-running on close

		if (initialMode === 'create') {
			setFormData({ name: '', location: '', quantity: 1, maxQuantity: 1, category: '' });
		} else if (item) {
			// Start with base item data
			const baseData = { ...item, category: item.category || '' };

			// Add mode-specific initial values
			if (initialMode === 'defect') {
				baseData.defective_quantity_change = 1;
				baseData.defect_reason_change = '';
				baseData.status = 'DEFECT'; // for the dropdown
			} else if (initialMode === 'repair') {
				baseData.repaired_quantity = 1;
				baseData.repair_notes = '';
			}
			setFormData(baseData);
		}
	}, [item, initialMode, isOpen]);


	const handleChange = (e) => {
		const { name, value } = e.target;
		const newFormData = { ...formData, [name]: value };

		// Enforce that quantity cannot exceed maxQuantity
		if (name === 'quantity' && newFormData.maxQuantity > 0 && parseInt(value) > newFormData.maxQuantity) {
			newFormData.quantity = newFormData.maxQuantity;
		}
		if (name === 'maxQuantity' && newFormData.maxQuantity > 0 && parseInt(value) < newFormData.quantity) {
			newFormData.quantity = value;
		}

		setFormData(newFormData);
	};


	const handleFileChange = (e) => {
		setFormData({ ...formData, imageFile: e.target.files[0] });
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const data = new FormData();
		// Ensure numeric values are sent correctly, even if empty
		const numericFields = ['quantity', 'maxQuantity', 'defectiveQuantity', 'weightKg', 'priceEur', 'currentHolderUserId', 'assignedEventId'];

		Object.keys(formData).forEach(key => {
			if (key !== 'imageFile') {
				let value = formData[key];
				// Handle potentially empty numeric fields by defaulting to 0
				if (numericFields.includes(key) && (value === '' || value === null || value === undefined)) {
					value = 0;
				}
				data.append(key, value);
			}
		});
		if (formData.imageFile) {
			data.append('imageFile', formData.imageFile);
		}

		try {
			const result = (mode === 'create')
				? await apiClient.post('/storage', data)
				: await apiClient.post(`/storage/${item.id}`, data);

			if (result.success) {
				addToast(`Artikel ${mode === 'create' ? 'erstellt' : 'aktualisiert'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const handleDefectSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			action: formData.status === 'UNREPAIRABLE' ? 'report_unrepairable' : 'report_defect',
			quantity: parseInt(formData.defective_quantity_change, 10),
			reason: formData.defect_reason_change
		};

		try {
			const result = await apiClient.put(`/storage/${item.id}`, payload);
			if (result.success) {
				addToast('Defekt-Status erfolgreich aktualisiert.', 'success');
				onSuccess();
			} else throw new Error(result.message);
		} catch (err) {
			setError(err.message || 'Fehler beim Melden des Defekts.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const handleRepairSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			action: 'repair',
			quantity: parseInt(formData.repaired_quantity, 10),
			notes: formData.repair_notes
		};

		try {
			const result = await apiClient.put(`/storage/${item.id}`, payload);
			if (result.success) {
				addToast('Reparatur erfolgreich verbucht.', 'success');
				onSuccess();
			} else throw new Error(result.message);
		} catch (err) {
			setError(err.message || 'Fehler beim Buchen der Reparatur.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const renderContent = () => {
		switch (mode) {
			case 'defect':
				return (
					<form onSubmit={handleDefectSubmit}>
						<div className="form-group">
							<label>Status</label>
							<select name="status" value={formData.status || 'DEFECT'} onChange={handleChange} className="form-group">
								<option value="DEFECT">Defekt melden</option>
								<option value="UNREPAIRABLE">Nicht reparierbar (wird ausgebucht)</option>
							</select>
						</div>
						<div className="form-group">
							<label>Anzahl zu meldender Artikel</label>
							<input type="number" name="defective_quantity_change" value={formData.defective_quantity_change || ''} min="1" max={formData.status === 'UNREPAIRABLE' ? item.defectiveQuantity : item.availableQuantity} onChange={handleChange} required />
						</div>
						<div className="form-group">
							<label>Grund</label>
							<textarea name="defect_reason_change" value={formData.defect_reason_change || ''} rows="3" onChange={handleChange}></textarea>
						</div>
						<button type="submit" className="btn" disabled={isSubmitting}>Speichern</button>
					</form>
				);
			case 'repair':
				return (
					<form onSubmit={handleRepairSubmit}>
						<div className="form-group">
							<label>Anzahl reparierter Artikel</label>
							<input type="number" name="repaired_quantity" value={formData.repaired_quantity || ''} min="1" max={item.defectiveQuantity} onChange={handleChange} required />
						</div>
						<div className="form-group">
							<label>Notiz (z.B. was wurde gemacht?)</label>
							<textarea name="repair_notes" value={formData.repair_notes || ''} rows="3" onChange={handleChange}></textarea>
						</div>
						<button type="submit" className="btn btn-success" disabled={isSubmitting}>Als repariert buchen</button>
					</form>
				);
			case 'relations':
				return (
					<RelatedItemsManager
						item={item}
						allItems={allItems || []}
						onSave={onSuccess}
						onCancel={onClose}
					/>
				);
			case 'create':
			case 'edit':
			default:
				return (
					<form onSubmit={handleSubmit}>
						<div className="form-group">
							<label>Name</label>
							<input type="text" name="name" value={formData.name || ''} onChange={handleChange} required />
						</div>
						<div className="form-group">
							<label>Kategorie (z.B. Audio, Licht, Kabel)</label>
							<input type="text" name="category" value={formData.category || ''} onChange={handleChange} />
						</div>
						<div className="form-group">
							<label>Ort</label>
							<input type="text" name="location" value={formData.location || ''} onChange={handleChange} />
						</div>
						<div className="responsive-dashboard-grid">
							<div className="form-group">
								<label>Menge im Bestand</label>
								<input type="number" name="quantity" value={formData.quantity || 0} onChange={handleChange} min="0" required />
							</div>
							<div className="form-group">
								<label>Max. Menge</label>
								<input type="number" name="maxQuantity" value={formData.maxQuantity || 0} onChange={handleChange} min="0" required />
							</div>
						</div>
						<div className="form-group">
							<label>Bild (optional)</label>
							<input type="file" name="imageFile" onChange={handleFileChange} accept="image/*" />
						</div>
						<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
							<button type="submit" className="btn" disabled={isSubmitting}>Speichern</button>
							{mode === 'edit' && (
								<button type="button" className="btn btn-secondary" onClick={() => setMode('relations')}>
									Zugehörige Artikel
								</button>
							)}
						</div>
					</form>
				);
		}
	};

	const getTitle = () => {
		switch (mode) {
			case 'create': return 'Neuen Artikel anlegen';
			case 'edit': return `Artikel bearbeiten: ${item?.name}`;
			case 'defect': return `Defekt-Status für "${item?.name}"`;
			case 'repair': return `Artikel "${item?.name}" repariert`;
			case 'relations': return `Zugehörige Artikel für "${item?.name}"`;
			default: return 'Lagerartikel';
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={getTitle()}>
			{error && <p className="error-message">{error}</p>}
			{renderContent()}
		</Modal>
	);
};

export default StorageItemModal;