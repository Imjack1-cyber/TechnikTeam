import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminAchievementsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/achievements'), []);
	const { data: achievements, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingAchievement, setEditingAchievement] = useState(null);
	const { addToast } = useToast();

	const openModal = (achievement = null) => {
		setEditingAchievement(achievement);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingAchievement(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (ach) => {
		if (window.confirm(`Abzeichen "${ach.name}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/achievements/${ach.id}`);
				if (result.success) {
					addToast('Abzeichen erfolgreich gelöscht', 'success');
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
			<h1><i className="fas fa-award"></i> Abzeichen verwalten</h1>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neues Abzeichen
				</button>
			</div>

			{loading && <p>Lade Abzeichen...</p>}
			{error && <p className="error-message">{error}</p>}

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Icon</th>
							<th>Name</th>
							<th>Schlüssel</th>
							<th>Beschreibung</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{achievements?.map(ach => (
							<tr key={ach.id}>
								<td><i className={`fas ${ach.iconClass}`} style={{ fontSize: '1.5rem' }}></i></td>
								<td>{ach.name}</td>
								<td><code>{ach.achievementKey}</code></td>
								<td>{ach.description}</td>
								<td>
									<button onClick={() => openModal(ach)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleDelete(ach)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			<div className="mobile-card-list">
				{achievements?.map(ach => (
					<div className="list-item-card" key={ach.id}>
						<h3 className="card-title"><i className={`fas ${ach.iconClass}`}></i> {ach.name}</h3>
						<div className="card-row"><strong>Schlüssel:</strong> <code>{ach.achievementKey}</code></div>
						<p style={{ marginTop: '0.5rem' }}>{ach.description}</p>
						<div className="card-actions">
							<button onClick={() => openModal(ach)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleDelete(ach)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>
			{isModalOpen && (
				<AchievementModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					achievement={editingAchievement}
				/>
			)}
		</div>
	);
};

const AchievementModal = ({ isOpen, onClose, onSuccess, achievement }) => {
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
			const result = achievement
				? await apiClient.put(`/achievements/${achievement.id}`, data)
				: await apiClient.post('/achievements', data);
			if (result.success) {
				addToast(`Abzeichen erfolgreich ${achievement ? 'aktualisiert' : 'erstellt'}.`, 'success');
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
		<Modal isOpen={isOpen} onClose={onClose} title={achievement ? 'Abzeichen bearbeiten' : 'Neues Abzeichen erstellen'}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="modal-name">Name</label>
					<input id="modal-name" name="name" defaultValue={achievement?.name} required />
				</div>
				<div className="form-group">
					<label htmlFor="modal-key">Achievement Schlüssel</label>
					<input id="modal-key" name="achievementKey" defaultValue={achievement?.achievementKey} required pattern="[A-Z0-9_]+" title="Nur Großbuchstaben, Zahlen und Unterstriche" />
				</div>
				<div className="form-group">
					<label htmlFor="modal-desc">Beschreibung</label>
					<textarea id="modal-desc" name="description" defaultValue={achievement?.description} rows="3" required></textarea>
				</div>
				<div className="form-group">
					<label htmlFor="modal-icon">Font Awesome Icon-Klasse</label>
					<input id="modal-icon" name="iconClass" defaultValue={achievement?.iconClass} placeholder="z.B. fa-star" required />
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AdminAchievementsPage;