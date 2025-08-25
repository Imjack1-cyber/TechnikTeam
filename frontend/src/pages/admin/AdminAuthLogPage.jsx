import React, { useCallback, useState, useMemo, useEffect } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';

const AdminAuthLogPage = () => {
    const [refetchTrigger, setRefetchTrigger] = useState(0);
    const apiCall = useCallback(() => apiClient.get(`/admin/auth-log?cb=${refetchTrigger}`), [refetchTrigger]);
    const { data: logs, loading, error, reload } = useApi(apiCall);
    const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const { addToast } = useToast();

    // Filter states
    const [showFilter, setShowFilter] = useState('ACTIVE'); // ACTIVE, INACTIVE, ALL
    const [userFilter, setUserFilter] = useState('');
    const [ipFilter, setIpFilter] = useState('');
    const [groupIp, setGroupIp] = useState(true);

    const canRevoke = isAdmin || user?.permissions.includes('LOG_REVOKE');

    const handleForceLogout = async (jti) => {
        if (!jti) {
            addToast('Sitzungs-ID nicht gefunden, kann nicht widerrufen werden.', 'error');
            return;
        }
        if (window.confirm(`Diese Sitzung wirklich beenden? Der Benutzer wird bei seiner nächsten Aktion ausgeloggt.`)) {
            try {
                const result = await apiClient.post('/admin/auth-log/revoke-session', { jti: jti });
                if (result.success) {
                    addToast('Sitzung erfolgreich widerrufen.', 'success');
                    await reload(); // Await the refetch before the component re-renders
                } else {
                    throw new Error(result.message);
                }
            } catch (err) {
                addToast(`Widerrufen der Sitzung fehlgeschlagen: ${err.message}`, 'error');
            }
        }
    };

    const isSessionActive = useCallback((log) => {
        return log.eventType === 'LOGIN_SUCCESS' && !log.isRevoked && log.tokenExpiry && new Date(log.tokenExpiry) > new Date();
    }, []);

    const filteredLogs = useMemo(() => {
        if (!logs) return [];

        let processedLogs = logs
            .filter(log => {
                const matchesUser = userFilter ? log.username.toLowerCase().includes(userFilter.toLowerCase()) : true;
                const matchesIp = ipFilter ? log.ipAddress.toLowerCase().includes(ipFilter.toLowerCase()) : true;
                return matchesUser && matchesIp;
            })
            .filter(log => {
                if (showFilter === 'ACTIVE') return isSessionActive(log);
                if (showFilter === 'INACTIVE') return !isSessionActive(log);
                return true; // 'ALL'
            });

        if (groupIp) {
            const uniqueIps = new Map();
            processedLogs.forEach(log => {
                if (!uniqueIps.has(log.ipAddress)) {
                    uniqueIps.set(log.ipAddress, log);
                }
            });
            return Array.from(uniqueIps.values());
        }

        return processedLogs;
    }, [logs, showFilter, userFilter, ipFilter, groupIp, isSessionActive]);


    const getStatusBadge = (log) => {
        if (log.eventType === 'LOGOUT') {
            return <span className="status-badge status-info">Ausgeloggt</span>;
        }
        if (log.isRevoked) {
            return <span className="status-badge status-info">Widerrufen</span>;
        }
        if (log.tokenExpiry && new Date(log.tokenExpiry) > new Date()) {
            return <span className="status-badge status-ok">Aktiv</span>;
        }
        return <span className="status-badge status-info">Abgelaufen</span>;
    };

    const renderAction = (log) => {
        if (isSessionActive(log)) {
            return (
                <button
                    onClick={() => handleForceLogout(log.jti)}
                    className="btn btn-small btn-danger"
                    title="Diese Sitzung zwangsweise beenden"
                >
                    Logout erzwingen
                </button>
            );
        }
        return null;
    };

    return (
        <div>
            <h1><i className="fas fa-history"></i> Aktive Sitzungen &amp; Login-Verlauf</h1>
            <p>Übersicht der letzten erfolgreichen An- und Abmeldungen. Fehlgeschlagene Logins werden hier nicht mehr angezeigt.</p>

            <div className="card">
                <h4 style={{marginTop: 0}}>Filter</h4>
                <div className="responsive-dashboard-grid">
                    <div className="form-group">
                        <label>Status anzeigen</label>
                        <div>
                            <label style={{marginRight: '1rem'}}><input type="radio" value="ACTIVE" checked={showFilter === 'ACTIVE'} onChange={e => setShowFilter(e.target.value)} /> Aktive</label>
                            <label style={{marginRight: '1rem'}}><input type="radio" value="INACTIVE" checked={showFilter === 'INACTIVE'} onChange={e => setShowFilter(e.target.value)} /> Inaktive</label>
                            <label><input type="radio" value="ALL" checked={showFilter === 'ALL'} onChange={e => setShowFilter(e.target.value)} /> Alle</label>
                        </div>
                    </div>
                    <div className="form-group">
                        <label htmlFor="user-filter">Nach Benutzer filtern</label>
                        <input type="text" id="user-filter" value={userFilter} onChange={e => setUserFilter(e.target.value)} placeholder="Username..."/>
                    </div>
                    <div className="form-group">
                        <label htmlFor="ip-filter">Nach IP-Adresse filtern</label>
                        <input type="text" id="ip-filter" value={ipFilter} onChange={e => setIpFilter(e.target.value)} placeholder="127.0.0.1..."/>
                    </div>
                    <div className="form-group" style={{alignSelf: 'flex-end'}}>
                        <label>
                            <input type="checkbox" checked={groupIp} onChange={e => setGroupIp(e.target.checked)} />
                            Neuesten Eintrag pro IP gruppieren
                        </label>
                    </div>
                </div>
            </div>

            <div className="table-wrapper">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Zeitpunkt</th>
                            <th>Benutzername</th>
                            <th>IP-Adresse</th>
                            <th>Status</th>
                            {canRevoke && <th>Aktion</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {loading && <tr><td colSpan={canRevoke ? 5 : 4}>Lade Verlauf...</td></tr>}
                        {error && <tr><td colSpan={canRevoke ? 5 : 4} className="error-message">{error}</td></tr>}
                        {filteredLogs.map(log => (
                            <tr key={log.id}>
                                <td>{new Date(log.timestamp).toLocaleString('de-DE')}</td>
                                <td>{log.username}</td>
                                <td>{log.ipAddress}</td>
                                <td>{getStatusBadge(log)}</td>
                                {canRevoke && <td>{renderAction(log)}</td>}
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default AdminAuthLogPage;