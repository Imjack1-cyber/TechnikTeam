import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';

const PasswordPage = () => {
	const [currentPassword, setCurrentPassword] = useState('');
	const [newPassword, setNewPassword] = useState('');
	const [confirmPassword, setConfirmPassword] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const [success, setSuccess] = useState('');

	const handleSubmit = async (e) => {
		e.preventDefault();
		setError('');
		setSuccess('');

		if (newPassword !== confirmPassword) {
			setError('Die neuen Passwörter stimmen nicht überein.');
			return;
		}

		setIsLoading(true);

		try {
			const result = await apiClient.put('/public/profile/password', {
				currentPassword,
				newPassword,
				confirmPassword
			});

			if (result.success) {
				setSuccess('Ihr Passwort wurde erfolgreich geändert.');
				setCurrentPassword('');
				setNewPassword('');
				setConfirmPassword('');
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div style={{ maxWidth: '600px', margin: 'auto' }}>
			<div className="card">
				<h1>Passwort ändern</h1>
				<p className="text-muted" style={{ marginTop: '-1rem', marginBottom: '1.5rem' }}>
					Das neue Passwort muss mindestens 10 Zeichen lang sein und Groß-, Kleinbuchstaben, Zahlen und Sonderzeichen enthalten.
				</p>

				{error && <p className="error-message">{error}</p>}
				{success && <p className="success-message">{success}</p>}

				<form onSubmit={handleSubmit}>
					<div className="form-group">
						<label htmlFor="currentPassword">Aktuelles Passwort</label>
						<input type="password" id="currentPassword" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required autoComplete="current-password" />
					</div>
					<div className="form-group">
						<label htmlFor="newPassword">Neues Passwort</label>
						<input type="password" id="newPassword" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required autoComplete="new-password" />
					</div>
					<div className="form-group">
						<label htmlFor="confirmPassword">Neues Passwort bestätigen</label>
						<input type="password" id="confirmPassword" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required autoComplete="new-password" />
					</div>
					<button type="submit" className="btn" disabled={isLoading}>
						{isLoading ? <><i className="fas fa-spinner fa-spin"></i> Speichern...</> : <><i className="fas fa-save"></i> Passwort speichern</>}
					</button>
					<Link to="/profil" className="btn btn-secondary" style={{ marginLeft: '1rem' }}>Zurück zum Profil</Link>
				</form>
			</div>
		</div>
	);
};

export default PasswordPage;