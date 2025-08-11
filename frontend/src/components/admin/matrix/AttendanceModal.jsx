import React, { useState, useEffect } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';

const AttendanceModal = ({ isOpen, onClose, onSuccess, cellData }) => {
	// State for meeting attendance part
	const [attended, setAttended] = useState(cellData.attended);
	const [meetingRemarks, setMeetingRemarks] = useState(cellData.remarks);
	const [isSubmittingMeeting, setIsSubmittingMeeting] = useState(false);
	const [meetingError, setMeetingError] = useState('');

	// State for overall course qualification part
	const [qualStatus, setQualStatus] = useState(cellData.qualification?.status || 'BESUCHT');
	const [qualDate, setQualDate] = useState(cellData.qualification?.completionDate || new Date().toISOString().split('T')[0]);
	const [qualRemarks, setQualRemarks] = useState(cellData.qualification?.remarks || '');
	const [isSubmittingQual, setIsSubmittingQual] = useState(false);
	const [qualError, setQualError] = useState('');

	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
			setAttended(cellData.attended);
			setMeetingRemarks(cellData.remarks);
			setQualStatus(cellData.qualification?.status || 'BESUCHT');
			setQualDate(cellData.qualification?.completionDate || new Date().toISOString().split('T')[0]);
			setQualRemarks(cellData.qualification?.remarks || '');
			setMeetingError('');
			setQualError('');
		}
	}, [isOpen, cellData]);


	const handleMeetingSubmit = async (e) => {
		e.preventDefault();
		setIsSubmittingMeeting(true);
		setMeetingError('');

		const payload = {
			userId: cellData.userId,
			meetingId: cellData.meetingId,
			attended,
			remarks: meetingRemarks,
		};

		try {
			const result = await apiClient.put('/matrix/attendance', payload);
			if (result.success) {
				addToast('Meeting-Teilnahme erfolgreich gespeichert.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setMeetingError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmittingMeeting(false);
		}
	};

	const handleQualificationSubmit = async (e) => {
		e.preventDefault();
		setIsSubmittingQual(true);
		setQualError('');

		const payload = {
			userId: cellData.userId,
			courseId: cellData.courseId,
			status: qualStatus,
			completionDate: qualDate,
			remarks: qualRemarks,
		};

		try {
			const result = await apiClient.put('/matrix/qualification', payload);
			if (result.success) {
				addToast('Qualifikations-Status erfolgreich gespeichert.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setQualError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmittingQual(false);
		}
	};

	return (
		<Modal
			isOpen={isOpen}
			onClose={onClose}
			title={`Eintrag bearbeiten für: ${cellData.userName}`}
		>
			<div className="card">
				<h4 className="card-title">Teilnahme am Meeting: "{cellData.meetingName}"</h4>
				<form onSubmit={handleMeetingSubmit}>
					{meetingError && <p className="error-message">{meetingError}</p>}
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
							Hat am Meeting teilgenommen
						</label>
					</div>
					<div className="form-group">
						<label htmlFor="modal-remarks">Anmerkungen zum Meeting</label>
						<textarea
							id="modal-remarks"
							name="remarks"
							value={meetingRemarks}
							onChange={(e) => setMeetingRemarks(e.target.value)}
							rows="2"
						></textarea>
					</div>
					<button type="submit" className="btn" disabled={isSubmittingMeeting}>
						{isSubmittingMeeting ? 'Speichern...' : 'Meeting-Teilnahme speichern'}
					</button>
				</form>
			</div>

			<div className="card" style={{ marginTop: '1.5rem' }}>
				<h4 className="card-title">Gesamt-Qualifikation für Kurs: "{cellData.courseName}"</h4>
				<form onSubmit={handleQualificationSubmit}>
					{qualError && <p className="error-message">{qualError}</p>}
					<div className="form-group">
						<label htmlFor="qual-status">Status</label>
						<select id="qual-status" value={qualStatus} onChange={e => setQualStatus(e.target.value)}>
							<option value="BESUCHT">Besucht</option>
							<option value="ABSOLVIERT">Absolviert</option>
							<option value="BESTANDEN">Bestanden (Qualifiziert)</option>
							<option value="NICHT BESUCHT">Nicht Besucht (Eintrag entfernen)</option>
						</select>
					</div>
					<div className="form-group">
						<label htmlFor="qual-date">Abschlussdatum</label>
						<input type="date" id="qual-date" value={qualDate} onChange={e => setQualDate(e.target.value)} />
					</div>
					<div className="form-group">
						<label htmlFor="qual-remarks">Anmerkungen zur Qualifikation</label>
						<textarea
							id="qual-remarks"
							value={qualRemarks}
							onChange={(e) => setQualRemarks(e.target.value)}
							rows="2"
						></textarea>
					</div>
					<button type="submit" className="btn btn-success" disabled={isSubmittingQual}>
						{isSubmittingQual ? 'Speichern...' : 'Qualifikation speichern'}
					</button>
				</form>
			</div>
		</Modal>
	);
};

export default AttendanceModal;