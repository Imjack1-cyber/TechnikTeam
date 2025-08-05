import React, { useState, useCallback, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../../components/ui/Modal';
import { Link } from 'react-router-dom';

const DocumentationModal = ({ isOpen, onClose, onSuccess, doc, allDocs, wikiList }) => {
	const isEditMode = !!doc;
	const getInitialState = () => ({
		pageKey: '',
		title: '',
		pagePath: '',
		features: '',
		relatedPages: '[]',
		adminOnly: false,
		wikiEntryId: null,
		category: 'Sonstiges'
	});
	const [formData, setFormData] = useState(getInitialState());
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
			const docData = isEditMode ? { ...doc } : getInitialState();
			if (docData.wikiEntryId === null || docData.wikiEntryId === 0) {
				docData.wikiEntryId = ""; // Ensure it's an empty string for the select default
			}
			setFormData(docData);
		}
	}, [doc, isEditMode, isOpen]);


	const handleChange = (e) => {
		const { name, value, type, checked } = e.target;
		setFormData(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
	};

	const handleRelatedPagesChange = (e) => {
		const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
		setFormData(prev => ({ ...prev, relatedPages: JSON.stringify(selectedOptions) }));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		try {
			const payload = {
				...formData,
				wikiEntryId: formData.wikiEntryId ? parseInt(formData.wikiEntryId, 10) : null
			};

			const result = isEditMode
				? await apiClient.put(`/admin/documentation/${doc.id}`, payload)
				: await apiClient.post('/admin/documentation', payload);

			if (result.success) {
				addToast('Dokumentation gespeichert', 'success');
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

	const selectedRelatedKeys = JSON.parse(formData.relatedPages || '[]');

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Dokumentation bearbeiten" : "Neue Dokumentation erstellen"}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group"><label>Titel</label><input name="title" value={formData.title} onChange={handleChange} required /></div>
				<div className="form-group"><label>Page Key (e.g., `admin_users`)</label><input name="pageKey" value={formData.pageKey} onChange={handleChange} required disabled={isEditMode} /></div>
				<div className="form-group"><label>Pfad (e.g., `/admin/mitglieder`)</label><input name="pagePath" value={formData.pagePath} onChange={handleChange} required /></div>
				<div className="form-group"><label>Kategorie</label><input name="category" value={formData.category} onChange={handleChange} required placeholder="z.B. Allgemein, Admin: Benutzer" /></div>
				<div className="form-group"><label>Features (Markdown)</label><textarea name="features" value={formData.features} onChange={handleChange} rows="8" required /></div>
				<div className="form-group"><label>Verknüpfte Seiten</label>
					<select multiple value={selectedRelatedKeys} onChange={handleRelatedPagesChange} style={{ height: '150px' }}>
						{allDocs.filter(d => d.id !== doc?.id).map(d => (
							<option key={d.id} value={d.pageKey}>{d.title}</option>
						))}
					</select>
				</div>
				<div className="form-group"><label>Technischer Wiki-Artikel</label>
					<select name="wikiEntryId" value={formData.wikiEntryId || ''} onChange={handleChange}>
						<option value="">(Keine Verknüpfung)</option>
						{wikiList.map(w => (
							<option key={w.id} value={w.id}>{w.filePath}</option>
						))}
					</select>
				</div>
				<div className="form-group"><label><input type="checkbox" name="adminOnly" checked={formData.adminOnly} onChange={handleChange} style={{ width: 'auto', marginRight: '0.5rem' }} />Nur für Admins sichtbar</label></div>
				<button type="submit" className="btn" disabled={isSubmitting}>{isSubmitting ? 'Wird gespeichert...' : 'Speichern'}</button>
			</form>
		</Modal>
	);
};

const AdminDocumentationPage = () => {
	const docsApiCall = useCallback(() => apiClient.get('/admin/documentation'), []);
	const wikiApiCall = useCallback(() => apiClient.get('/wiki/list'), []);

	const { data: docs, loading, error, reload } = useApi(docsApiCall);
	const { data: wikiList, loading: wikiLoading } = useApi(wikiApiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingDoc, setEditingDoc] = useState(null);
	const { addToast } = useToast();

	const openModal = (doc = null) => {
		setEditingDoc(doc);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingDoc(null);
		setIsModalOpen(false);
	};

	const handleDelete = async (doc) => {
		if (window.confirm(`Dokumentation für "${doc.title}" wirklich löschen?`)) {
			try {
				await apiClient.delete(`/admin/documentation/${doc.id}`);
				addToast('Dokumentation gelöscht', 'success');
				reload();
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	return (
		<div>
			<h1><i className="fas fa-book"></i> Anwendungsdokumentation verwalten</h1>
			<div className="table-controls">
				<button className="btn btn-success" onClick={() => openModal()}><i className="fas fa-plus"></i> Neue Seite anlegen</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr><th>Titel</th><th>Kategorie</th><th>Key</th><th>Pfad</th><th>Sichtbarkeit</th><th>Aktionen</th></tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="6">Lade...</td></tr>}
						{error && <tr><td colSpan="6" className="error-message">{error}</td></tr>}
						{docs?.map(doc => (
							<tr key={doc.id}>
								<td>{doc.title}</td>
								<td>{doc.category}</td>
								<td><code>{doc.pageKey}</code></td>
								<td><Link to={doc.pagePath}>{doc.pagePath}</Link></td>
								<td>{doc.adminOnly ? 'Admin' : 'Alle'}</td>
								<td>
									<button onClick={() => openModal(doc)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleDelete(doc)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			{isModalOpen && !wikiLoading && <DocumentationModal isOpen={isModalOpen} onClose={closeModal} onSuccess={() => { closeModal(); reload(); }} doc={editingDoc} allDocs={docs || []} wikiList={wikiList || []} />}
		</div>
	);
};

export default AdminDocumentationPage;