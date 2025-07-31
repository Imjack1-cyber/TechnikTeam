import React, { useState } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';

const KitModal = ({ isOpen, onClose, onSuccess, kit }) => {
	const isEditMode = !!kit;
	const [formData, setFormData] = useState({
		name: kit?.name || '',
		description: kit?.description || '',
		location: kit?.location || ''
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		try {
			const result = isEditMode
				? await apiClient.put(`/kits/${kit.id}`, formData)
				: await apiClient.post('/kits', formData);

			if (result.success) {
				addToast(`Kit erfolgreich ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
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

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Kit bearbeiten" : "Neues Kit anlegen"}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="name-modal">Name des Kits</label>
					<input type="text" id="name-modal" name="name" value={formData.name} onChange={handleChange} required />
				</div>
				<div className="form-group">
					<label htmlFor="description-modal">Beschreibung</label>
					<textarea id="description-modal" name="description" value={formData.description} onChange={handleChange} rows="3"></textarea>
				</div>
				<div className="form-group">
					<label htmlFor="location-modal">Physischer Standort des Kits</label>
					<input type="text" id="location-modal" name="location" value={formData.location} onChange={handleChange} placeholder="z.B. Lager, Schrank 3, Fach A" />
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default KitModal;