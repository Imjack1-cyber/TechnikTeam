import React from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../../services/apiClient';
import { passkeyService } from '../../services/passkeyService';
import { useToast } from '../../context/ToastContext';

const ProfileSecurity = ({ passkeys, onUpdate }) => {
	const { addToast } = useToast();

	const handleRegisterPasskey = async () => {
		const deviceName = prompt('Bitte geben Sie einen Namen für dieses Gerät ein (z.B. "Mein Laptop"):', 'Mein Gerät');
		if (!deviceName) return;

		try {
			await passkeyService.registerPasskey(deviceName);
			addToast('Passkey erfolgreich registriert!', 'success');
			onUpdate();
		} catch (error) {
			console.error(error);
			addToast(`Fehler bei der Registrierung: ${error.message}`, 'error');
		}
	};

	const handleDeletePasskey = async (id) => {
		if (window.confirm('Diesen Passkey wirklich entfernen?')) {
			try {
				const result = await apiClient.delete(`/public/profile/passkeys/${id}`);
				if (result.success) {
					addToast('Passkey erfolgreich entfernt.', 'success');
					onUpdate();
				} else {
					throw new Error(result.message);
				}
			} catch (error) {
				console.error(error);
				addToast(`Fehler beim Entfernen: ${error.message}`, 'error');
			}
		}
	};

	return (
		<div className="card" id="profile-security-container">
			<h2 className="card-title">Sicherheit</h2>
			<ul className="details-list">
				<li>
					<Link to="/passwort" className="btn btn-secondary">Passwort ändern</Link>
				</li>
			</ul>
			<h3 style={{ marginTop: '1.5rem', fontSize: '1.1rem' }}>Passkeys (Passwortloser Login)</h3>
			<p>Registrieren Sie Geräte für einen schnellen und sicheren Login ohne Passwort.</p>
			<button onClick={handleRegisterPasskey} className="btn btn-success" style={{ marginBottom: '1rem' }}>
				<i className="fas fa-plus-circle"></i> Neues Gerät registrieren
			</button>
			<ul className="details-list">
				{passkeys.length === 0 ? (
					<li>Keine Passkeys registriert.</li>
				) : (
					passkeys.map(key => (
						<li key={key.id}>
							<span>
								<i className="fas fa-key"></i> {key.name}
								<small style={{ display: 'block', color: 'var(--text-muted-color)' }}>
									Registriert am: {new Date(key.createdAt).toLocaleDateString('de-DE')}
								</small>
							</span>
							<button onClick={() => handleDeletePasskey(key.id)} className="btn btn-small btn-danger-outline">Entfernen</button>
						</li>
					))
				)}
			</ul>
		</div>
	);
};

export default ProfileSecurity;