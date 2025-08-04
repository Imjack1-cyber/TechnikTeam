import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useToast } from '../../context/ToastContext';
import { passkeyService } from '../../services/passkeyService';

// To make this component work, ensure you have installed the required client-side library:
// npm install @simplewebauthn/browser

const ProfileSecurity = ({ passkeys, onUpdate }) => {
	const [isLoading, setIsLoading] = useState(false);
	const { addToast } = useToast();

	const handleRegisterNewDevice = async () => {
		const deviceName = prompt("Bitte geben Sie einen Namen für dieses Gerät ein (z.B. 'Mein Laptop'):");
		if (!deviceName) {
			return;
		}
		setIsLoading(true);
		try {
			const success = await passkeyService.register(deviceName);
			if (success) {
				addToast('Neues Gerät/Passkey erfolgreich registriert.', 'success');
				onUpdate();
			} else {
				throw new Error("Die Registrierung ist fehlgeschlagen.");
			}
		} catch (err) {
			console.error(err);
			addToast(err.message || 'Passkey-Registrierung fehlgeschlagen.', 'error');
		} finally {
			setIsLoading(false);
		}
	};

	const handleDeletePasskey = async (credential) => {
		if (window.confirm(`Passkey "${credential.name}" wirklich von Ihrem Konto entfernen?`)) {
			try {
				const success = await passkeyService.deleteCredential(credential.id);
				if (success) {
					addToast('Passkey erfolgreich entfernt.', 'success');
					onUpdate();
				} else {
					throw new Error("Das Löschen ist fehlgeschlagen.");
				}
			} catch (err) {
				console.error(err);
				addToast(err.message || 'Passkey konnte nicht entfernt werden.', 'error');
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

			{passkeys && passkeys.length > 0 ? (
				<ul className="details-list">
					{passkeys.map(pk => (
						<li key={pk.id}>
							<span><i className="fas fa-key"></i> {pk.name}</span>
							<button className="btn btn-small btn-danger" onClick={() => handleDeletePasskey(pk)}>Entfernen</button>
						</li>
					))}
				</ul>
			) : (
				<p>Sie haben noch keine Passkeys registriert.</p>
			)}

			<button className="btn btn-success" onClick={handleRegisterNewDevice} disabled={isLoading} style={{ marginTop: '1rem' }}>
				{isLoading ? 'Warte auf Gerät...' : <><i className="fas fa-plus-circle"></i> Neues Gerät registrieren</>}
			</button>
		</div>
	);
};

export default ProfileSecurity;