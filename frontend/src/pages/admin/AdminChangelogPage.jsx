import React, { useState, useCallback, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const AdminChangelogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/changelogs'), []);
	const { data: changelogs, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingChangelog, setEditingChangelog] = useState(null);
	const { addToast } = useToast();

	const openModal = (changelog = null) => {
		setEditingChangelog(changelog);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingChangelog(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (changelog) => {
		if (window.confirm(`Changelog für Version "${changelog.version}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/changelogs/${changelog.id}`);
				if (result.success) {
					addToast('Changelog gelöscht', 'success');
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
			<h1><i className="fas fa-history"></i> Changelogs verwalten</h1>
			<p>Verwalten Sie hier die "Was ist neu?"-Benachrichtigungen für die Benutzer.</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neuer Changelog-Eintrag
				</button>
			</div>
			{loading && <p>Lade Changelogs...</p>}
			{error && <p className="error-message">{error}</p>}
			{changelogs?.map(cl => (
				<div className="card" key={cl.id}>
					<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
						<h2 className="card-title" style={{ border: 'none', padding: 0 }}>Version {cl.version} - {cl.title}</h2>
						<div>
							<button onClick={() => openModal(cl)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleDelete(cl)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
						</div>
					</div>
					<p className="details-subtitle" style={{ marginTop: '-0.5rem' }}>
						Veröffentlicht: {new Date(cl.releaseDate).toLocaleDateString('de-DE')}
					</p>
					<div className="markdown-content">
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{cl.notes}</ReactMarkdown>
					</div>
				</div>
			))}
			{isModalOpen && (
				<ChangelogModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					changelog={editingChangelog}
				/>
			)}
		</div>
	);
};

const ChangelogModal = ({ isOpen, onClose, onSuccess, changelog }) => {
	const isEditMode = !!changelog;
	const getInitialState = () => ({
		version: '',
		releaseDate: new Date().toISOString().split('T')[0], // Default to today
		title: '',
		notes: '',
	});

	const [formData, setFormData] = useState(getInitialState());
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
			if (isEditMode) {
				setFormData({
					version: changelog.version || '',
					releaseDate: changelog.releaseDate || new Date().toISOString().split('T')[0],
					title: changelog.title || '',
					notes: changelog.notes || '',
				});
			} else {
				setFormData(getInitialState());
			}
		}
	}, [changelog, isEditMode, isOpen]);

	const handleChange = (e) => {
		const { name, value } = e.target;
		setFormData(prev => ({
			...prev,
			[name]: value,
		}));
	};


	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		try {
			const result = changelog
				? await apiClient.put(`/admin/changelogs/${changelog.id}`, formData)
				: await apiClient.post('/admin/changelogs', formData);
			if (result.success) {
				addToast(`Changelog erfolgreich ${changelog ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			}
			else throw new Error(result.message);
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={changelog ? 'Changelog bearbeiten' : 'Neuen Changelog erstellen'}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Version (z.B. 2.1.0)</label>
					<input name="version" value={formData.version} onChange={handleChange} required />
				</div>
				<div className="form-group">
					<label>Titel</label>
					<input name="title" value={formData.title} onChange={handleChange} required />
				</div>
				<div className="form-group">
					<label>Veröffentlichungsdatum</label>
					<input type="date" name="releaseDate" value={formData.releaseDate} onChange={handleChange} required />
				</div>
				<div className="form-group">
					<label>Anmerkungen (Markdown unterstützt)</label>
					<textarea name="notes" value={formData.notes} onChange={handleChange} rows="10" required></textarea>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AdminChangelogPage;