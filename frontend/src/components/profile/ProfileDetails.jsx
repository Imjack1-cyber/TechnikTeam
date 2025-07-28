import React, { useState } from 'react';
import apiClient from '../../services/apiClient';

const ProfileDetails = ({ user, hasPendingRequest, onUpdate }) => {
	const [isEditing, setIsEditing] = useState(false);
	const [formData, setFormData] = useState({
		email: user.email || '',
		classYear: user.classYear || '',
		className: user.className || ''
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const handleEditToggle = () => setIsEditing(!isEditing);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleCancel = () => {
		setFormData({
			email: user.email || '',
			classYear: user.classYear || '',
			className: user.className || ''
		});
		setIsEditing(false);
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		try {
			const result = await apiClient.post('/public/profile/request-change', formData);
			if (result.success) {
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
				onUpdate();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			console.error(err);
		}
	}

	return (
		<div className="card" id="profile-details-container">
			<h2 className="card-title">Stammdaten</h2>
			{hasPendingRequest && (
				<div className="info-message"><i className="fas fa-info-circle"></i> Sie haben eine ausstehende Profiländerung.</div>
			)}
			{error && <div className="error-message">{error}</div>}
			<form onSubmit={handleSubmit}>
				<ul className="details-list">
					<li><strong>Benutzername:</strong> <span>{user.username}</span></li>
					<li><strong>Jahrgang:</strong> <input type="number" name="classYear" value={formData.classYear} onChange={handleChange} readOnly={!isEditing} className="form-group editable-field" /></li>
					<li><strong>Klasse:</strong> <input type="text" name="className" value={formData.className} onChange={handleChange} readOnly={!isEditing} className="form-group editable-field" /></li>
					<li><strong>E-Mail:</strong> <input type="email" name="email" value={formData.email} onChange={handleChange} readOnly={!isEditing} className="form-group editable-field" /></li>
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