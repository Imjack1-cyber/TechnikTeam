import React from 'react';
import { Link } from 'react-router-dom';

const ProfileEventHistory = ({ eventHistory }) => {
	const formatDate = (dateString) => {
		if (!dateString) return '';
		return new Date(dateString).toLocaleString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
	};

	return (
		<div className="card" style={{ gridColumn: '1 / -1' }} id="profile-history-container">
			<h2 className="card-title">Meine Event-Historie</h2>
			<div className="desktop-table-wrapper">
				<div className="table-wrapper" style={{ maxHeight: '500px', overflowY: 'auto' }}>
					<table className="data-table">
						<thead>
							<tr>
								<th>Event</th>
								<th>Datum</th>
								<th>Dein Status</th>
								<th>Feedback</th>
							</tr>
						</thead>
						<tbody>
							{eventHistory.length === 0 ? (
								<tr><td colSpan="4">Keine Event-Historie vorhanden.</td></tr>
							) : (
								eventHistory.map(event => (
									<tr key={event.id}>
										<td><Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link></td>
										<td>{formatDate(event.eventDateTime)} Uhr</td>
										<td>{event.userAttendanceStatus}</td>
										<td>
											{event.status === 'ABGESCHLOSSEN' && event.userAttendanceStatus === 'ZUGEWIESEN' ? (
												<Link to={`/feedback/event/${event.id}`} className="btn btn-small">Feedback geben</Link>
											) : (
												<span className="text-muted">-</span>
											)}
										</td>
									</tr>
								))
							)}
						</tbody>
					</table>
				</div>
			</div>
			<div className="mobile-card-list">
				{eventHistory.length === 0 ? (
					<p>Keine Event-Historie vorhanden.</p>
				) : (
					eventHistory.map(event => (
						<div className="list-item-card" key={event.id}>
							<h3 className="card-title"><Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link></h3>
							<div className="card-row"><strong>Datum:</strong> <span>{formatDate(event.eventDateTime)} Uhr</span></div>
							<div className="card-row"><strong>Dein Status:</strong> <span>{event.userAttendanceStatus}</span></div>
							{event.status === 'ABGESCHLOSSEN' && event.userAttendanceStatus === 'ZUGEWIESEN' && (
								<div className="card-actions">
									<Link to={`/feedback/event/${event.id}`} className="btn btn-small">Feedback geben</Link>
								</div>
							)}
						</div>
					))
				)}
			</div>
		</div>
	);
};

export default ProfileEventHistory;