import React, { useCallback, useMemo } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const HelpDetailsPage = () => {
	const { pageKey } = useParams();
	const apiCall = useCallback(() => apiClient.get(`/public/documentation/${pageKey}`), [pageKey]);
	const { data: doc, loading, error } = useApi(apiCall);
	const { data: allDocs } = useApi(useCallback(() => apiClient.get('/public/documentation'), []));

	const relatedPageDetails = useMemo(() => {
		if (!doc || !doc.relatedPages || !allDocs) return [];
		try {
			const relatedKeys = JSON.parse(doc.relatedPages);
			return relatedKeys
				.map(key => allDocs.find(d => d.pageKey === key))
				.filter(Boolean);
		} catch (e) {
			return [];
		}
	}, [doc, allDocs]);


	if (loading) return <div>Lade Dokumentation...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!doc) return <div className="error-message">Dokumentation nicht gefunden.</div>;

	return (
		<div>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
				<h1>{doc.title}</h1>
				<Link to={doc.pagePath} className="btn btn-secondary" target="_blank" rel="noopener noreferrer">
					<i className="fas fa-external-link-alt"></i> Seite öffnen
				</Link>
			</div>

			<div className="card">
				<h2 className="card-title">Features</h2>
				<div className="markdown-content">
					<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{doc.features}</ReactMarkdown>
				</div>
			</div>

			<div className="responsive-dashboard-grid">
				<div className="card">
					<h2 className="card-title">Verknüpfte Seiten</h2>
					{relatedPageDetails.length > 0 ? (
						<ul className="details-list">
							{relatedPageDetails.map(p => (
								<li key={p.id}><Link to={`/help/${p.pageKey}`}>{p.title}</Link></li>
							))}
						</ul>
					) : <p>Keine verknüpften Seiten.</p>}
				</div>
				<div className="card">
					<h2 className="card-title">Technische Details</h2>
					<p>Weitere technische Informationen zu dieser Seite finden Sie in der Admin-Wiki.</p>
					{doc.wikiLink ? (
						<a href={doc.wikiLink} className="btn btn-small" target="_blank" rel="noopener noreferrer">
							Zur Admin-Wiki
						</a>
					) : (
						<p className="text-muted">Für diese Seite ist kein technischer Wiki-Artikel verknüpft.</p>
					)}
				</div>
			</div>

			<Link to="/help" className="btn" style={{ marginTop: '1.5rem' }}>
				<i className="fas fa-arrow-left"></i> Zurück zur Übersicht
			</Link>
		</div>
	);
};

export default HelpDetailsPage;