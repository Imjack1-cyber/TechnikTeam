import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const AdminRequestsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/profile-requests'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();

	const handleAction = async (requestId, action) => {
		const endpoint = `/profile-requests/${requestId}/${action}`;
		const confirmationText = action === 'approve'
			? 'Änderungen wirklich übernehmen?'
			: 'Antrag wirklich ablehnen?';

		if (window.confirm(confirmationText)) {
			try {
				const result = await apiClient.post(endpoint);
				if (result.success) {
					addToast(`Antrag erfolgreich ${action === 'approve' ? 'genehmigt' : 'abgelehnt'}.`, 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler: ${err.message}`, 'error');
			}
		}
	};

	const renderChanges = (changesJson) => {
		try {
			const changes = JSON.parse(changesJson);
			return (
				<ul style={{ paddingLeft: '1.5rem', margin: 0 }}>
					{Object.entries(changes).map(([key, value]) => (
						<li key={key}><strong>{key}:</strong> {value}</li>
					))}
				</ul>
			);
		} catch (e) {
			return <span className="text-danger">Fehler beim Parsen der Änderungen.</span>;
		}
	};

	const renderTable = () => {
		if (loading) return <tr><td colSpan="4">Lade Anträge...</td></tr>;
		if (error) return <tr><td colSpan="4" className="error-message">{error}</td></tr>;
		if (!requests || requests.length === 0) return <tr><td colSpan="4" style={{ textAlign: 'center' }}>Keine offenen Anträge vorhanden.</td></tr>;

		return requests.map(req => (
			<tr key={req.id}>
				<td>{req.username}</td>
				<td>{new Date(req.requestedAt).toLocaleString('de-DE')}</td>
				<td>{renderChanges(req.requestedChanges)}</td>
				<td style={{ display: 'flex', gap: '0.5rem' }}>
					<button onClick={() => handleAction(req.id, 'approve')} className="btn btn-small btn-success">Genehmigen</button>
					<button onClick={() => handleAction(req.id, 'deny')} className="btn btn-small btn-danger">Ablehnen</button>
				</td>
			</tr>
		));
	};

	return (
		<div>
			<h1><i className="fas fa-inbox"></i> Offene Anträge</h1>
			<p>Hier sehen Sie alle von Benutzern beantragten Änderungen an Stammdaten. Genehmigte Änderungen werden sofort wirksam.</p>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Benutzer</th>
							<th>Beantragt am</th>
							<th>Gewünschte Änderungen</th>
							<th>Aktion</th>
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

export default AdminRequestsPage;