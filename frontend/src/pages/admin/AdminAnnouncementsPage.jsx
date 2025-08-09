import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const AnnouncementModal = ({ isOpen, onClose, onSuccess, announcement }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const formData = new FormData(e.target);
		const data = Object.fromEntries(formData.entries());

		try {
			const result = announcement
				? await apiClient.put(`/admin/announcements/${announcement.id}`, data)
				: await apiClient.post('/admin/announcements', data);

			if (result.success) {
				addToast(`Mitteilung erfolgreich ${announcement ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={announcement ? "Mitteilung bearbeiten" : "Neue Mitteilung erstellen"}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="title">Titel</label>
					<input type="text" id="title" name="title" defaultValue={announcement?.title} required />
				</div>
				<div className="form-group">
					<label htmlFor="content">Inhalt (Markdown unterstützt)</label>
					<textarea id="content" name="content" defaultValue={announcement?.content} rows="10" required></textarea>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

const AdminAnnouncementsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/announcements'), []);
	const { data: announcements, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingAnnouncement, setEditingAnnouncement] = useState(null);
	const { addToast } = useToast();

	const openModal = (announcement = null) => {
		setEditingAnnouncement(announcement);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingAnnouncement(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (announcement) => {
		if (window.confirm(`Mitteilung "${announcement.title}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/announcements/${announcement.id}`);
				if (result.success) {
					addToast('Mitteilung gelöscht', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler: ${err.message}`, 'error');
			}
		}
	};

	return (
		<div>
			<h1><i className="fas fa-thumbtack"></i> Anschlagbrett verwalten</h1>
			<p>Verwalten Sie hier die Mitteilungen, die allen Benutzern auf dem Anschlagbrett angezeigt werden.</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neue Mitteilung
				</button>
			</div>

			{loading && <p>Lade Mitteilungen...</p>}
			{error && <p className="error-message">{error}</p>}

			{announcements?.map(post => (
				<div className="card" key={post.id}>
					<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
						<div>
							<h2 className="card-title" style={{ border: 'none', padding: 0 }}>{post.title}</h2>
							<p className="details-subtitle" style={{ marginTop: '-0.5rem' }}>
								Von <strong>{post.authorUsername}</strong> am {new Date(post.createdAt).toLocaleDateString('de-DE')}
							</p>
						</div>
						<div>
							<button onClick={() => openModal(post)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleDelete(post)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
						</div>
					</div>
					<div className="markdown-content" style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem 1rem', borderRadius: 'var(--border-radius)' }}>
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{post.content}</ReactMarkdown>
					</div>
				</div>
			))}

			{isModalOpen && (
				<AnnouncementModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					announcement={editingAnnouncement}
				/>
			)}
		</div>
	);
};

export default AdminAnnouncementsPage;