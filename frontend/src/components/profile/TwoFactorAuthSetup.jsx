import React, { useState, useEffect, useCallback } from 'react';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';

const TwoFactorAuthSetup = ({ onSetupComplete }) => {
    const [setupData, setSetupData] = useState(null);
    const [verificationCode, setVerificationCode] = useState('');
    const [backupCodes, setBackupCodes] = useState([]);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();

    const startSetup = async () => {
        try {
            const result = await apiClient.post('/public/profile/2fa/setup');
            if (result.success) {
                setSetupData(result.data);
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Setup could not be started.');
        }
    };

    useEffect(() => {
        startSetup();
    }, []);

    const handleVerifyAndEnable = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');
        try {
            const payload = {
                secret: setupData.secret,
                token: verificationCode
            };
            const result = await apiClient.post('/public/profile/2fa/enable', payload);
            if (result.success) {
                setBackupCodes(result.data.backupCodes);
                setSetupData(null); // Move to backup codes view
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Verification failed. Check the code and your device time.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCopyCodes = () => {
        navigator.clipboard.writeText(backupCodes.join('\n'));
        addToast('Backup-Codes in die Zwischenablage kopiert!', 'success');
    };

    if (error) {
        return <p className="error-message">{error}</p>;
    }

    if (backupCodes.length > 0) {
        return (
            <div>
                <h4 className="text-success">2FA erfolgreich aktiviert!</h4>
                <p className="info-message">Bitte speichern Sie die folgenden Backup-Codes an einem sicheren Ort. Sie können verwendet werden, um auf Ihr Konto zuzugreifen, falls Sie Ihr Authentifizierungsgerät verlieren.</p>
                <div style={{ background: 'var(--bg-color)', padding: '1rem', borderRadius: 'var(--border-radius)', fontFamily: 'monospace', margin: '1rem 0' }}>
                    {backupCodes.map(code => <div key={code}>{code}</div>)}
                </div>
                <button onClick={handleCopyCodes} className="btn btn-secondary">Codes kopieren</button>
                <button onClick={onSetupComplete} className="btn" style={{ marginLeft: '1rem' }}>Fertig</button>
            </div>
        );
    }

    if (setupData) {
        return (
            <form onSubmit={handleVerifyAndEnable}>
                <p>1. Scannen Sie diesen QR-Code mit Ihrer Authenticator-App (z.B. Google Authenticator, Authy).</p>
                <div style={{ textAlign: 'center', margin: '1rem 0' }}>
                    <img src={setupData.qrCodeDataUri} alt="2FA QR Code" />
                </div>
                <p>2. Geben Sie den 6-stelligen Code aus Ihrer App ein, um die Einrichtung abzuschließen.</p>
                <div className="form-group">
                    <label htmlFor="2fa-code">Verifizierungscode</label>
                    <input
                        id="2fa-code"
                        type="text"
                        value={verificationCode}
                        onChange={e => setVerificationCode(e.target.value)}
                        required
                        maxLength="6"
                        pattern="\d{6}"
                        title="Bitte geben Sie einen 6-stelligen Code ein."
                    />
                </div>
                <button type="submit" className="btn btn-success" disabled={isSubmitting}>
                    {isSubmitting ? 'Wird überprüft...' : 'Aktivieren'}
                </button>
            </form>
        );
    }

    return <p>Lade 2FA-Setup...</p>;
};

export default TwoFactorAuthSetup;