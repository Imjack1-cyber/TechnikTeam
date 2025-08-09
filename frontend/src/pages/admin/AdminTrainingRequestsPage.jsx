import React, { useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const AdminTrainingRequestsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/training-requests'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();

	const handleDelete = async (request) => {
		if (window.confirm(`Anfrage für "${request.topic}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/training-requests/${request.id}`);
				if (result.success) {
					addToast('Anfrage gelöscht', 'success');
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
			<h1><i className="fas fa-question-circle"></i> Lehrgangsanfragen</h1>
			<p>Hier sehen Sie alle von Benutzern eingereichten Wünsche für neue Lehrgänge.</p>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Thema</th>
							<th>Angefragt von</th>
							<th>Datum</th>
							<th>Interessenten</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="5">Lade Anfragen...</td></tr>}
						{error && <tr><td colSpan="5" className="error-message">{error}</td></tr>}
						{requests?.map(req => (
							<tr key={req.id}>
								<td>{req.topic}</td>
								<td>{req.requesterUsername}</td>
								<td>{new Date(req.createdAt).toLocaleDateString('de-DE')}</td>
								<td>{req.interestCount}</td>
								<td>
									<button onClick={() => handleDelete(req)} className="btn btn-small btn-danger">Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{loading && <p>Lade Anfragen...</p>}
				{error && <p className="error-message">{error}</p>}
				{requests?.map(req => (
					<div className="list-item-card" key={req.id}>
						<h3 className="card-title">{req.topic}</h3>
						<div className="card-row"><strong>Angefragt von:</strong> <span>{req.requesterUsername}</span></div>
						<div className="card-row"><strong>Datum:</strong> <span>{new Date(req.createdAt).toLocaleDateString('de-DE')}</span></div>
						<div className="card-row"><strong>Interessenten:</strong> <span>{req.interestCount}</span></div>
						<div className="card-actions">
							<button onClick={() => handleDelete(req)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>
		</div>
	);
};

export default AdminTrainingRequestsPage;