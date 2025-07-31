import React, { useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';

const LehrgaengePage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/meetings'), []);
	const { data: meetings, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();

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
		</div>
	);
};

export default LehrgaengePage;