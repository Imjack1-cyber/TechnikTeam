import React, { useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import RepeatMeetingModal from '../../components/admin/meetings/RepeatMeetingModal';

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


	const handleSubmit = async (e) => {
		e.preventDefault();
		const formData = new FormData(e.target);
		const data = Object.fromEntries(formData.entries());

		const payload = {
			...data,
			courseId: parseInt(courseId),
			leaderUserId: data.leaderUserId ? parseInt(data.leaderUserId) : null
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
							<th>Leitung</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="4">Lade Meetings...</td></tr>}
						{error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
						{meetingsData?.map(meeting => (
							<tr key={meeting.id}>
								<td><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></td>
								<td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
								<td>{meeting.leaderUsername || 'N/A'}</td>
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

			<div className="mobile-card-list">
				{loading && <p>Lade Meetings...</p>}
				{error && <p className="error-message">{error}</p>}
				{meetingsData?.map(meeting => (
					<div className="list-item-card" key={meeting.id}>
						<h3 className="card-title"><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></h3>
						<div className="card-row"><strong>Datum:</strong> <span>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</span></div>
						<div className="card-row"><strong>Leitung:</strong> <span>{meeting.leaderUsername || 'N/A'}</span></div>
						<div className="card-actions">
							<button onClick={() => handleOpenEditModal(meeting)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleOpenRepeatModal(meeting)} className="btn btn-small btn-secondary">Wiederholen</button>
							<button onClick={() => handleDelete(meeting)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
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
		</div>
	);
};

export default AdminMeetingsPage;