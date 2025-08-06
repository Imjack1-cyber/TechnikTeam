import React, { useCallback, useState } from 'react';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import DownloadWarningModal from '../components/ui/DownloadWarningModal';

const FileLink = ({ file }) => {
	const [isModalOpen, setIsModalOpen] = useState(false);

	const handleDownloadClick = (e) => {
		if (file.needsWarning) {
			e.preventDefault();
			setIsModalOpen(true);
		}
	};

	const handleConfirmDownload = () => {
		setIsModalOpen(false);
		// Programmatically trigger the download by creating a temporary link
		const link = document.createElement('a');
		link.href = `/api/v1/public/files/download/${file.id}`;
		link.setAttribute('download', file.filename);
		document.body.appendChild(link);
		link.click();
		document.body.removeChild(link);
	};

	return (
		<>
			<a href={`/api/v1/public/files/download/${file.id}`} target="_blank" rel="noopener noreferrer" onClick={handleDownloadClick}>
				<i className="fas fa-download"></i> {file.filename}
			</a>
			<DownloadWarningModal
				isOpen={isModalOpen}
				onClose={() => setIsModalOpen(false)}
				onConfirm={handleConfirmDownload}
				file={file}
			/>
		</>
	);
};

const FilesPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/files'), []);
	const { data: fileData, loading, error } = useApi(apiCall);

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
				<ul className="details-list">
					{files.map(file => (
						<li key={file.id} style={{ padding: '0.75rem 0' }}>
							<div>
								<FileLink file={file} />
							</div>
						</li>
					))}
				</ul>
			</div>
		));
	};

	return (
		<div>
			<h1><i className="fas fa-folder-open"></i> Dateien & Dokumente</h1>
			<p>Hier können Sie zentrale Dokumente und Vorlagen herunterladen.</p>
			{renderContent()}
		</div>
	);
};

export default FilesPage;