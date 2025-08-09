import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';

const AdminLogPage = () => {
	const apiCall = useCallback(() => apiClient.get('/logs'), []);
	const { data: logs, loading, error } = useApi(apiCall);

	const renderTable = () => {
		if (loading) return <tr><td colSpan="4">Lade Logs...</td></tr>;
		if (error) return <tr><td colSpan="4" className="error-message">{error}</td></tr>;
		if (!logs || logs.length === 0) return <tr><td colSpan="4" style={{ textAlign: 'center' }}>Keine Log-Einträge gefunden.</td></tr>;

		return logs.map(log => (
			<tr key={log.id}>
				<td>{new Date(log.actionTimestamp).toLocaleString('de-DE')} Uhr</td>
				<td>{log.adminUsername}</td>
				<td>{log.actionType}</td>
				<td style={{ whiteSpace: 'normal' }}>{log.details}</td>
			</tr>
		));
	};

	const renderMobileList = () => {
		if (loading) return <p>Lade Logs...</p>;
		if (error) return <p className="error-message">{error}</p>;
		if (!logs || logs.length === 0) return <div className="card"><p>Keine Log-Einträge gefunden.</p></div>;

		return logs.map(log => (
			<div className="list-item-card" key={log.id}>
				<h3 className="card-title">{log.actionType}</h3>
				<div className="card-row"><strong>Wer:</strong> <span>{log.adminUsername}</span></div>
				<div className="card-row"><strong>Wann:</strong> <span>{new Date(log.actionTimestamp).toLocaleString('de-DE')}</span></div>
				<p style={{ marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid var(--border-color)', whiteSpace: 'normal' }}>{log.details}</p>
			</div>
		));
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