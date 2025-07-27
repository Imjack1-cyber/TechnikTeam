import React, { useState, useEffect } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';

const StorageItemModal = ({ isOpen, onClose, onSuccess, item, initialMode = 'edit' }) => {
	const [mode, setMode] = useState(initialMode);
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	useEffect(() => {
		setMode(initialMode);
		// Reset form data when the item or mode changes
		if (initialMode === 'create') {
			setFormData({ name: '', location: '', quantity: 1, maxQuantity: 1 });
		} else if (item) {
			setFormData({ ...item });
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
				: await apiClient.post(`/storage/${item.id}`, data); // POST for multipart form data

			if (result.success) {
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
			if (result.success) onSuccess();
			else throw new Error(result.message);
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
			if (result.success) onSuccess();
			else throw new Error(result.message);
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
						<button type="submit" className="btn" disabled={isSubmitting}>Speichern</button>
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