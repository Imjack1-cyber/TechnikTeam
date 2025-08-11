import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';

const AdminLogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/logs'), []);
	const { data: logs, loading, error, reload } = useApi(apiCall);
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const { addToast } = useToast();

	const canRevoke = isAdmin || user?.permissions.includes('LOG_REVOKE');

	const handleRevoke = async (log) => {
		if (window.confirm(`Aktion "${log.actionType}" (ID: ${log.id}) wirklich widerrufen? Dies führt die entsprechende Gegenaktion aus.`)) {
			try {
				const result = await apiClient.post(`/logs/${log.id}/revoke`);
				if (result.success) {
					addToast('Aktion erfolgreich widerrufen.', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Widerrufen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};


	const renderTable = () => {
		if (loading) return <tr><td colSpan={canRevoke ? "6" : "5"}>Lade Logs...</td></tr>;
		if (error) return <tr><td colSpan={canRevoke ? "6" : "5"} className="error-message">{error}</td></tr>;
		if (!logs || logs.length === 0) return <tr><td colSpan={canRevoke ? "6" : "5"} style={{ textAlign: 'center' }}>Keine Log-Einträge gefunden.</td></tr>;

		return logs.map(log => {
			let context = {};
			try {
				if (log.context) context = JSON.parse(log.context);
			} catch (e) { /* ignore parse error */ }
			const isRevocable = canRevoke && log.status === 'ACTIVE' && context.revocable === true;

			return (
				<tr key={log.id}>
					<td>{new Date(log.actionTimestamp).toLocaleString('de-DE')} Uhr</td>
					<td>{log.adminUsername}</td>
					<td>{log.actionType}</td>
					<td style={{ whiteSpace: 'normal' }}>{log.details}</td>
					<td>
						{log.status === 'REVOKED'
							? <span className="status-badge status-info" title={`Widerrufen von ${log.revokingAdminUsername} am ${new Date(log.revokedAt).toLocaleString('de-DE')}`}>Widerrufen</span>
							: <span className="status-badge status-ok">Aktiv</span>}
					</td>
					{canRevoke && (
						<td>
							<button onClick={() => handleRevoke(log)} className="btn btn-small btn-warning" disabled={!isRevocable} title={isRevocable ? 'Diese Aktion widerrufen' : 'Diese Aktion kann nicht widerrufen werden'}>
								Widerrufen
							</button>
						</td>
					)}
				</tr>
			);
		});
	};

	const renderMobileList = () => {
		if (loading) return <p>Lade Logs...</p>;
		if (error) return <p className="error-message">{error}</p>;
		if (!logs || logs.length === 0) return <div className="card"><p>Keine Log-Einträge gefunden.</p></div>;

		return logs.map(log => {
			let context = {};
			try {
				if (log.context) context = JSON.parse(log.context);
			} catch (e) { /* ignore parse error */ }
			const isRevocable = canRevoke && log.status === 'ACTIVE' && context.revocable === true;

			return (
				<div className="list-item-card" key={log.id}>
					<div style={{ display: 'flex', justifyContent: 'space-between' }}>
						<h3 className="card-title">{log.actionType}</h3>
						{log.status === 'REVOKED'
							? <span className="status-badge status-info" title={`Widerrufen von ${log.revokingAdminUsername} am ${new Date(log.revokedAt).toLocaleString('de-DE')}`}>Widerrufen</span>
							: <span className="status-badge status-ok">Aktiv</span>}
					</div>
					<div className="card-row"><strong>Wer:</strong> <span>{log.adminUsername}</span></div>
					<div className="card-row"><strong>Wann:</strong> <span>{new Date(log.actionTimestamp).toLocaleString('de-DE')}</span></div>
					<p style={{ marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid var(--border-color)', whiteSpace: 'normal' }}>{log.details}</p>
					{canRevoke && (
						<div className="card-actions">
							<button onClick={() => handleRevoke(log)} className="btn btn-small btn-warning" disabled={!isRevocable} title={isRevocable ? 'Diese Aktion widerrufen' : 'Diese Aktion kann nicht widerrufen werden'}>
								Widerrufen
							</button>
						</div>
					)}
				</div>
			);
		});
	};

	return (
		<div>
			<h1><i className="fas fa-clipboard-list"></i> Admin Aktions-Protokoll</h1>
			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Wann</th>
							<th>Wer</th>
							<th>Aktionstyp</th>
							<th>Details</th>
							<th>Status</th>
							{canRevoke && <th>Aktion</th>}
						</tr>
					</thead>
					<tbody>
						{renderTable()}
					</tbody>
				</table>
			</div>
			<div className="mobile-card-list">
				{renderMobileList()}
			</div>
		</div>
	);
};

export default AdminLogPage;