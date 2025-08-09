import React, { useCallback } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';

const SearchResultsPage = () => {
	const [searchParams] = useSearchParams();
	const query = searchParams.get('q');

	const apiCall = useCallback(() => {
		if (!query) return Promise.resolve({ success: true, data: [] });
		return apiClient.get(`/public/search?query=${encodeURIComponent(query)}`);
	}, [query]);

	const { data: results, loading, error } = useApi(apiCall);

	const getIconForType = (type) => {
		switch (type) {
			case 'Veranstaltung': return 'fa-calendar-check';
			case 'Lagerartikel': return 'fa-cube';
			case 'Lehrgang': return 'fa-graduation-cap';
			case 'Dokumentation': return 'fa-file-alt';
			default: return 'fa-search';
		}
	};

	return (
		<div>
			<h1><i className="fas fa-search"></i> Suchergebnisse</h1>
			{query ? (
				<p className="details-subtitle" style={{ marginTop: '-1rem' }}>
					Ergebnisse für: <strong>"{query}"</strong>
				</p>
			) : (
				<p>Bitte geben Sie einen Suchbegriff in die Suchleiste ein.</p>
			)}

			{loading && <div>Suche läuft...</div>}
			{error && <div className="error-message">{error}</div>}

			{results && (
				<div className="card">
					{results.length === 0 && !loading ? (
						<p>Keine Ergebnisse für Ihre Suche gefunden.</p>
					) : (
						<ul className="details-list">
							{results.map((result, index) => (
								<li key={index} style={{ alignItems: 'flex-start' }}>
									<div style={{ flex: '1' }}>
										<Link to={result.url}>
											<i className={`fas ${getIconForType(result.type)} fa-fw`}></i>
											<strong> {result.title}</strong>
										</Link>
										<small style={{ display: 'block', color: 'var(--text-muted-color)', marginLeft: '1.75rem' }}>
											{result.snippet}
										</small>
									</div>
									<span className="status-badge status-info">{result.type}</span>
								</li>
							))}
						</ul>
					)}
				</div>
			)}
		</div>
	);
};

export default SearchResultsPage;