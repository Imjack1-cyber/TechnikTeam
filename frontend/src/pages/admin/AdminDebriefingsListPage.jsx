import React, { useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const AdminDebriefingsListPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/events/debriefings'), []);
	const { data: debriefings, loading, error } = useApi(apiCall);

	if (loading) return <div>Lade Debriefings...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div>
			<h1><i className="fas fa-clipboard-check"></i> Event-Debriefings</h1>
			<p>Eine Übersicht aller nachbereiteten Veranstaltungen zur Analyse und Verbesserung.</p>

			{debriefings?.length === 0 ? (
				<div className="card"><p>Es wurden noch keine Debriefings eingereicht.</p></div>
			) : (
				debriefings?.map(debrief => (
					<div className="card" key={debrief.id}>
						<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
							<h2 className="card-title" style={{ border: 'none', padding: 0 }}>
								<Link to={`/admin/veranstaltungen/${debrief.eventId}/debriefing`}>{debrief.eventName}</Link>
							</h2>
							<small>Eingereicht von {debrief.authorUsername} am {new Date(debrief.submittedAt).toLocaleDateString('de-DE')}</small>
						</div>
						<div className="responsive-dashboard-grid" style={{ alignItems: 'flex-start' }}>
							<div>
								<h4>Was lief gut?</h4>
								<div className="markdown-content"><ReactMarkdown rehypePlugins={[rehypeSanitize]}>{debrief.whatWentWell}</ReactMarkdown></div>
							</div>
							<div>
								<h4>Was kann verbessert werden?</h4>
								<div className="markdown-content"><ReactMarkdown rehypePlugins={[rehypeSanitize]}>{debrief.whatToImprove}</ReactMarkdown></div>
							</div>
						</div>
						{debrief.standoutCrewDetails?.length > 0 && (
							<div style={{ marginTop: '1rem' }}>
								<h4>Besonders hervorgehobene Mitglieder:</h4>
								<p>{debrief.standoutCrewDetails.map(u => u.username).join(', ')}</p>
							</div>
						)}
					</div>
				))
			)}
		</div>
	);
};

export default AdminDebriefingsListPage;