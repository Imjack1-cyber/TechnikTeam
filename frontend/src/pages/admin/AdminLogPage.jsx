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

	return (
		<div>
			<h1>Admin Aktions-Protokoll</h1>
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
		</div>
	);
};

export default AdminLogPage;