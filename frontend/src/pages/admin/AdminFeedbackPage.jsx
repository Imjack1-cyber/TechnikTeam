import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import StatusBadge from '../../components/ui/StatusBadge';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const FeedbackColumn = ({ title, submissions, onCardClick }) => (
	<div className="feedback-column">
		<h2>{title}</h2>
		<div className="feedback-list">
			{submissions.map(sub => (
				<div key={sub.id} className="feedback-card-item" onClick={() => onCardClick(sub)}>
					<strong className="subject">{sub.subject}</strong>
					<p className="content-preview">{sub.content}</p>
					<div className="meta">
						<span>Von: {sub.username}</span>
					</div>
				</div>
			))}
		</div>
	</div>
);

const AdminFeedbackPage = () => {
	const apiCall = useCallback(() => apiClient.get('/feedback'), []);
	const { data: submissions, loading, error, reload } = useApi(apiCall);
	const [selectedFeedback, setSelectedFeedback] = useState(null);
	const { addToast } = useToast();

	const groupedSubmissions = submissions?.reduce((acc, sub) => {
		(acc[sub.status] = acc[sub.status] || []).push(sub);
		return acc;
	}, {}) || {};

	const handleStatusChange = async (newStatus) => {
		if (!selectedFeedback) return;
		try {
			const result = await apiClient.put(`/feedback/${selectedFeedback.id}/status`, { status: newStatus });
			if (result.success) {
				addToast('Status erfolgreich aktualisiert', 'success');
				setSelectedFeedback(null);
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(`Fehler beim Ändern des Status: ${err.message}`, 'error');
		}
	};

	if (loading) return <div>Lade Feedback...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<div>
			<h1><i className="fas fa-inbox"></i> Feedback-Verwaltung</h1>
			<p>Verwalten Sie hier alle Benutzereinreichungen. Klicken Sie auf eine Karte, um den Status zu ändern.</p>
			<div className="feedback-board">
				<FeedbackColumn title="Neu" submissions={groupedSubmissions['NEW'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Gesehen" submissions={groupedSubmissions['VIEWED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Geplant" submissions={groupedSubmissions['PLANNED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Erledigt" submissions={groupedSubmissions['COMPLETED'] || []} onCardClick={setSelectedFeedback} />
				<FeedbackColumn title="Abgelehnt" submissions={groupedSubmissions['REJECTED'] || []} onCardClick={setSelectedFeedback} />
			</div>

			{selectedFeedback && (
				<Modal isOpen={!!selectedFeedback} onClose={() => setSelectedFeedback(null)} title={selectedFeedback.subject}>
					<p><strong>Von:</strong> {selectedFeedback.username}</p>
					<p><strong>Eingereicht am:</strong> {new Date(selectedFeedback.submittedAt).toLocaleString('de-DE')}</p>
					<div className="card" style={{ backgroundColor: 'var(--bg-color)', whiteSpace: 'pre-wrap' }}>{selectedFeedback.content}</div>
					<div style={{ marginTop: '1.5rem' }}>
						<h4>Status ändern:</h4>
						<div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
							{['NEW', 'VIEWED', 'PLANNED', 'COMPLETED', 'REJECTED'].map(status => (
								<button key={status} onClick={() => handleStatusChange(status)} className="btn btn-small">
									{status}
								</button>
							))}
						</div>
					</div>
				</Modal>
			)}
		</div>
	);
};

export default AdminFeedbackPage;