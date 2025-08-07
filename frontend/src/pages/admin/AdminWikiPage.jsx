import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const WikiPageModal = ({ isOpen, onClose, onSuccess, parentPath }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const formData = new FormData(e.target);
		const fileName = formData.get('fileName');
		const fullPath = parentPath ? `${parentPath}/${fileName}` : fileName;

		try {
			const result = await apiClient.post('/wiki', { filePath: fullPath, content: `# ${fileName}\n\nNeue Seite.` });
			if (result.success) {
				addToast('Seite erfolgreich erstellt', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Erstellen der Seite fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neue Wiki-Seite erstellen">
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="wiki-parent-path">Übergeordneter Pfad</label>
					<input id="wiki-parent-path" type="text" value={parentPath || '/'} readOnly disabled />
				</div>
				<div className="form-group">
					<label htmlFor="wiki-file-name">Dateiname (z.B. `neue-seite.md`)</label>
					<input id="wiki-file-name" name="fileName" required pattern=".*\.md$" title="Muss mit .md enden" />
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird erstellt...' : 'Seite erstellen'}
				</button>
			</form>
		</Modal>
	);
};


const AdminWikiPage = () => {
	const treeApiCall = useCallback(() => apiClient.get('/wiki'), []);
	const { data: wikiTree, loading, error, reload } = useApi(treeApiCall);
	const [selectedEntry, setSelectedEntry] = useState(null);
	const [isEditing, setIsEditing] = useState(false);
	const [editContent, setEditContent] = useState('');
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [modalParentPath, setModalParentPath] = useState('');
	const { addToast } = useToast();

	const handleSelectEntry = async (entry) => {
		try {
			const result = await apiClient.get(`/wiki/${entry.id}`);
			if (result.success) {
				setSelectedEntry(result.data);
				setEditContent(result.data.content);
				setIsEditing(false);
			}
		} catch (err) {
			addToast(`Fehler beim Laden der Seite: ${err.message}`, 'error');
		}
	};

	const handleSave = async () => {
		if (!selectedEntry) return;
		try {
			const result = await apiClient.put(`/wiki/${selectedEntry.id}`, { content: editContent });
			if (result.success) {
				addToast('Seite gespeichert', 'success');
				await handleSelectEntry(selectedEntry); // Reload content
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(`Fehler beim Speichern: ${err.message}`, 'error');
		}
	};

	const handleDelete = async () => {
		if (!selectedEntry) return;
		if (window.confirm(`Seite "${selectedEntry.filePath}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/wiki/${selectedEntry.id}`);
				if (result.success) {
					addToast('Seite gelöscht', 'success');
					setSelectedEntry(null);
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler beim Löschen: ${err.message}`, 'error');
			}
		}
	};

	const renderTree = (node, path = '') => (
		<ul>
			{Object.entries(node).map(([name, child]) => (
				<li key={path + name}>
					{child.id ? ( // It's a file
						<a href="#" onClick={(e) => { e.preventDefault(); handleSelectEntry(child); }} className={selectedEntry?.id === child.id ? 'active' : ''}>
							<i className="fas fa-file-alt fa-fw"></i> {name}
						</a>
					) : ( // It's a directory
						<details open>
							<summary><i className="fas fa-folder fa-fw"></i> {name}</summary>
							{renderTree(child, `${path}${name}/`)}
						</details>
					)}
				</li>
			))}
		</ul>
	);

	if (loading) return <div>Lade Wiki-Struktur...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div className={`wiki-page-wrapper ${selectedEntry ? 'content-visible' : 'tree-visible'}`}>
			<aside className="wiki-sidebar">
				<div className="wiki-sidebar-header">
					<h3>Wiki-Verzeichnis</h3>
					<button className="btn btn-small" onClick={() => { setModalParentPath(''); setIsModalOpen(true); }}>+</button>
				</div>
				<div className="wiki-tree-container">
					{wikiTree && renderTree(wikiTree)}
				</div>
			</aside>
			<main className="wiki-content-pane">
				{selectedEntry ? (
					<>
						<div className="wiki-content-header">
							<button className="mobile-only btn btn-small" onClick={() => setSelectedEntry(null)}>
								<i className="fas fa-arrow-left"></i> Verzeichnis
							</button>
							<h2>{selectedEntry.filePath}</h2>
							<div className="wiki-editor-controls">
								{isEditing ? (
									<>
										<button onClick={handleSave} className="btn btn-success btn-small">Speichern</button>
										<button onClick={() => setIsEditing(false)} className="btn btn-secondary btn-small">Abbrechen</button>
									</>
								) : (
									<>
										<button onClick={() => setIsEditing(true)} className="btn btn-small">Bearbeiten</button>
										<button onClick={handleDelete} className="btn btn-danger btn-small">Löschen</button>
									</>
								)}
							</div>
						</div>
						{isEditing ? (
							<textarea
								id="editor"
								className="form-group"
								value={editContent}
								onChange={(e) => setEditContent(e.target.value)}
								style={{ flexGrow: 1, fontFamily: 'monospace' }}
							/>
						) : (
							<div className="markdown-content">
								<ReactMarkdown rehypePlugins={[rehypeSanitize]}>
									{selectedEntry.content}
								</ReactMarkdown>
							</div>
						)}
					</>
				) : (
					<div className="wiki-welcome-pane desktop-only">
						<i className="fas fa-book-reader" style={{ fontSize: '4rem' }}></i>
						<h1>Wiki</h1>
						<p>Wählen Sie eine Seite aus der Navigation aus, um sie anzuzeigen oder zu bearbeiten.</p>
					</div>
				)}
			</main>
			{isModalOpen && (
				<WikiPageModal
					isOpen={isModalOpen}
					onClose={() => setIsModalOpen(false)}
					onSuccess={() => { setIsModalOpen(false); reload(); }}
					parentPath={modalParentPath}
				/>
			)}
		</div>
	);
};

export default AdminWikiPage;