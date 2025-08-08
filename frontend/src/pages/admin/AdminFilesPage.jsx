import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';

const FileUploadModal = ({ isOpen, onClose, onSuccess, categories }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const formData = new FormData(e.target);
		const file = formData.get('file');

		console.log("[FileUploadModal] Submitting form data:");
		for (let [key, value] of formData.entries()) {
			console.log(`- ${key}:`, value instanceof File ? `${value.name} (${value.size} bytes)` : value);
		}

		if (file.size > 10 * 1024 * 1024) { // 10MB limit
			setError('Datei ist zu groß. Maximal 10MB erlaubt.');
			setIsSubmitting(false);
			return;
		}

		try {
			const result = await apiClient.post('/admin/files', formData);
			console.log("[FileUploadModal] API Response:", result);
			if (result.success) {
				addToast('Datei erfolgreich hochgeladen', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			console.error("[FileUploadModal] Upload failed:", err);
			setError(err.message || 'Upload fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neue Datei hochladen">
			<form onSubmit={handleSubmit} encType="multipart/form-data">
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="file-upload">Datei auswählen</label>
					<input type="file" id="file-upload" name="file" required />
				</div>
				<div className="form-group">
					<label htmlFor="categoryId-upload">Kategorie</label>
					<select name="categoryId" id="categoryId-upload">
						{categories.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
					</select>
				</div>
				<div className="form-group">
					<label htmlFor="requiredRole-upload">Sichtbarkeit</label>
					<select name="requiredRole" id="requiredRole-upload" defaultValue="NUTZER">
						<option value="NUTZER">Alle Benutzer</option>
						<option value="ADMIN">Nur Admins</option>
					</select>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird hochgeladen...' : 'Hochladen'}
				</button>
			</form>
		</Modal>
	);
};


const AdminFilesPage = () => {
	const filesApiCall = useCallback(() => apiClient.get('/admin/files'), []);
	const categoriesApiCall = useCallback(() => apiClient.get('/admin/files/categories'), []);

	const { data: fileApiResponse, loading: filesLoading, error: filesError, reload: reloadFiles } = useApi(filesApiCall);
	const { data: categories, loading: catsLoading, error: catsError, reload: reloadCats } = useApi(categoriesApiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const { addToast } = useToast();

	const filesGrouped = fileApiResponse?.grouped;
	const rawFiles = fileApiResponse?.raw;

	console.log("[AdminFilesPage] Render. Grouped data from API:", filesGrouped);

	const handleSuccess = () => {
		setIsModalOpen(false);
		reloadFiles();
		reloadCats();
	};

	const handleDeleteFile = async (file) => {
		if (window.confirm(`Datei "${file.filename}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/files/${file.id}`);
				if (result.success) {
					addToast('Datei gelöscht', 'success');
					reloadFiles();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	const handleCreateCategory = async () => {
		const name = prompt('Name für die neue Kategorie:');
		if (name) {
			try {
				await apiClient.post('/admin/files/categories', { name });
				addToast('Kategorie erstellt', 'success');
				reloadCats();
				reloadFiles();
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	const renderContent = () => {
		if (filesLoading) return <div className="card"><p>Lade Dateien...</p></div>;
		if (filesError) return <div className="error-message">{filesError}</div>;

		if (!filesGrouped || Object.keys(filesGrouped).length === 0) {
			return <div className="card"><p>Es sind keine Dateien oder Dokumente verfügbar.</p></div>;
		}

		return Object.entries(filesGrouped).map(([categoryName, files]) => (
			<div className="card" key={categoryName}>
				<h2><i className="fas fa-folder"></i> {categoryName}</h2>
				<ul className="details-list">
					{files.map(file => {
						console.log(`[AdminFilesPage] Rendering file: ${file.filename} in category: ${file.categoryName} (ID: ${file.categoryId})`);
						return (
							<li key={file.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
								<div>
									<a href={`/api/v1/public/files/download/${file.id}`} target="_blank" rel="noopener noreferrer">
										<i className="fas fa-download"></i> {file.filename}
									</a>
									<small style={{ display: 'block', color: 'var(--text-muted-color)' }}>
										Sichtbarkeit: {file.requiredRole}
									</small>
								</div>
								<div>
									<button onClick={() => handleDeleteFile(file)} className="btn btn-small btn-danger">Löschen</button>
								</div>
							</li>
						);
					})}
				</ul>
			</div>
		));
	};

	return (
		<div>
			<h1><i className="fas fa-file-upload"></i> Datei-Verwaltung</h1>
			<p>Hier können Sie alle zentralen Dokumente und Vorlagen verwalten.</p>
			<div className="table-controls">
				<button onClick={() => setIsModalOpen(true)} className="btn btn-success">
					<i className="fas fa-upload"></i> Neue Datei hochladen
				</button>
				<button onClick={handleCreateCategory} className="btn btn-secondary">
					<i className="fas fa-folder-plus"></i> Neue Kategorie
				</button>
			</div>

			<details style={{ marginBottom: '1rem' }}>
				<summary>Rohdaten-Diagnose</summary>
				<pre style={{ backgroundColor: 'var(--bg-color)', padding: '1rem', borderRadius: 'var(--border-radius)', maxHeight: '300px', overflowY: 'auto' }}>
					{JSON.stringify(rawFiles, null, 2)}
				</pre>
			</details>

			{catsError && <p className="error-message">{catsError}</p>}
			{renderContent()}

			{isModalOpen && (
				<FileUploadModal
					isOpen={isModalOpen}
					onClose={() => setIsModalOpen(false)}
					onSuccess={handleSuccess}
					categories={categories || []}
				/>
			)}
		</div>
	);
};

export default AdminFilesPage;