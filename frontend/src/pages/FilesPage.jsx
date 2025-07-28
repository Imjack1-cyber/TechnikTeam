import React from 'react';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';

const FilesPage = () => {
	// NOTE: This assumes the backend exposes a public endpoint at /api/v1/public/files
	// which returns files grouped by category based on the authenticated user's role.
	const { data: fileData, loading, error } = useApi(() => apiClient.get('/public/files'));

	const renderContent = () => {
		if (loading) return <div className="card"><p>Lade Dateien...</p></div>;
		if (error) return <div className="error-message">{error}</div>;
		if (!fileData || Object.keys(fileData).length === 0) {
			return <div className="card"><p>Es sind keine Dateien oder Dokumente verfügbar.</p></div>;
		}

		return Object.entries(fileData).map(([categoryName, files]) => (
			<div className="card" key={categoryName}>
				<h2>
					<i className="fas fa-folder"></i> {categoryName}
				</h2>
				<ul className="file-list">
					{files.map(file => (
						<li key={file.id} style={{ padding: '0.75rem 0' }}>
							<div>
								<a href={`/api/v1/public/files/download/${file.id}`} target="_blank" rel="noopener noreferrer">
									<i className="fas fa-download"></i> {file.filename}
								</a>
							</div>
							{/* Additional actions like 'Edit' could be added here based on permissions */}
						</li>
					))}
				</ul>
			</div>
		));
	};

	return (
		<div>
			<h1>Dateien & Dokumente</h1>
			<p>Hier können Sie zentrale Dokumente und Vorlagen herunterladen.</p>
			{renderContent()}
		</div>
	);
};

export default FilesPage;