import React, { useState } from 'react';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const ProfileDetails = ({ user, hasPendingRequest, onUpdate }) => {
	const [isEditing, setIsEditing] = useState(false);
	const [formData, setFormData] = useState({
		email: user.email || '',
		classYear: user.classYear || '',
		className: user.className || ''
	});
	const [profilePicture, setProfilePicture] = useState(null);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleEditToggle = () => setIsEditing(!isEditing);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleFileChange = (e) => {
		const file = e.target.files[0];
		if (file && file.size > 2 * 1024 * 1024) { // 2MB limit
			setError('Profilbild darf maximal 2MB groß sein.');
			e.target.value = null;
			setProfilePicture(null);
		} else {
			setError('');
			setProfilePicture(file);
		}
	};

	const handleCancel = () => {
		setFormData({
			email: user.email || '',
			classYear: user.classYear || '',
			className: user.className || ''
		});
		setProfilePicture(null);
		setIsEditing(false);
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const data = new FormData();
		data.append('profileData', new Blob([JSON.stringify(formData)], { type: 'application/json' }));
		if (profilePicture) {
			data.append('profilePicture', profilePicture);
		}

		try {
			const result = await apiClient.post('/public/profile/request-change', data);
			if (result.success) {
				addToast('Änderungsantrag erfolgreich eingereicht.', 'success');
				setIsEditing(false);
				onUpdate();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Fehler beim Einreichen der Anfrage.');
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
		<div className="card" id="profile-details-container">
			<div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
				{user.profilePicturePath ? (
					<img src={`/api/v1/public/files/avatars/user/${user.id}`} alt="Profilbild" style={{ width: '80px', height: '80px', borderRadius: '50%', objectFit: 'cover' }} />
				) : (
					<i className="fas fa-user-circle" style={{ fontSize: '80px', color: 'var(--text-muted-color)' }}></i>
				)}
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
							<strong>Profilbild ändern:</strong>
							<input type="file" name="profilePicture" onChange={handleFileChange} accept="image/jpeg, image/png" />
						</li>
					)}
				</ul>
				{!hasPendingRequest && (
					<div style={{ marginTop: '1.5rem', display: 'flex', gap: '0.5rem' }}>
						{!isEditing ? (
							<button type="button" onClick={handleEditToggle} className="btn btn-secondary">Profil bearbeiten</button>
						) : (
							<>
								<button type="submit" className="btn btn-success" disabled={isSubmitting}>{isSubmitting ? 'Wird gesendet...' : 'Antrag einreichen'}</button>
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
	);
};

export default ProfileDetails;