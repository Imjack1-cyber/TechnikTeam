import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
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
      // The useEffect will handle the navigation on successful state change
    } catch (err) {
      setError(err.message || 'Login fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.');
    } finally {
      setIsLoading(false);
    }
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
              autoComplete="username"
              autoFocus
              disabled={isLoading}
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Passwort</label>
            <div className="password-input-wrapper">
              <input
                type="password"
                id="password"
                name="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
                disabled={isLoading}
              />
              <span className="password-toggle-icon">
                <i className="fas fa-eye"></i>
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
        </form>
      </div>
    </div>
  );
};

export default LoginPage;