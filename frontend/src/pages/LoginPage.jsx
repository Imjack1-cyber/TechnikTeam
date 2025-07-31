import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { passkeyService } from '../services/passkeyService'; // Assuming you have a passkey service

const LoginPage = () => {
	const [username, setUsername] = useState('');
	const [password, setPassword] = useState('');
	const [isPasswordVisible, setIsPasswordVisible] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const navigate = useNavigate();
	const location = useLocation();

	const login = useAuthStore((state) => state.login);
	const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

	useEffect(() => {
		if (isAuthenticated) {
			const from = location.state?.from?.pathname || '/home';
			navigate(from, { replace: true });
		}
	}, [isAuthenticated, navigate, location.state]);

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setError('');
		try {
			await login(username, password);
		} catch (err) {
			setError(err.message || 'Login fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.');
		} finally {
			setIsLoading(false);
		}
	};

	const handlePasskeyLogin = async () => {
		setIsLoading(true);
		setError('');
		if (!username) {
			setError('Bitte geben Sie zuerst Ihren Benutzernamen ein.');
			setIsLoading(false);
			return;
		}
		try {
			await passkeyService.loginWithPasskey(username);
			// The login function in passkeyService should set the auth state
		} catch (err) {
			setError(err.message || 'Passkey-Login fehlgeschlagen.');
		} finally {
			setIsLoading(false);
		}
	};


	const togglePasswordVisibility = () => {
		setIsPasswordVisible(!isPasswordVisible);
	};

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
							autoComplete="username webauthn"
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
						onClick={handlePasskeyLogin}
						disabled={isLoading}
					>
						<i className="fas fa-fingerprint"></i> Mit Passkey anmelden
					</button>
				</form>
			</div>
		</div>
	);
};

export default LoginPage;