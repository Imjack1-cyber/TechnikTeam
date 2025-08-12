import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminDamageReportsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/damage-reports/pending'), []);
	const { data: reports, loading, error, reload } = useApi(apiCall);
	const [selectedReport, setSelectedReport] = useState(null);
	const [action, setAction] = useState(null); // 'confirm' or 'reject'
	const { addToast } = useToast();

	const openModal = (report, act) => {
		setSelectedReport(report);
		setAction(act);
	};

	const closeModal = () => {
		setSelectedReport(null);
		setAction(null);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	return (
		<div>
			<h1><i className="fas fa-tools"></i> Offene Schadensmeldungen</h1>
			<p>Hier sehen Sie alle von Benutzern gemeldeten Schäden, die noch nicht von einem Admin bestätigt wurden.</p>

			{loading && <p>Lade Meldungen...</p>}
			{error && <p className="error-message">{error}</p>}
			{!loading && !error && reports?.length === 0 && <div className="card"><p>Keine offenen Meldungen vorhanden.</p></div>}

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Gemeldet am</th>
							<th>Artikel</th>
							<th>Gemeldet von</th>
							<th>Beschreibung</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{reports?.map(report => (
							<tr key={report.id}>
								<td>{new Date(report.reportedAt).toLocaleString('de-DE')}</td>
								<td><Link to={`/lager/details/${report.itemId}`}>{report.itemName}</Link></td>
								<td><Link to={`/team/${report.reporterUserId}`}>{report.reporterUsername}</Link></td>
								<td style={{ whiteSpace: 'normal' }}>{report.reportDescription}</td>
								<td style={{ display: 'flex', gap: '0.5rem' }}>
									<button onClick={() => openModal(report, 'confirm')} className="btn btn-small btn-success">Bestätigen</button>
									<button onClick={() => openModal(report, 'reject')} className="btn btn-small btn-danger">Ablehnen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{reports?.map(report => (
					<div className="list-item-card" key={report.id}>
						<h3 className="card-title"><Link to={`/lager/details/${report.itemId}`}>{report.itemName}</Link></h3>
						<div className="card-row"><strong>Von:</strong> <span><Link to={`/team/${report.reporterUserId}`}>{report.reporterUsername}</Link></span></div>
						<div className="card-row"><strong>Am:</strong> <span>{new Date(report.reportedAt).toLocaleString('de-DE')}</span></div>
						<p style={{ marginTop: '0.5rem' }}><strong>Beschreibung:</strong> {report.reportDescription}</p>
						<div className="card-actions">
							<button onClick={() => openModal(report, 'confirm')} className="btn btn-small btn-success">Bestätigen</button>
							<button onClick={() => openModal(report, 'reject')} className="btn btn-small btn-danger">Ablehnen</button>
						</div>
					</div>
				))}
			</div>

			{selectedReport && (
				<ActionModal
					isOpen={!!selectedReport}
					onClose={closeModal}
					onSuccess={handleSuccess}
					report={selectedReport}
					action={action}
				/>
			)}
		</div>
	);
};

const ActionModal = ({ isOpen, onClose, onSuccess, report, action }) => {
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const [notes, setNotes] = useState('');
	const [quantity, setQuantity] = useState(1);
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsLoading(true);
		setError('');

		try {
			const payload = action === 'confirm' ? { quantity } : { adminNotes: notes };
			const result = await apiClient.post(`/admin/damage-reports/${report.id}/${action}`, payload);

			if (result.success) {
				addToast(result.message, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Aktion fehlgeschlagen');
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Meldung #${report.id} ${action === 'confirm' ? 'bestätigen' : 'ablehnen'}`}>
			<p><strong>Artikel:</strong> {report.itemName}</p>
			<p><strong>Beschreibung des Nutzers:</strong> {report.reportDescription}</p>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				{action === 'confirm' && (
					<div className="form-group">
						<label htmlFor="quantity">Anzahl als defekt markieren</label>
						<input type="number" id="quantity" value={quantity} onChange={e => setQuantity(parseInt(e.target.value, 10))} min="1" required />
					</div>
				)}
				{action === 'reject' && (
					<div className="form-group">
						<label htmlFor="notes">Grund für die Ablehnung (optional)</label>
						<textarea id="notes" value={notes} onChange={e => setNotes(e.target.value)} rows="3"></textarea>
					</div>
				)}
				<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1rem' }}>
					<button type="button" className="btn btn-secondary" onClick={onClose} disabled={isLoading}>Abbrechen</button>
					<button type="submit" className={`btn ${action === 'confirm' ? 'btn-success' : 'btn-danger'}`} disabled={isLoading}>
						{isLoading ? 'Wird verarbeitet...' : (action === 'confirm' ? 'Bestätigen' : 'Ablehnen')}
					</button>
				</div>
			</form>
		</Modal>
	);
};


export default AdminDamageReportsPage;