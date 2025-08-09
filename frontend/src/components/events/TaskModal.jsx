import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const TaskModal = ({ isOpen, onClose, onSuccess, event, task, allUsers }) => {
	const isEditMode = !!task;
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [formData, setFormData] = useState({
		description: '',
		details: '',
		status: 'OFFEN',
		assignedUserIds: [],
	});

	useEffect(() => {
		if (isOpen) {
			console.log("[TaskModal] Opening modal. Is edit mode:", isEditMode, "Task data:", task);
			if (isEditMode) {
				setFormData({
					description: task.description || '',
					details: task.details || '',
					status: task.status || 'OFFEN',
					assignedUserIds: task.assignedUsers?.map(u => u.id) || [],
				});
			} else {
				setFormData({
					description: '',
					details: '',
					status: 'OFFEN',
					assignedUserIds: [],
				});
			}
		}
	}, [task, isEditMode, isOpen]);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleMultiSelectChange = (e) => {
		const options = [...e.target.selectedOptions];
		const values = options.map(option => parseInt(option.value, 10));
		setFormData(prev => ({ ...prev, assignedUserIds: values }));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			id: task?.id || 0,
			description: formData.description,
			details: formData.details,
			status: formData.status,
			// The backend expects assignedUsers array, not just IDs
			assignedUsers: formData.assignedUserIds.map(id => ({ id })),
			// Stub other fields for now as they are not editable in this modal
			dependsOn: task?.dependsOn || [],
			requiredItems: task?.requiredItems || [],
			requiredKits: task?.requiredKits || [],
			requiredPersons: task?.requiredPersons || 0,
			displayOrder: task?.displayOrder || 0,
		};

		console.log('[TaskModal] Submitting task payload to API:', payload);

		try {
			const result = await apiClient.post(`/events/${event.id}/tasks`, payload);
			console.log('[TaskModal] API response received:', result);
			if (result.success) {
				addToast(`Aufgabe erfolgreich ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			console.error('[TaskModal] Task submission failed:', err);
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Aufgabe bearbeiten" : "Neue Aufgabe erstellen"}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Beschreibung (Titel)</label>
					<input name="description" value={formData.description} onChange={handleChange} required />
				</div>
				<div className="form-group">
					<label>Details (Markdown unterstützt)</label>
					<textarea name="details" value={formData.details} onChange={handleChange} rows="5" />
				</div>
				<div className="form-group">
					<label>Status</label>
					<select name="status" value={formData.status} onChange={handleChange}>
						<option value="OFFEN">Offen</option>
						<option value="IN_ARBEIT">In Arbeit</option>
						<option value="ERLEDIGT">Erledigt</option>
					</select>
				</div>
				<div className="form-group">
					<label>Zugewiesen an (halten Sie Strg/Cmd gedrückt, um mehrere auszuwählen)</label>
					<select name="assignedUserIds" multiple value={formData.assignedUserIds} onChange={handleMultiSelectChange} style={{ height: '120px' }}>
						{allUsers?.map(user => (
							<option key={user.id} value={user.id}>{user.username}</option>
						))}
					</select>
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Aufgabe speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default TaskModal;