import React, { useCallback, useMemo } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const HelpListPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/documentation'), []);
	const { data: docs, loading, error } = useApi(apiCall);

	const groupedDocs = useMemo(() => {
		if (!docs) return {};
		return docs.reduce((acc, doc) => {
			const category = doc.category || 'Sonstiges';
			if (!acc[category]) {
				acc[category] = [];
			}
			acc[category].push(doc);
			return acc;
		}, {});
	}, [docs]);

	return (
		<div>
			<h1><i className="fas fa-question-circle"></i> Hilfe & Dokumentation</h1>
			<p>Hier finden Sie Erklärungen zu den einzelnen Seiten und Funktionen der Anwendung.</p>

			{loading && <p>Lade Dokumentation...</p>}
			{error && <p className="error-message">{error}</p>}

			{Object.entries(groupedDocs).map(([category, docsInCategory]) => (
				<div className="card" key={category}>
					<h2 className="card-title">{category}</h2>
					<ul className="details-list">
						{docsInCategory.map(doc => (
							<li key={doc.id}>
								<Link to={`/help/${doc.pageKey}`} style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
									{doc.adminOnly && <i className="fas fa-user-shield" title="Nur für Admins"></i>}
									{doc.title}
								</Link>
								<span><i className="fas fa-arrow-right"></i></span>
							</li>
						))}
					</ul>
				</div>
			))}
		</div>
	);
};

export default HelpListPage;