import React, { useState } from 'react';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';

const ConfirmationModal = ({ isOpen, onClose, onConfirm, changes, isSubmitting }) => {
	if (!isOpen) return null;

	const changeLabels = {
		email: 'E-Mail',
		classYear: 'Jahrgang',
		className: 'Klasse',
		profileIconClass: 'Profil-Icon'
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Änderungen bestätigen">
			<p>Bitte überprüfen Sie die folgenden Änderungen. Diese müssen von einem Administrator genehmigt werden.</p>
			<ul className="details-list" style={{ marginTop: '1rem', marginBottom: '1.5rem' }}>
				{Object.entries(changes).map(([key, values]) => (
					<li key={key}>
						<strong>{changeLabels[key] || key}</strong>
						<div>
							<span style={{ textDecoration: 'line-through', color: 'var(--danger-color)', marginRight: '0.5rem' }}>{values.oldVal || 'Nicht gesetzt'}</span>
							<span>→</span>
							<strong style={{ color: 'var(--success-color)', marginLeft: '0.5rem' }}>{values.newVal || 'Wird entfernt'}</strong>
						</div>
					</li>
				))}
			</ul>
			<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
				<button type="button" onClick={onClose} className="btn btn-secondary" disabled={isSubmitting}>Abbrechen</button>
				<button type="button" onClick={onConfirm} className="btn btn-success" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gesendet...' : 'Bestätigen & Senden'}
				</button>
			</div>
		</Modal>
	);
};


const ProfileDetails = ({ user, hasPendingRequest, onUpdate }) => {
	const [isEditing, setIsEditing] = useState(false);
	const [formData, setFormData] = useState({
		email: user.email || '',
		classYear: user.classYear || '',
		className: user.className || '',
		profileIconClass: user.profileIconClass || 'fa-user-circle',
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
	const [detectedChanges, setDetectedChanges] = useState({});

	const handleEditToggle = () => setIsEditing(!isEditing);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleCancel = () => {
		setFormData({
			email: user.email || '',
			classYear: user.classYear || '',
			className: user.className || '',
			profileIconClass: user.profileIconClass || 'fa-user-circle',
		});
		setIsEditing(false);
		setError('');
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setError('');

		const changes = {};
		if (formData.email !== (user.email || '')) {
			changes.email = { oldVal: user.email, newVal: formData.email };
		}
		if (formData.classYear.toString() !== (user.classYear || '').toString()) {
			changes.classYear = { oldVal: user.classYear, newVal: formData.classYear };
		}
		if (formData.className !== (user.className || '')) {
			changes.className = { oldVal: user.className, newVal: formData.className };
		}
		if (formData.profileIconClass !== (user.profileIconClass || '')) {
			changes.profileIconClass = { oldVal: user.profileIconClass, newVal: formData.profileIconClass };
		}

		if (Object.keys(changes).length === 0) {
			addToast('Keine Änderungen vorgenommen.', 'info');
			return;
		}

		setDetectedChanges(changes);
		setIsConfirmModalOpen(true);
	};

	const handleConfirmSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		try {
			// No longer sending form data, just the JSON payload
			const result = await apiClient.post('/public/profile/request-change', formData);
			if (result.success) {
				addToast('Änderungsantrag erfolgreich eingereicht.', 'success');
				setIsEditing(false);
				setIsConfirmModalOpen(false);
				onUpdate();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Fehler beim Einreichen der Anfrage.');
			setIsConfirmModalOpen(false);
		} finally {
			setIsSubmitting(false);
		}
	};


	const handleColorSubmit = async (e) => {
		e.preventDefault();
		const newColor = e.target.elements.chatColor.value;
		try {
			const result = await apiClient.put('/public/profile/chat-color', { chatColor: newColor });
			if (result.success) {
				addToast('Chat-Farbe gespeichert', 'success');
				onUpdate();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message || 'Fehler beim Speichern', 'error');
		}
	}

	return (
		<>
			<div className="card" id="profile-details-container">
				<div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
					<i className={`fas ${user.profileIconClass || 'fa-user-circle'}`} style={{ fontSize: '80px', color: 'var(--text-muted-color)', width: '80px', textAlign: 'center' }}></i>
					<h2 className="card-title" style={{ border: 'none', padding: 0, margin: 0 }}>Stammdaten</h2>
				</div>

				{hasPendingRequest && (
					<div className="info-message"><i className="fas fa-info-circle"></i> Sie haben eine ausstehende Profiländerung.</div>
				)}
				{error && <div className="error-message">{error}</div>}
				<form onSubmit={handleSubmit}>
					<ul className="details-list">
						<li><strong>Benutzername:</strong> <span>{user.username}</span></li>
						<li><strong>Jahrgang:</strong> <input type="number" name="classYear" value={formData.classYear} onChange={handleChange} readOnly={!isEditing} style={{ border: isEditing ? '' : 'none', background: isEditing ? '' : 'transparent' }} /></li>
						<li><strong>Klasse:</strong> <input type="text" name="className" value={formData.className} onChange={handleChange} readOnly={!isEditing} style={{ border: isEditing ? '' : 'none', background: isEditing ? '' : 'transparent' }} /></li>
						<li><strong>E-Mail:</strong> <input type="email" name="email" value={formData.email} onChange={handleChange} readOnly={!isEditing} style={{ border: isEditing ? '' : 'none', background: isEditing ? '' : 'transparent' }} /></li>
						{isEditing && (
							<li>
								<label htmlFor="profileIconClass" style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
									<span>
										<strong>Profil-Icon:</strong>
										<a href="https://fontawesome.com/search?m=free&s=solid" target="_blank" rel="noopener noreferrer" style={{ marginLeft: '0.5rem', fontSize: '0.8rem', fontWeight: 'normal' }}>
											<i className="fas fa-search"></i> Icons suchen
										</a>
									</span>
									<input type="text" id="profileIconClass" name="profileIconClass" value={formData.profileIconClass} onChange={handleChange} placeholder="z.B. fa-user-ninja" style={{ maxWidth: '200px' }} />
								</label>
							</li>
						)}
					</ul>
					{!hasPendingRequest && (
						<div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.5rem' }}>
							{!isEditing ? (
								<button type="button" onClick={handleEditToggle} className="btn btn-secondary">Profil bearbeiten</button>
							) : (
								<>
									<button type="submit" className="btn btn-success" disabled={isSubmitting}>Änderungen überprüfen</button>
									<button type="button" onClick={handleCancel} className="btn" style={{ backgroundColor: 'var(--text-muted-color)' }}>Abbrechen</button>
								</>
							)}
						</div>
					)}
				</form>
				<hr style={{ margin: '1.5rem 0' }} />
				<ul className="details-list">
					<li style={{ alignItems: 'center', gap: '1rem' }}>
						<strong>Chat-Farbe:</strong>
						<form onSubmit={handleColorSubmit} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
							<input type="color" name="chatColor" defaultValue={user.chatColor || '#E9ECEF'} title="Wähle deine Chat-Farbe" />
							<button type="submit" className="btn btn-small">Speichern</button>
						</form>
					</li>
				</ul>
			</div>
			<ConfirmationModal
				isOpen={isConfirmModalOpen}
				onClose={() => setIsConfirmModalOpen(false)}
				onConfirm={handleConfirmSubmit}
				changes={detectedChanges}
				isSubmitting={isSubmitting}
			/>
		</>
	);
};

export default ProfileDetails;