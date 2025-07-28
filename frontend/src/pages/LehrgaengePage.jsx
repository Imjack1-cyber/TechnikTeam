import React from 'react';
import { Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';

const LehrgaengePage = () => {
	const { data: meetings, loading, error, reload } = useApi(() => apiClient.get('/public/meetings'));

	const handleAction = async (meetingId, action) => {
		const endpoint = `/public/meetings/${meetingId}/${action}`;
		const successMessage = action === 'signup' ? 'Erfolgreich angemeldet!' : 'Erfolgreich abgemeldet!';
		const failureMessage = action === 'signup' ? 'Anmeldung fehlgeschlagen.' : 'Abmeldung fehlgeschlagen.';

		try {
			const result = await apiClient.post(endpoint, {});
			if (result.success) {
				// In a real app, show a toast notification here.
				console.log(successMessage);
				reload(); // Refresh the list to show the new status
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			console.error(err);
			// In a real app, show an error toast here.
			alert(err.message || failureMessage);
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

	const renderContent = () => {
		if (loading) return <tr><td colSpan="5" style={{ textAlign: 'center' }}>Lade Lehrgänge...</td></tr>;
		if (error) return <tr><td colSpan="5" className="error-message">{error}</td></tr>;
		if (!meetings || meetings.length === 0) {
			return <tr><td colSpan="5" style={{ textAlign: 'center' }}>Derzeit stehen keine Lehrgänge oder Meetings an.</td></tr>;
		}

		return meetings.map(meeting => (
			<tr key={meeting.id}>
				<td><Link to={`/lehrgaenge/details/${meeting.id}`}>{meeting.name}</Link></td>
				<td>{meeting.parentCourseName}</td>
				<td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
				<td>{renderUserStatus(meeting.userAttendanceStatus)}</td>
				<td>{renderActionButtons(meeting)}</td>
			</tr>
		));
	};

	return (
		<div>
			<h1>Anstehende Lehrgänge & Meetings</h1>
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
						{renderContent()}
					</tbody>
				</table>
			</div>
			{/* A mobile card view could be rendered here as well */}
		</div>
	);
};

export default LehrgaengePage;