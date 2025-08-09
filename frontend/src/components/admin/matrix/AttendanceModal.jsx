import React, { useState } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';

const AttendanceModal = ({ isOpen, onClose, onSuccess, cellData }) => {
	const [attended, setAttended] = useState(cellData.attended);
	const [remarks, setRemarks] = useState(cellData.remarks);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			userId: cellData.userId,
			meetingId: cellData.meetingId,
			attended,
			remarks,
		};

		try {
			const result = await apiClient.put('/matrix/attendance', payload);
			if (result.success) {
				addToast('Teilnahme erfolgreich gespeichert.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal
			isOpen={isOpen}
			onClose={onClose}
			title={`Teilnahme bearbeiten: ${cellData.userName}`}
		>
			<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
				Meeting: {cellData.meetingName}
			</p>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
					<input
						type="checkbox"
						id="modal-attended"
						name="attended"
						checked={attended}
						onChange={(e) => setAttended(e.target.checked)}
						style={{ width: '1.5rem', height: '1.5rem' }}
					/>
					<label htmlFor="modal-attended" style={{ marginBottom: 0 }}>
						Hat teilgenommen
					</label>
				</div>
				<div className="form-group">
					<label htmlFor="modal-remarks">Anmerkungen (z.B. "entschuldigt gefehlt")</label>
					<textarea
						id="modal-remarks"
						name="remarks"
						value={remarks}
						onChange={(e) => setRemarks(e.target.value)}
						rows="3"
					></textarea>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AttendanceModal;