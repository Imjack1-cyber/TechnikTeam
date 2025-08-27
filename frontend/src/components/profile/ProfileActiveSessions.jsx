import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';

const RenameSessionModal = ({ isOpen, onClose, onSuccess, session }) => {
    const [deviceName, setDeviceName] = useState(session.deviceName || '');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/public/sessions/${session.id}/name`, { deviceName });
            if (result.success) {
                addToast('Gerät erfolgreich umbenannt.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Gerät umbenennen">
            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="deviceName">Name für diese Sitzung/dieses Gerät</label>
                    <input id="deviceName" value={deviceName} onChange={e => setDeviceName(e.target.value)} required />
                </div>
                <button type="submit" className="btn" disabled={isSubmitting}>
                    {isSubmitting ? 'Speichern...' : 'Speichern'}
                </button>
            </form>
        </Modal>
    );
};


const ProfileActiveSessions = () => {
    const apiCall = useCallback(() => apiClient.get('/public/sessions'), []);
    const { data: sessions, loading, error, reload } = useApi(apiCall);
    const { addToast } = useToast();
    const [renamingSession, setRenamingSession] = useState(null);

    const getDeviceIcon = (deviceType) => {
        switch (deviceType?.toLowerCase()) {
            case 'desktop': return 'fa-desktop';
            case 'mobile': return 'fa-mobile-alt';
            case 'tablet': return 'fa-tablet-alt';
            default: return 'fa-question-circle';
        }
    };

    const handleRevoke = async (session) => {
        if (window.confirm(`Sitzung auf Gerät "${session.deviceName || 'Unbenannt'}" (${session.ipAddress}) wirklich beenden?`)) {
            try {
                const result = await apiClient.post(`/public/sessions/${session.jti}/revoke`);
                if (result.success) {
                    addToast('Sitzung wurde beendet.', 'success');
                    reload();
                } else {
                    throw new Error(result.message);
                }
            } catch (err) {
                addToast(`Fehler: ${err.message}`, 'error');
            }
        }
    };

    const handleRevokeAll = async () => {
        if (window.confirm("Möchten Sie wirklich ALLE anderen Sitzungen beenden? Sie bleiben in dieser Sitzung angemeldet.")) {
            try {
                const result = await apiClient.post('/public/sessions/revoke-all');
                if (result.success) {
                    addToast('Alle anderen Sitzungen wurden beendet.', 'success');
                    reload();
                } else {
                    throw new Error(result.message);
                }
            } catch (err) {
                addToast(`Fehler: ${err.message}`, 'error');
            }
        }
    };

    return (
        <>
            <div className="card">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h2 className="card-title">Aktive Sitzungen</h2>
                    <button onClick={handleRevokeAll} className="btn btn-danger-outline" disabled={!sessions || sessions.length <= 1}>
                        <i className="fas fa-sign-out-alt"></i> Alle Anderen abmelden
                    </button>
                </div>
                <p>Hier sehen Sie alle Geräte, auf denen Sie aktuell angemeldet sind.</p>
                {loading && <p>Lade Sitzungen...</p>}
                {error && <p className="error-message">{error}</p>}

                <div className="desktop-table-wrapper">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Gerät</th>
                                <th>Standort</th>
                                <th>Letzte Aktivität</th>
                                <th>Aktion</th>
                            </tr>
                        </thead>
                        <tbody>
                            {sessions?.map(session => (
                                <tr key={session.id}>
                                    <td title={session.userAgent}>
                                        <i className={`fas ${getDeviceIcon(session.deviceType)}`} style={{ marginRight: '0.5rem' }}></i>
                                        <strong>{session.deviceName || 'Unbenanntes Gerät'}</strong>
                                        <br />
                                        <small>{session.deviceType}</small>
                                    </td>
                                    <td>{session.countryCode} ({session.ipAddress})</td>
                                    <td>{new Date(session.timestamp).toLocaleString('de-DE')}</td>
                                    <td>
                                        <button onClick={() => setRenamingSession(session)} className="btn btn-small btn-secondary">Umbenennen</button>
                                        <button onClick={() => handleRevoke(session)} className="btn btn-small btn-danger-outline" style={{ marginLeft: '0.5rem' }}>Abmelden</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <div className="mobile-card-list">
                    {sessions?.map(session => (
                        <div className="list-item-card" key={session.id}>
                            <h3 className="card-title" title={session.userAgent}>
                                <i className={`fas ${getDeviceIcon(session.deviceType)}`}></i> {session.deviceName || 'Unbenanntes Gerät'}
                            </h3>
                            <div className="card-row"><strong>Standort:</strong> <span>{session.countryCode} ({session.ipAddress})</span></div>
                            <div className="card-row"><strong>Letzte Aktivität:</strong> <span>{new Date(session.timestamp).toLocaleString('de-DE')}</span></div>
                            <div className="card-actions">
                                <button onClick={() => setRenamingSession(session)} className="btn btn-small btn-secondary">Umbenennen</button>
                                <button onClick={() => handleRevoke(session)} className="btn btn-small btn-danger-outline">Abmelden</button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {renamingSession && (
                <RenameSessionModal
                    isOpen={!!renamingSession}
                    onClose={() => setRenamingSession(null)}
                    onSuccess={() => { setRenamingSession(null); reload(); }}
                    session={renamingSession}
                />
            )}
        </>
    );
};

export default ProfileActiveSessions;