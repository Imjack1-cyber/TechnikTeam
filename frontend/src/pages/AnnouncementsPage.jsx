import React, { useCallback } from 'react';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const AnnouncementsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/announcements'), []);
	const { data: announcements, loading, error } = useApi(apiCall);

	return (
		<div>
			<h1><i className="fas fa-thumbtack"></i> Anschlagbrett</h1>
			<p>Wichtige und langfristige Mitteilungen für das gesamte Team.</p>

			{loading && <p>Lade Mitteilungen...</p>}
			{error && <p className="error-message">{error}</p>}

			{announcements?.length === 0 && (
				<div className="card">
					<p>Aktuell gibt es keine neuen Mitteilungen.</p>
				</div>
			)}

			{announcements?.map(post => (
				<div className="card" key={post.id}>
					<h2 className="card-title">{post.title}</h2>
					<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
						Gepostet von <strong>{post.authorUsername}</strong> am {new Date(post.createdAt).toLocaleDateString('de-DE')}
					</p>
					<div className="markdown-content">
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{post.content}</ReactMarkdown>
					</div>
				</div>
			))}
		</div>
	);
};

export default AnnouncementsPage;