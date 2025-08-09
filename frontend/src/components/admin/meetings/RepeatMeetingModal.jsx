import React, { useState } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';

const RepeatMeetingModal = ({ isOpen, onClose, onSuccess, meeting }) => {
	const [datetime, setDatetime] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/meetings/${meeting.id}/repeat`, {
				meetingDateTime: datetime,
			});
			if (result.success) {
				addToast('Meeting erfolgreich wiederholt.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Erstellen des Wiederholungs-Meetings fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`"${meeting.name}" wiederholen`}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<p>Geben Sie das neue Datum und die Uhrzeit für dieses wiederholte Meeting an.</p>
				<div className="form-group">
					<label htmlFor="repeat-datetime">Neues Datum & Uhrzeit</label>
					<input
						type="datetime-local"
						id="repeat-datetime"
						value={datetime}
						onChange={(e) => setDatetime(e.target.value)}
						required
					/>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird erstellt...' : 'Wiederholung erstellen'}
				</button>
			</form>
		</Modal>
	);
};

export default RepeatMeetingModal;