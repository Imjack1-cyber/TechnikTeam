import React, { useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import Modal from '../components/ui/Modal';

const RequestTrainingModal = ({ isOpen, onClose, onSuccess }) => {
	const [topic, setTopic] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post('/public/training-requests', { topic });
			if (result.success) {
				addToast('Lehrgangswunsch erfolgreich eingereicht!', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Einreichen fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neuen Lehrgang anfragen">
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<p>Welches Thema oder welche Fähigkeit würdest du gerne lernen?</p>
				<div className="form-group">
					<label htmlFor="topic">Thema des Lehrgangs</label>
					<input
						type="text"
						id="topic"
						value={topic}
						onChange={(e) => setTopic(e.target.value)}
						placeholder="z.B. Fortgeschrittene Videomischung"
						required
					/>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird eingereicht...' : 'Wunsch einreichen'}
				</button>
			</form>
		</Modal>
	);
};


const LehrgaengePage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/meetings'), []);
	const { data: meetings, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
	const [isModalOpen, setIsModalOpen] = useState(false);


	const handleAction = async (meetingId, action) => {
		const endpoint = `/public/meetings/${meetingId}/${action}`;
		const successMessage = action === 'signup' ? 'Erfolgreich angemeldet!' : 'Erfolgreich abgemeldet!';
		const failureMessage = action === 'signup' ? 'Anmeldung fehlgeschlagen.' : 'Abmeldung fehlgeschlagen.';

		try {
			const result = await apiClient.post(endpoint, {});
			if (result.success) {
				addToast(successMessage, 'success');
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message || failureMessage, 'error');
		}
	};

	const renderActionButtons = (meeting) => {
		if (meeting.userAttendanceStatus === 'ANGEMELDET') {
			return (
				<button
					onClick={() => handleAction(meeting.id, 'signoff')}
					className="btn btn-small btn-danger"
				>
					Abmelden
				</button>
			);
		}
		return (
			<button
				onClick={() => handleAction(meeting.id, 'signup')}
				className="btn btn-small btn-success"
			>
				Anmelden
			</button>
		);
	};

	const renderUserStatus = (status) => {
		if (status === 'ANGEMELDET') {
			return <strong className="text-success">Angemeldet</strong>;
		}
		if (status === 'ABGEMELDET') {
			return <span className="text-danger">Abgemeldet</span>;
		}
		return <span className="text-muted">Offen</span>;
	};

	if (loading) return <div>Lade Lehrgänge...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div>
			<h1><i className="fas fa-graduation-cap"></i> Anstehende Lehrgänge & Meetings</h1>
			<div className="table-controls">
				<button onClick={() => setIsModalOpen(true)} className="btn btn-secondary">
					<i className="fas fa-question-circle"></i> Neuen Lehrgang anfragen
				</button>
			</div>


			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Meeting</th>
							<th>Gehört zu Kurs</th>
							<th>Datum & Uhrzeit</th>
							<th>Dein Status</th>
							<th>Aktion</th>
						</tr>
					</thead>
					<tbody>
						{!meetings || meetings.length === 0 ? (
							<tr><td colSpan="5" style={{ textAlign: 'center' }}>Derzeit stehen keine Lehrgänge oder Meetings an.</td></tr>
						) : (
							meetings.map(meeting => (
								<tr key={meeting.id}>
									<td><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></td>
									<td>{meeting.parentCourseName}</td>
									<td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
									<td>{renderUserStatus(meeting.userAttendanceStatus)}</td>
									<td>{renderActionButtons(meeting)}</td>
								</tr>
							))
						)}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{!meetings || meetings.length === 0 ? (
					<div className="card"><p>Derzeit stehen keine Lehrgänge oder Meetings an.</p></div>
				) : (
					meetings.map(meeting => (
						<div className="list-item-card" key={meeting.id}>
							<h3 className="card-title"><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></h3>
							<div className="card-row"><strong>Kurs:</strong> <span>{meeting.parentCourseName}</span></div>
							<div className="card-row"><strong>Wann:</strong> <span>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</span></div>
							<div className="card-row"><strong>Status:</strong> <span>{renderUserStatus(meeting.userAttendanceStatus)}</span></div>
							<div className="card-actions">{renderActionButtons(meeting)}</div>
						</div>
					))
				)}
			</div>
			{isModalOpen && (
				<RequestTrainingModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => setIsModalOpen(false)} />
			)}
		</div>
	);
};

export default LehrgaengePage;