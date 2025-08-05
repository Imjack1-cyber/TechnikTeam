import React, { useState, useEffect, useCallback } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import useApi from '../../../hooks/useApi';

const RelatedItemsManager = ({ item, allItems, onSave, onCancel }) => {
	const relationsApiCall = useCallback(() => apiClient.get(`/admin/storage/${item.id}/relations`), [item.id]);
	const { data: relatedItems, loading, error, reload } = useApi(relationsApiCall);
	const [selectedIds, setSelectedIds] = useState(new Set());
	const [isSubmitting, setIsSubmitting] = useState(false);

	useEffect(() => {
		if (relatedItems) {
			setSelectedIds(new Set(relatedItems.map(i => i.id)));
		}
	}, [relatedItems]);

	const handleToggle = (itemId) => {
		setSelectedIds(prev => {
			const newSet = new Set(prev);
			if (newSet.has(itemId)) {
				newSet.delete(itemId);
			} else {
				newSet.add(itemId);
			}
			return newSet;
		});
	};

	const handleSave = async () => {
		setIsSubmitting(true);
		try {
			await apiClient.put(`/admin/storage/${item.id}/relations`, { relatedItemIds: Array.from(selectedIds) });
			onSave();
		} catch (err) {
			console.error("Failed to save related items", err);
		} finally {
			setIsSubmitting(false);
		}
	};

	const availableItems = allItems.filter(i => i.id !== item.id);

	return (
		<div>
			{loading && <p>Lade Beziehungen...</p>}
			{error && <p className="error-message">{error}</p>}
			<div className="form-group" style={{ maxHeight: '40vh', overflowY: 'auto', border: '1px solid var(--border-color)', borderRadius: 'var(--border-radius)', padding: '0.5rem' }}>
				{availableItems.map(i => (
					<label key={i.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
						<input type="checkbox" checked={selectedIds.has(i.id)} onChange={() => handleToggle(i.id)} />
						{i.name}
					</label>
				))}
			</div>
			<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1rem' }}>
				<button type="button" className="btn btn-secondary" onClick={onCancel}>Abbrechen</button>
				<button type="button" className="btn btn-success" onClick={handleSave} disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Beziehungen speichern'}
				</button>
			</div>
		</div>
	);
};


const StorageItemModal = ({ isOpen, onClose, onSuccess, item, initialMode = 'edit' }) => {
	const { data: allItems } = useApi(useCallback(() => apiClient.get('/storage'), []));
	const [mode, setMode] = useState(initialMode);
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		setMode(initialMode);
		if (initialMode === 'create') {
			setFormData({ name: '', location: '', quantity: 1, maxQuantity: 1, category: '' });
		} else if (item) {
			setFormData({ ...item, category: item.category || '' });
		}
	}, [item, initialMode, isOpen]);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleFileChange = (e) => {
		setFormData({ ...formData, imageFile: e.target.files[0] });
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const data = new FormData();
		Object.keys(formData).forEach(key => {
			if (key !== 'imageFile') {
				data.append(key, formData[key] === null ? '' : formData[key]);
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
							<select name="status" onChange={handleChange} defaultValue="DEFECT" className="form-group">
								<option value="DEFECT">Defekt melden</option>
								<option value="UNREPAIRABLE">Nicht reparierbar (wird ausgebucht)</option>
							</select>
						</div>
						<div className="form-group">
							<label>Anzahl defekter Artikel</label>
							<input type="number" name="defective_quantity_change" defaultValue="1" min="1" max={item.availableQuantity} onChange={handleChange} required />
						</div>
						<div className="form-group">
							<label>Grund</label>
							<textarea name="defect_reason_change" rows="3" onChange={handleChange}></textarea>
						</div>
						<button type="submit" className="btn" disabled={isSubmitting}>Speichern</button>
					</form>
				);
			case 'repair':
				return (
					<form onSubmit={handleRepairSubmit}>
						<div className="form-group">
							<label>Anzahl reparierter Artikel</label>
							<input type="number" name="repaired_quantity" defaultValue="1" min="1" max={item.defectiveQuantity} onChange={handleChange} required />
						</div>
						<div className="form-group">
							<label>Notiz (z.B. was wurde gemacht?)</label>
							<textarea name="repair_notes" rows="3" onChange={handleChange}></textarea>
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