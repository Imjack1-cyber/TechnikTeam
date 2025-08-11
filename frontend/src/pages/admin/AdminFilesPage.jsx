import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';

const FileUploadModal = ({ isOpen, onClose, onSuccess, categories, existingFile = null }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const formData = new FormData(e.target);
		const file = formData.get('file');

		if (file.size > 10 * 1024 * 1024) { // 10MB limit
			setError('Datei ist zu groß. Maximal 10MB erlaubt.');
			setIsSubmitting(false);
			return;
		}

		try {
			// If replacing, we use the special POST endpoint with the ID
			const url = existingFile ? `/admin/files/replace/${existingFile.id}` : '/admin/files';
			const result = await apiClient.post(url, formData);

			if (result.success) {
				addToast(`Datei erfolgreich ${existingFile ? 'ersetzt' : 'hochgeladen'}`, 'success');
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
		<Modal isOpen={isOpen} onClose={onClose} title={existingFile ? `Datei ersetzen: ${existingFile.filename}` : "Neue Datei hochladen"}>
			<form onSubmit={handleSubmit} encType="multipart/form-data">
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="file-upload">Datei auswählen</label>
					<input type="file" id="file-upload" name="file" required />
				</div>
				<div className="form-group">
					<label htmlFor="categoryId-upload">Kategorie</label>
					<select name="categoryId" id="categoryId-upload" defaultValue={existingFile?.categoryId || categories[0]?.id}>
						{categories.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
					</select>
				</div>
				<div className="form-group">
					<label htmlFor="requiredRole-upload">Sichtbarkeit</label>
					<select name="requiredRole" id="requiredRole-upload" defaultValue={existingFile?.requiredRole || "NUTZER"}>
						<option value="NUTZER">Alle Benutzer</option>
						<option value="ADMIN">Nur Admins</option>
					</select>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird hochgeladen...' : (existingFile ? 'Ersetzen' : 'Hochladen')}
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
	const [modalState, setModalState] = useState({ isOpen: false, file: null });
	const { addToast } = useToast();

	const filesGrouped = fileApiResponse?.grouped;

	const handleSuccess = () => {
		setModalState({ isOpen: false, file: null });
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

	const handleRenameFile = async (file) => {
		const newName = prompt('Neuer Dateiname:', file.filename);
		if (newName && newName !== file.filename) {
			try {
				await apiClient.put(`/admin/files/${file.id}/rename`, { newName });
				addToast('Datei umbenannt', 'success');
				reloadFiles();
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

	const handleRenameCategory = async (categoryId, currentName) => {
		const newName = prompt('Neuer Name für die Kategorie:', currentName);
		if (newName && newName !== currentName) {
			try {
				await apiClient.put(`/admin/files/categories/${categoryId}`, { name: newName });
				addToast('Kategorie umbenannt', 'success');
				reloadCats();
				reloadFiles();
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	const handleDeleteCategory = async (categoryId, categoryName) => {
		if (window.confirm(`Kategorie "${categoryName}" wirklich löschen? Alle Dateien in dieser Kategorie werden in "Ohne Kategorie" verschoben.`)) {
			try {
				await apiClient.delete(`/admin/files/categories/${categoryId}`);
				addToast('Kategorie gelöscht', 'success');
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

		return Object.entries(filesGrouped).map(([categoryName, files]) => {
			const categoryId = files[0]?.categoryId;
			return (
				<div className="card" key={categoryName}>
					<div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
						<h2><i className="fas fa-folder"></i> {categoryName}</h2>
						{categoryId && (
							<div>
								<button className="btn btn-small btn-secondary" onClick={() => handleRenameCategory(categoryId, categoryName)}>
									<i className="fas fa-edit"></i>
								</button>
								<button className="btn btn-small btn-danger-outline" style={{ marginLeft: '0.5rem' }} onClick={() => handleDeleteCategory(categoryId, categoryName)}>
									<i className="fas fa-trash"></i>
								</button>
							</div>
						)}
					</div>
					<ul className="details-list">
						{files.map(file => {
							const isMarkdown = file.filename.toLowerCase().endsWith('.md');
							const editUrl = file.requiredRole === 'ADMIN' ? `/admin/content/dateien/edit/${file.id}` : `/dateien/edit/${file.id}`;
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
									<div style={{ display: 'flex', gap: '0.5rem' }}>
										{isMarkdown && (
											<Link to={editUrl} className="btn btn-small btn-secondary" title="Inhalt bearbeiten">
												<i className="fas fa-pen-alt"></i>
											</Link>
										)}
										<button onClick={() => handleRenameFile(file)} className="btn btn-small btn-secondary" title="Umbenennen">
											<i className="fas fa-i-cursor"></i>
										</button>
										<button onClick={() => setModalState({ isOpen: true, file: file })} className="btn btn-small btn-secondary" title="Ersetzen">
											<i className="fas fa-sync-alt"></i>
										</button>
										<button onClick={() => handleDeleteFile(file)} className="btn btn-small btn-danger">Löschen</button>
									</div>
								</li>
							);
						})}
					</ul>
				</div>
			);
		});
	};

	return (
		<div>
			<h1><i className="fas fa-file-upload"></i> Datei-Verwaltung</h1>
			<p>Hier können Sie alle zentralen Dokumente und Vorlagen verwalten.</p>
			<div className="table-controls">
				<button onClick={() => setModalState({ isOpen: true, file: null })} className="btn btn-success">
					<i className="fas fa-upload"></i> Neue Datei hochladen
				</button>
				<button onClick={handleCreateCategory} className="btn btn-secondary">
					<i className="fas fa-folder-plus"></i> Neue Kategorie
				</button>
			</div>

			{catsError && <p className="error-message">{catsError}</p>}
			{renderContent()}

			{modalState.isOpen && (
				<FileUploadModal
					isOpen={modalState.isOpen}
					onClose={() => setModalState({ isOpen: false, file: null })}
					onSuccess={handleSuccess}
					categories={categories || []}
					existingFile={modalState.file}
				/>
			)}
		</div>
	);
};

export default AdminFilesPage;