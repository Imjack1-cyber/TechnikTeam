import React, { useState, useCallback, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AchievementKeyDisplay = ({ achievementKey }) => {
	const parts = achievementKey.split('_');
	const [trigger, action, condition] = parts;

	const descriptions = {
		trigger: "Trigger-Bereich: In welchem Teil der Anwendung wird das Abzeichen ausgelöst? (z.B. EVENT)",
		action: "Aktion: Welche Benutzeraktion wird gezählt? (z.B. PARTICIPANT, LEADER)",
		condition: "Bedingung: Welcher Schwellenwert muss erreicht werden? (z.B. 1, 5, 10)"
	};

	return (
		<div style={{ display: 'flex', gap: '0.25rem', fontFamily: 'monospace', flexWrap: 'wrap' }}>
			<span title={descriptions.trigger} style={{ padding: '0.1rem 0.4rem', background: 'var(--primary-color-light)', color: 'var(--primary-color)', borderRadius: '4px', border: '1px solid var(--primary-color)' }}>{trigger}</span>
			<span title={descriptions.action} style={{ padding: '0.1rem 0.4rem', background: 'var(--bg-color)', borderRadius: '4px', border: '1px solid var(--border-color)' }}>{action}</span>
			{condition && <span title={descriptions.condition} style={{ padding: '0.1rem 0.4rem', background: 'var(--bg-color)', borderRadius: '4px', border: '1px solid var(--border-color)' }}>{condition}</span>}
		</div>
	);
};

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
								<td><AchievementKeyDisplay achievementKey={ach.achievementKey} /></td>
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
						<div className="card-row"><strong>Schlüssel:</strong> <AchievementKeyDisplay achievementKey={ach.achievementKey} /></div>
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
	const [achievementKey, setAchievementKey] = useState('');

	const formDataApiCall = useCallback(() => apiClient.get('/form-data/achievements'), []);
	const { data: formData } = useApi(formDataApiCall);

	const [keyParts, setKeyParts] = useState({ trigger: 'EVENT', action: '', condition: '' });

	useEffect(() => {
		if (isOpen) {
			if (achievement) {
				setAchievementKey(achievement.achievementKey);
				const parts = achievement.achievementKey.split('_');
				setKeyParts({
					trigger: parts[0] || 'EVENT',
					action: parts[1] || '',
					condition: parts[2] || ''
				});
			} else {
				setAchievementKey('EVENT__');
				setKeyParts({ trigger: 'EVENT', action: '', condition: '' });
			}
		}
	}, [achievement, isOpen]);

	const handleKeyPartChange = (part, value) => {
		const newKeyParts = { ...keyParts, [part]: value };
		if (part === 'trigger') {
			newKeyParts.condition = ''; // Reset condition when trigger changes
		}
		setKeyParts(newKeyParts);
		const { trigger, action, condition } = newKeyParts;
		setAchievementKey(`${trigger}_${action}_${condition}`.toUpperCase());
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		const form = new FormData(e.target);
		const data = Object.fromEntries(form.entries());
		data.achievementKey = achievementKey;

		try {
			const result = achievement
				? await apiClient.put(`/achievements/${achievement.id}`, data)
				: await apiClient.post('/achievements', data);
			if (result.success) {
				addToast(`Abzeichen erfolgreich ${achievement ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
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
					<label>Achievement Schlüssel</label>
					<div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.5rem', marginBottom: '0.5rem' }}>
						<div>
							<label htmlFor="key-trigger" style={{ fontSize: '0.8rem' }}>Bereich</label>
							<select id="key-trigger" value={keyParts.trigger} onChange={e => handleKeyPartChange('trigger', e.target.value)} className="form-group">
								<option value="EVENT">Event</option>
								<option value="QUALIFICATION">Qualifikation</option>
							</select>
						</div>
						<div>
							<label htmlFor="key-action" style={{ fontSize: '0.8rem' }}>Aktion</label>
							<select id="key-action" value={keyParts.action} onChange={e => handleKeyPartChange('action', e.target.value)} className="form-group" required>
								<option value="">-- Aktion --</option>
								{keyParts.trigger === 'EVENT' ? (
									<>
										<option value="PARTICIPANT">Teilnahme</option>
										<option value="LEADER">Leitung</option>
									</>
								) : (
									<option value="GAINED">Erhalten</option>
								)}
							</select>
						</div>
						<div>
							<label htmlFor="key-condition" style={{ fontSize: '0.8rem' }}>Bedingung</label>
							{keyParts.trigger === 'QUALIFICATION' ? (
								<select id="key-condition" value={keyParts.condition} onChange={e => handleKeyPartChange('condition', e.target.value)} className="form-group" required>
									<option value="">-- Kurs --</option>
									{formData?.courses?.map(course => <option key={course.id} value={course.abbreviation}>{course.name}</option>)}
								</select>
							) : (
								<input id="key-condition" type="number" value={keyParts.condition} onChange={e => handleKeyPartChange('condition', e.target.value)} className="form-group" placeholder="Anzahl" min="1" required />
							)}
						</div>
					</div>
					<input type="text" name="achievementKey" value={achievementKey} readOnly disabled style={{ fontFamily: 'monospace', background: 'var(--bg-color)' }} />
				</div>
				<div className="form-group">
					<label htmlFor="modal-desc">Beschreibung</label>
					<textarea id="modal-desc" name="description" defaultValue={achievement?.description} rows="3" required></textarea>
				</div>
				<div className="form-group">
					<label htmlFor="modal-icon">
						Font Awesome Icon-Klasse
						<a href="https://fontawesome.com/search?m=free&s=solid" target="_blank" rel="noopener noreferrer" style={{ marginLeft: '0.5rem', fontSize: '0.8rem' }}>
							<i className="fas fa-search"></i> Icons suchen
						</a>
					</label>
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