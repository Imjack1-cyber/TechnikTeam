import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminChecklistTemplatesPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);
	const { data: templates, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingTemplate, setEditingTemplate] = useState(null);
	const { addToast } = useToast();

	const openModal = (template = null) => {
		setEditingTemplate(template);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingTemplate(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (template) => {
		if (window.confirm(`Vorlage "${template.name}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/checklist-templates/${template.id}`);
				if (result.success) {
					addToast('Vorlage gelöscht', 'success');
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
			<h1><i className="fas fa-tasks"></i> Pre-Flight Checklisten-Vorlagen</h1>
			<p>Verwalten Sie hier Vorlagen für wiederverwendbare Checklisten (z.B. für Standard-Setups).</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neue Vorlage erstellen
				</button>
			</div>
			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name</th>
							<th>Beschreibung</th>
							<th>Anzahl Items</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="4">Lade Vorlagen...</td></tr>}
						{error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
						{templates?.map(template => (
							<tr key={template.id}>
								<td>{template.name}</td>
								<td>{template.description}</td>
								<td>{template.items?.length || 0}</td>
								<td>
									<button onClick={() => openModal(template)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleDelete(template)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			{isModalOpen && (
				<TemplateModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					template={editingTemplate}
				/>
			)}
		</div>
	);
};

const TemplateModal = ({ isOpen, onClose, onSuccess, template }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [items, setItems] = useState(template?.items || [{ itemText: '' }]);
	const { addToast } = useToast();

	const handleItemChange = (index, value) => {
		const newItems = [...items];
		newItems[index].itemText = value;
		setItems(newItems);
	};

	const handleAddItem = () => setItems([...items, { itemText: '' }]);
	const handleRemoveItem = (index) => setItems(items.filter((_, i) => i !== index));

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const formData = new FormData(e.target);
		const data = {
			name: formData.get('name'),
			description: formData.get('description'),
			items: items.filter(item => item.itemText.trim() !== '')
		};

		try {
			const result = template
				? await apiClient.put(`/admin/checklist-templates/${template.id}`, data)
				: await apiClient.post('/admin/checklist-templates', data);
			if (result.success) {
				addToast(`Vorlage erfolgreich ${template ? 'aktualisiert' : 'erstellt'}.`, 'success');
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
		<Modal isOpen={isOpen} onClose={onClose} title={template ? 'Vorlage bearbeiten' : 'Neue Vorlage erstellen'}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Name der Vorlage</label>
					<input name="name" defaultValue={template?.name} required />
				</div>
				<div className="form-group">
					<label>Beschreibung</label>
					<textarea name="description" defaultValue={template?.description} rows="2"></textarea>
				</div>
				<div className="form-group">
					<label>Checklisten-Punkte</label>
					{items.map((item, index) => (
						<div className="dynamic-row" key={index}>
							<input
								value={item.itemText}
								onChange={e => handleItemChange(index, e.target.value)}
								placeholder={`Punkt #${index + 1}`}
								className="form-group"
							/>
							<button type="button" onClick={() => handleRemoveItem(index)} className="btn btn-small btn-danger">×</button>
						</div>
					))}
					<button type="button" onClick={handleAddItem} className="btn btn-small btn-secondary" style={{ marginTop: '0.5rem' }}>Punkt hinzufügen</button>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AdminChecklistTemplatesPage;