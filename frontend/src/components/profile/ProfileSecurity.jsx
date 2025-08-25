import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import TwoFactorAuthSetup from './TwoFactorAuthSetup';
import { useAuthStore } from '../../store/authStore';

const ProfileSecurity = ({ onUpdate }) => {
	const user = useAuthStore(state => state.user);
	const { addToast } = useToast();
	const [is2faModalOpen, setIs2faModalOpen] = useState(false);

	const handleDisable2FA = async () => {
		const code = prompt("Um 2FA zu deaktivieren, geben Sie bitte einen aktuellen Code aus Ihrer Authenticator-App ein.");
		if (code) {
			try {
				const result = await apiClient.post('/public/profile/2fa/disable', { token: code });
				if (result.success) {
					addToast('Zwei-Faktor-Authentifizierung erfolgreich deaktiviert.', 'success');
					onUpdate();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler: ${err.message}`, 'error');
			}
		}
	};

	const handleSetupComplete = () => {
		setIs2faModalOpen(false);
		onUpdate(); // Reload profile data to get the new 2FA status
	};

	return (
		<>
			<div className="card" id="profile-security-container">
				<h2 className="card-title">Sicherheit</h2>
				<ul className="details-list">
					<li>
						<Link to="/passwort" className="btn btn-secondary">Passwort ändern</Link>
					</li>
					<li>
						<strong>Zwei-Faktor-Authentifizierung (2FA)</strong>
						{user.totpEnabled ? (
							<button onClick={handleDisable2FA} className="btn btn-danger-outline">2FA Deaktivieren</button>
						) : (
							<button onClick={() => setIs2faModalOpen(true)} className="btn btn-success">2FA Aktivieren</button>
						)}
					</li>
				</ul>
				<h3 style={{ marginTop: '1.5rem', fontSize: '1.1rem' }}>Passkeys (Passwortloser Login)</h3>
				<p className="text-muted">
					Dieses Feature wird zurzeit überarbeitet und ist in Kürze wieder verfügbar.
				</p>
				<button className="btn btn-success" disabled={true} style={{ marginBottom: '1rem' }}>
					<i className="fas fa-plus-circle"></i> Neues Gerät registrieren
				</button>
			</div>
			<Modal
				isOpen={is2faModalOpen}
				onClose={() => setIs2faModalOpen(false)}
				title="Zwei-Faktor-Authentifizierung einrichten"
			>
				<TwoFactorAuthSetup onSetupComplete={handleSetupComplete} />
			</Modal>
		</>
	);
};

export default ProfileSecurity;