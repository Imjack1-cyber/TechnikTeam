import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const AdminGeoIpPage = () => {
    const apiCall = useCallback(() => apiClient.get('/admin/geoip/rules'), []);
    const { data: rules, loading, error, reload } = useApi(apiCall);
    const [newRule, setNewRule] = useState({ countryCode: '', ruleType: 'BLOCK' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewRule(prev => ({ ...prev, [name]: value.toUpperCase() }));
    };

    const handleAddRule = async (e) => {
        e.preventDefault();
        if (newRule.countryCode.length !== 2) {
            addToast('Ländercode muss genau 2 Zeichen lang sein.', 'error');
            return;
        }
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/admin/geoip/rules', newRule);
            if (result.success) {
                addToast('Regel erfolgreich gespeichert.', 'success');
                setNewRule({ countryCode: '', ruleType: 'BLOCK' });
                reload();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteRule = async (countryCode) => {
        if (window.confirm(`Regel für Ländercode "${countryCode}" wirklich löschen?`)) {
            try {
                const result = await apiClient.delete(`/admin/geoip/rules/${countryCode}`);
                if (result.success) {
                    addToast('Regel erfolgreich gelöscht.', 'success');
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
        <div>
            <h1><i className="fas fa-globe-americas"></i> GeoIP Filterregeln</h1>
            <p>Verwalten Sie hier, aus welchen Ländern der Login erlaubt oder blockiert ist.</p>

            <div className="card">
                <h2 className="card-title">Neue Regel hinzufügen</h2>
                <form onSubmit={handleAddRule} style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end' }}>
                    <div className="form-group" style={{ flexGrow: 1 }}>
                        <label htmlFor="countryCode">Ländercode (ISO 3166-1 alpha-2)</label>
                        <input type="text" id="countryCode" name="countryCode" value={newRule.countryCode} onChange={handleInputChange} maxLength="2" placeholder="z.B. DE, US, CN" required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="ruleType">Regeltyp</label>
                        <select id="ruleType" name="ruleType" value={newRule.ruleType} onChange={handleInputChange}>
                            <option value="BLOCK">Blockieren</option>
                            <option value="ALLOW">Erlauben</option>
                        </select>
                    </div>
                    <button type="submit" className="btn btn-success" disabled={isSubmitting}>
                        {isSubmitting ? 'Speichern...' : 'Hinzufügen'}
                    </button>
                </form>
            </div>

            <div className="card">
                <h2 className="card-title">Bestehende Regeln</h2>
                {loading && <p>Lade Regeln...</p>}
                {error && <p className="error-message">{error}</p>}
                <div className="table-wrapper">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Ländercode</th>
                                <th>Regel</th>
                                <th>Aktion</th>
                            </tr>
                        </thead>
                        <tbody>
                            {rules?.map(rule => (
                                <tr key={rule.countryCode}>
                                    <td>{rule.countryCode}</td>
                                    <td>
                                        <span className={`status-badge ${rule.ruleType === 'BLOCK' ? 'status-danger' : 'status-ok'}`}>
                                            {rule.ruleType}
                                        </span>
                                    </td>
                                    <td>
                                        <button onClick={() => handleDeleteRule(rule.countryCode)} className="btn btn-small btn-danger-outline">Löschen</button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminGeoIpPage;