import React, { useCallback } from 'react';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const ChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/changelog'), []);
	const { data: changelogs, loading, error } = useApi(apiCall);

	return (
		<div>
			<h1><i className="fas fa-history"></i> Changelogs & Neuerungen</h1>
			<p>Hier finden Sie eine Übersicht aller wichtigen Änderungen und neuen Features der Anwendung.</p>
			{loading && <p>Lade Changelogs...</p>}
			{error && <p className="error-message">{error}</p>}
			{changelogs?.map(cl => (
				<div className="card" key={cl.id}>
					<h2 className="card-title" style={{ border: 'none', padding: 0 }}>
						Version {cl.version} - {cl.title}
					</h2>
					<p className="details-subtitle" style={{ marginTop: '-0.5rem' }}>
						Veröffentlicht am {new Date(cl.releaseDate).toLocaleDateString('de-DE')}
					</p>
					<div className="markdown-content">
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{cl.notes}</ReactMarkdown>
					</div>
				</div>
			))}
		</div>
	);
};

export default ChangelogPage;