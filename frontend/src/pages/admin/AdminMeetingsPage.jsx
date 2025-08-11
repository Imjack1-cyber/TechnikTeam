import React, { useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import RepeatMeetingModal from '../../components/admin/meetings/RepeatMeetingModal';

const ParticipantsModal = ({ isOpen, onClose, meeting, onPromote }) => {
	const participantsApiCall = useCallback(() => apiClient.get(`/admin/meetings/${meeting.id}/participants`), [meeting.id]);
	const waitlistApiCall = useCallback(() => apiClient.get(`/admin/meetings/${meeting.id}/waitlist`), [meeting.id]);
	const { data: participantsData, loading: pLoading, error: pError } = useApi(participantsApiCall);
	const { data: waitlistData, loading: wLoading, error: wError, reload: reloadWaitlist } = useApi(waitlistApiCall);

	const [isPromoting, setIsPromoting] = useState(false);

	const handlePromote = async (userId) => {
		setIsPromoting(true);
		await onPromote(userId);
		reloadWaitlist();
		setIsPromoting(false);
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Teilnehmer & Warteliste: ${meeting.name}`}>
			<div className="responsive-dashboard-grid">
				<div>
					<h4>Teilnehmer ({participantsData?.length || 0} / {meeting.maxParticipants || '∞'})</h4>
					{pLoading && <p>Lade Teilnehmer...</p>}
					{pError && <p className="error-message">{pError}</p>}
					<ul className="details-list">
						{participantsData?.map(user => <li key={user.id}>{user.username}</li>)}
					</ul>
				</div>
				<div>
					<h4>Warteliste ({waitlistData?.waitlist?.length || 0})</h4>
					{wLoading && <p>Lade Warteliste...</p>}
					{wError && <p className="error-message">{wError}</p>}
					<ul className="details-list">
						{waitlistData?.waitlist?.map(user => (
							<li key={user.id}>
								<span>{user.username}</span>
								<button onClick={() => handlePromote(user.id)} className="btn btn-small btn-success" disabled={isPromoting}>
									Befördern
								</button>
							</li>
						))}
					</ul>
				</div>
			</div>
		</Modal>
	);
};

const AdminMeetingsPage = () => {
	const { courseId } = useParams();
	const meetingsApiCall = useCallback(() => apiClient.get(`/meetings?courseId=${courseId}`), [courseId]);
	const usersApiCall = useCallback(() => apiClient.get('/users'), []);

	const { data: meetingsData, loading, error, reload } = useApi(meetingsApiCall);
	const { data: allUsers } = useApi(usersApiCall);
	const { addToast } = useToast();

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingMeeting, setEditingMeeting] = useState(null);
	const [formError, setFormError] = useState('');
	const [isRepeatModalOpen, setIsRepeatModalOpen] = useState(false);
	const [repeatingMeeting, setRepeatingMeeting] = useState(null);
	const [viewingParticipantsMeeting, setViewingParticipantsMeeting] = useState(null);

	const courseName = meetingsData?.[0]?.parentCourseName || 'Lehrgang';

	const handleOpenNewModal = () => {
		setEditingMeeting(null);
		setIsModalOpen(true);
	};

	const handleOpenEditModal = (meeting) => {
		setEditingMeeting(meeting);
		setIsModalOpen(true);
	};

	const handleCloseModal = () => {
		setIsModalOpen(false);
		setEditingMeeting(null);
		setFormError('');
	};

	const handleOpenRepeatModal = (meeting) => {
		setRepeatingMeeting(meeting);
		setIsRepeatModalOpen(true);
	};

	const handleCloseRepeatModal = () => {
		setIsRepeatModalOpen(false);
		setRepeatingMeeting(null);
	};

	const handlePromoteUser = async (userId) => {
		try {
			const result = await apiClient.post(`/admin/meetings/${viewingParticipantsMeeting.id}/promote`, { userId });
			if (result.success) {
				addToast('Benutzer erfolgreich zur Teilnahme hinzugefügt.', 'success');
				// Let the modal reload its own data
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message, 'error');
		}
	};


	const handleSubmit = async (e) => {
		e.preventDefault();
		const formData = new FormData(e.target);
		const data = Object.fromEntries(formData.entries());

		const payload = {
			...data,
			courseId: parseInt(courseId),
			leaderUserId: data.leaderUserId ? parseInt(data.leaderUserId) : null,
			maxParticipants: data.maxParticipants ? parseInt(data.maxParticipants) : null,
			signupDeadline: data.signupDeadline || null
		};

		try {
			const result = editingMeeting
				? await apiClient.put(`/meetings/${editingMeeting.id}`, payload)
				: await apiClient.post('/meetings', payload);

			if (result.success) {
				addToast(`Meeting erfolgreich ${editingMeeting ? 'aktualisiert' : 'geplant'}.`, 'success');
				handleCloseModal();
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setFormError(err.message || 'Ein Fehler ist aufgetreten.');
		}
	};

	const handleDelete = async (meeting) => {
		if (window.confirm(`Meeting '${meeting.name}' wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/meetings/${meeting.id}`);
				if (result.success) {
					addToast('Meeting gelöscht.', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	return (
		<div>
			<h1>Meetings für "{courseName}"</h1>
			<Link to="/admin/lehrgaenge" style={{ marginBottom: '1rem', display: 'inline-block' }}>
				<i className="fas fa-arrow-left"></i> Zurück zu allen Vorlagen
			</Link>

			<div className="table-controls">
				<button onClick={handleOpenNewModal} className="btn btn-success">
					<i className="fas fa-plus"></i> Neues Meeting planen
				</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Meeting-Name</th>
							<th>Datum & Uhrzeit</th>
							<th>Teilnehmer</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="4">Lade Meetings...</td></tr>}
						{error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
						{meetingsData?.map(meeting => (
							<tr key={meeting.id}>
								<td>
									<Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link>
									{meeting.parentMeetingId > 0 && <i className="fas fa-redo-alt" style={{ marginLeft: '0.5rem', color: 'var(--text-muted-color)' }} title={`Wiederholung von Termin ID ${meeting.parentMeetingId}`}></i>}
								</td>
								<td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
								<td>
									<button onClick={() => setViewingParticipantsMeeting(meeting)} className="btn btn-small btn-info">
										{meeting.participantCount || 0} / {meeting.maxParticipants || '∞'} (+{meeting.waitlistCount || 0})
									</button>
								</td>
								<td style={{ display: 'flex', gap: '0.5rem' }}>
									<button onClick={() => handleOpenEditModal(meeting)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleOpenRepeatModal(meeting)} className="btn btn-small btn-secondary">Wiederholen</button>
									<button onClick={() => handleDelete(meeting)} className="btn btn-small btn-danger">Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			{isModalOpen && (
				<Modal isOpen={isModalOpen} onClose={handleCloseModal} title={editingMeeting ? "Meeting bearbeiten" : "Neues Meeting planen"}>
					<form onSubmit={handleSubmit}>
						{formError && <p className="error-message">{formError}</p>}
						<div className="form-group">
							<label htmlFor="name-modal">Name des Meetings</label>
							<input type="text" id="name-modal" name="name" defaultValue={editingMeeting?.name} required />
						</div>
						<div className="responsive-dashboard-grid">
							<div className="form-group">
								<label htmlFor="meetingDateTime-modal">Beginn</label>
								<input type="datetime-local" id="meetingDateTime-modal" name="meetingDateTime" defaultValue={editingMeeting?.meetingDateTime ? editingMeeting.meetingDateTime.substring(0, 16) : ''} required />
							</div>
							<div className="form-group">
								<label htmlFor="endDateTime-modal">Ende (optional)</label>
								<input type="datetime-local" id="endDateTime-modal" name="endDateTime" defaultValue={editingMeeting?.endDateTime ? editingMeeting.endDateTime.substring(0, 16) : ''} />
							</div>
						</div>
						<div className="form-group">
							<label htmlFor="location-modal">Ort</label>
							<input type="text" id="location-modal" name="location" defaultValue={editingMeeting?.location} />
						</div>
						<div className="form-group">
							<label htmlFor="leader-modal">Leitende Person</label>
							<select name="leaderUserId" id="leader-modal" defaultValue={editingMeeting?.leaderUserId}>
								<option value="">(Keine)</option>
								{allUsers?.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}
							</select>
						</div>
						<div className="responsive-dashboard-grid">
							<div className="form-group">
								<label htmlFor="maxParticipants-modal">Max. Teilnehmer</label>
								<input type="number" id="maxParticipants-modal" name="maxParticipants" defaultValue={editingMeeting?.maxParticipants} min="1" placeholder="Unbegrenzt" />
							</div>
							<div className="form-group">
								<label htmlFor="signupDeadline-modal">Anmeldefrist (optional)</label>
								<input type="datetime-local" id="signupDeadline-modal" name="signupDeadline" defaultValue={editingMeeting?.signupDeadline ? editingMeeting.signupDeadline.substring(0, 16) : ''} />
							</div>
						</div>
						<div className="form-group">
							<label htmlFor="description-modal">Beschreibung</label>
							<textarea id="description-modal" name="description" defaultValue={editingMeeting?.description} rows="3"></textarea>
						</div>
						<button type="submit" className="btn"><i className="fas fa-save"></i> Speichern</button>
					</form>
				</Modal>
			)}
			{isRepeatModalOpen && (
				<RepeatMeetingModal
					isOpen={isRepeatModalOpen}
					onClose={handleCloseRepeatModal}
					onSuccess={() => { handleCloseRepeatModal(); reload(); }}
					meeting={repeatingMeeting}
				/>
			)}
			{viewingParticipantsMeeting && (
				<ParticipantsModal
					isOpen={!!viewingParticipantsMeeting}
					onClose={() => setViewingParticipantsMeeting(null)}
					meeting={viewingParticipantsMeeting}
					onPromote={handlePromoteUser}
				/>
			)}
		</div>
	);
};

export default AdminMeetingsPage;