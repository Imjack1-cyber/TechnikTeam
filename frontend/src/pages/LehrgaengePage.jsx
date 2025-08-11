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

const CourseAccordion = ({ course, onAction }) => {
	const [isOpen, setIsOpen] = useState(false);

	const getCourseStatusBadge = (status) => {
		switch (status) {
			case 'QUALIFIZIERT': return <span className="status-badge status-ok">Qualifiziert</span>;
			case 'IN_BEARBEITUNG': return <span className="status-badge status-warn">In Bearbeitung</span>;
			case 'VERFÜGBAR': return <span className="status-badge status-info">Verfügbar</span>;
			default: return null;
		}
	};

	const getMeetingStatusBadge = (meeting) => {
		let text = meeting.userAttendanceStatus;
		if (meeting.isWaitlisted) text = 'Warteliste';
		return <span className={`status-badge ${meeting.userAttendanceStatus === 'ANGEMELDET' ? 'status-ok' : 'status-info'}`}>{text}</span>;
	};

	return (
		<div className="card" style={{ marginBottom: '1rem' }}>
			<div onClick={() => setIsOpen(!isOpen)} style={{ cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
				<div>
					<h2 className="card-title" style={{ border: 'none', padding: 0, margin: 0, display: 'inline-block' }}>
						{course.name} ({course.abbreviation})
					</h2>
					<div style={{ marginLeft: '1rem', display: 'inline-block' }}>
						{getCourseStatusBadge(course.userCourseStatus)}
					</div>
				</div>
				<i className={`fas fa-chevron-down`} style={{ transition: 'transform 0.2s', transform: isOpen ? 'rotate(180deg)' : 'rotate(0deg)' }}></i>
			</div>

			{isOpen && (
				<div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid var(--border-color)' }}>
					<p>{course.description}</p>
					<h4 style={{ marginTop: '1.5rem' }}>Anstehende Termine:</h4>
					{course.upcomingMeetings.length === 0 ? (
						<p>Für diesen Lehrgang sind derzeit keine Termine geplant.</p>
					) : (
						<div className="table-wrapper">
							<table className="data-table">
								<thead><tr><th>Name</th><th>Datum</th><th>Teilnehmer</th><th>Status</th><th>Aktion</th></tr></thead>
								<tbody>
									{course.upcomingMeetings.map(meeting => {
										const isFull = meeting.maxParticipants && meeting.participantCount >= meeting.maxParticipants;
										const isPastDeadline = meeting.signupDeadline && new Date(meeting.signupDeadline) < new Date();
										return (
											<tr key={meeting.id}>
												<td><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></td>
												<td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
												<td>{meeting.participantCount} / {meeting.maxParticipants || '∞'}</td>
												<td>{getMeetingStatusBadge(meeting)}</td>
												<td>
													{meeting.userAttendanceStatus === 'ANGEMELDET' ?
														<button onClick={() => onAction(meeting.id, 'signoff')} className="btn btn-small btn-danger" disabled={isPastDeadline}>Abmelden</button> :
														<button onClick={() => onAction(meeting.id, 'signup')} className="btn btn-small btn-success" disabled={isPastDeadline}>
															{isFull ? 'Warteliste' : 'Anmelden'}
														</button>
													}
												</td>
											</tr>
										);
									})}
								</tbody>
							</table>
						</div>
					)}
				</div>
			)}
		</div>
	);
};

const LehrgaengePage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/meetings'), []);
	const { data: courses, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
	const [isModalOpen, setIsModalOpen] = useState(false);

	const handleAction = async (meetingId, action) => {
		const endpoint = `/public/meetings/${meetingId}/${action}`;

		if (action === 'signup') {
			const meeting = courses.flatMap(c => c.upcomingMeetings).find(m => m.id === meetingId);
			if (meeting?.parentMeetingId > 0 && courses.find(c => c.id === meeting.courseId)?.userCourseStatus === 'QUALIFIZIERT') {
				if (!window.confirm("Du bist bereits für diesen Lehrgang qualifiziert. Möchtest du dich auf die Warteliste für diesen Wiederholungstermin setzen lassen?")) {
					return;
				}
			}
		}

		try {
			const result = await apiClient.post(endpoint, {});
			if (result.success) {
				addToast(result.message, 'success');
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message || 'Aktion fehlgeschlagen.', 'error');
		}
	};

	if (loading) return <div>Lade Lehrgänge...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div>
			<h1><i className="fas fa-graduation-cap"></i> Lehrgangs-Hub</h1>
			<div className="table-controls">
				<p>Hier finden Sie eine Übersicht aller Lehrgänge. Klappen Sie einen Lehrgang auf, um die anstehenden Termine zu sehen und sich anzumelden.</p>
				<button onClick={() => setIsModalOpen(true)} className="btn btn-secondary">
					<i className="fas fa-question-circle"></i> Neuen Lehrgang anfragen
				</button>
			</div>

			<div>
				{courses?.map(course => (
					<CourseAccordion key={course.id} course={course} onAction={handleAction} />
				))}
			</div>

			{isModalOpen && (
				<RequestTrainingModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => setIsModalOpen(false)} />
			)}
		</div>
	);
};

export default LehrgaengePage;