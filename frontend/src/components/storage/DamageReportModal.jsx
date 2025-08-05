import React, { useState } from 'react';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';

const DamageReportModal = ({ isOpen, onClose, onSuccess, item }) => {
	const [description, setDescription] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		try {
			const result = await apiClient.post(`/public/storage/${item.id}/report-damage`, { description });
			if (result.success) {
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Meldung konnte nicht gesendet werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const handleClose = () => {
		setDescription('');
		setError('');
		setIsSubmitting(false);
		onClose();
	};

	return (
		<Modal isOpen={isOpen} onClose={handleClose} title={`Schaden für "${item?.name}" melden`}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<p>Bitte beschreiben Sie den Defekt so genau wie möglich. Ein Administrator wird die Meldung prüfen.</p>
				<div className="form-group">
					<label htmlFor="damage-description">Beschreibung des Schadens</label>
					<textarea
						id="damage-description"
						name="description"
						value={description}
						onChange={(e) => setDescription(e.target.value)}
						rows="5"
						required
					></textarea>
				</div>
				<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
					<button type="button" className="btn btn-secondary" onClick={handleClose} disabled={isSubmitting}>
						Abbrechen
					</button>
					<button type="submit" className="btn btn-danger" disabled={isSubmitting}>
						{isSubmitting ? 'Wird gesendet...' : 'Schaden melden'}
					</button>
				</div>
			</form>
		</Modal>
	);
};

export default DamageReportModal;