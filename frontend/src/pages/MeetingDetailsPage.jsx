import React from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';

const MeetingDetailsPage = () => {
	const { meetingId } = useParams();

	// NOTE: Assumes a public API endpoint at `/api/v1/public/meetings/${meetingId}` will be created.
	// The backend code currently only provides an admin version of this.
	const { data, loading, error } = useApi(() => apiClient.get(`/meetings/${meetingId}`));

	if (loading) return <div>Lade Meeting-Details...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!data) return <div className="error-message">Meeting nicht gefunden.</div>;

	const { meeting, attachments } = data;

	const renderMarkdown = (content) => {
		// In a real app, use a library like `marked` or `react-markdown`
		return { __html: (content || '').replace(/\n/g, '<br />') };
	};

	return (
		<div>
			<h1>{meeting.parentCourseName}</h1>
			<h2 style={{ border: 'none', padding: 0, marginTop: '-1rem' }}>{meeting.name}</h2>

			<div className="responsive-dashboard-grid" style={{ gridTemplateColumns: '2fr 1fr', alignItems: 'flex-start' }}>
				<div className="card">
					<ul className="details-list">
						<li>
							<strong>Datum & Uhrzeit:</strong>
							<span>
								{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}
								{meeting.endDateTime && ` - ${new Date(meeting.endDateTime).toLocaleString('de-DE')}`}
							</span>
						</li>
						<li><strong>Ort:</strong> <span>{meeting.location || 'N/A'}</span></li>
						<li><strong>Leitung:</strong> <span>{meeting.leaderUsername || 'N/A'}</span></li>
					</ul>
					<h3 style={{ marginTop: '2rem' }}>Beschreibung</h3>
					<div className="markdown-content" dangerouslySetInnerHTML={renderMarkdown(meeting.description || 'Keine Beschreibung für dieses Meeting vorhanden.')} />
				</div>

				<div className="card">
					<h2 className="card-title">Anhänge</h2>
					{attachments?.length > 0 ? (
						<ul className="details-list">
							{attachments.map(att => (
								<li key={att.id}>
									<a href={`/api/v1/public/files/download/${att.id}`} target="_blank" rel="noopener noreferrer">{att.filename}</a>
								</li>
							))}
						</ul>
					) : (
						<p>Für dieses Meeting sind keine Anhänge verfügbar.</p>
					)}
				</div>
			</div>

			<div style={{ marginTop: '1rem' }}>
				<Link to="/lehrgaenge" className="btn btn-secondary">
					<i className="fas fa-arrow-left"></i> Zurück zu allen Lehrgängen
				</Link>
			</div>
		</div>
	);
};

export default MeetingDetailsPage;