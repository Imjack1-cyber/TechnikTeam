import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';

const TwoFactorAuthForm = ({ username, preAuthToken, onAuthSuccess }) => {
	const [token, setToken] = useState('');
	const [backupCode, setBackupCode] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setError('');

		try {
			// Set the temporary token for this one request
			apiClient.setAuthToken(preAuthToken);
			const result = await apiClient.post('/auth/verify-2fa', { token, backupCode });
			if (result.success && result.data.token) {
				onAuthSuccess(result.data.token);
			} else {
				throw new Error(result.message || 'Verifizierung fehlgeschlagen.');
			}
		} catch (err) {
			setError(err.message);
		} finally {
			setIsLoading(false);
			apiClient.setAuthToken(null); // Unset the temporary token
		}
	};

	return (
		<div>
			<h3>Zwei-Faktor-Authentifizierung</h3>
			<p>Login für <strong>{username}</strong> von einem neuen Standort. Bitte geben Sie Ihren Code ein.</p>
			{error && <p className="error-message">{error}</p>}
			<form onSubmit={handleSubmit}>
				<div className="form-group">
					<label htmlFor="2fa-token">Authenticator-Code</label>
					<input
						type="text"
						id="2fa-token"
						value={token}
						onChange={(e) => setToken(e.target.value)}
						placeholder="6-stelliger Code"
						autoFocus
					/>
				</div>
				<div className="form-group">
					<label htmlFor="2fa-backup">oder Backup-Code</label>
					<input
						type="text"
						id="2fa-backup"
						value={backupCode}
						onChange={(e) => setBackupCode(e.target.value)}
						placeholder="8-stelliger Code"
					/>
				</div>
				<button type="submit" className="btn" style={{ width: '100%' }} disabled={isLoading}>
					{isLoading ? 'Wird geprüft...' : 'Bestätigen'}
				</button>
			</form>
		</div>
	);
};

const LoginPage = () => {
	const [username, setUsername] = useState('');
	const [password, setPassword] = useState('');
	const [isPasswordVisible, setIsPasswordVisible] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const navigate = useNavigate();
	const location = useLocation();

	// State for 2FA flow
	const [preAuthToken, setPreAuthToken] = useState(null);

	const { login, isAuthenticated, completeLoginWithToken } = useAuthStore();
	const { addToast } = useToast();

	useEffect(() => {
		if (isAuthenticated) {
			const from = location.state?.from?.pathname || '/home';
			navigate(from, { replace: true });
		}
	}, [isAuthenticated, navigate, location.state]);

	const handleAuthSuccess = async (finalToken) => {
		await completeLoginWithToken(finalToken);
		addToast('Erfolgreich angemeldet!', 'success');
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setError('');
		try {
			const result = await login(username, password);
			if (result.status === '2FA_REQUIRED') {
				setPreAuthToken(result.token);
			}
		} catch (err) {
			console.error(err);
			setError(err.message || 'Login fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.');
		} finally {
			setIsLoading(false);
		}
	};

	const togglePasswordVisibility = () => {
		setIsPasswordVisible(!isPasswordVisible);
	};

	if (preAuthToken) {
		return (
			<div className="login-page-container">
				<div className="login-box">
					<TwoFactorAuthForm
						username={username}
						preAuthToken={preAuthToken}
						onAuthSuccess={handleAuthSuccess}
					/>
				</div>
			</div>
		);
	}

	return (
		<div className="login-page-container">
			<div className="login-box">
				<h1>
					<i className="fas fa-bolt"></i> TechnikTeam
				</h1>
				{error && <p className="error-message">{error}</p>}
				<form id="login-form" onSubmit={handleSubmit}>
					<div className="form-group">
						<label htmlFor="username">Benutzername</label>
						<input
							type="text"
							id="username"
							name="username"
							value={username}
							onChange={(e) => setUsername(e.target.value)}
							required
							autoComplete="username"
							autoFocus
							disabled={isLoading}
						/>
					</div>
					<div className="form-group">
						<label htmlFor="password">Passwort</label>
						<div className="password-input-wrapper">
							<input
								type={isPasswordVisible ? 'text' : 'password'}
								id="password"
								name="password"
								value={password}
								onChange={(e) => setPassword(e.target.value)}
								required
								autoComplete="current-password"
								disabled={isLoading}
							/>
							<span className="password-toggle-icon" onClick={togglePasswordVisibility} title="Passwort anzeigen/verbergen">
								<i className={`fas ${isPasswordVisible ? 'fa-eye-slash' : 'fa-eye'}`}></i>
							</span>
						</div>
					</div>
					<button
						type="submit"
						className="btn"
						style={{ width: '100%', marginBottom: '0.75rem' }}
						disabled={isLoading}
					>
						{isLoading ? (
							<>
								<i className="fas fa-spinner fa-spin"></i> Anmelden...
							</>
						) : (
							'Anmelden'
						)}
					</button>
					<button
						type="button"
						className="btn btn-secondary"
						style={{ width: '100%' }}
						disabled={true}
						title="Dieses Feature ist in Kürze verfügbar."
					>
						<i className="fas fa-fingerprint"></i> Mit Passkey anmelden (in Kürze)
					</button>
				</form>
			</div>
		</div>
	);
};

export default LoginPage;