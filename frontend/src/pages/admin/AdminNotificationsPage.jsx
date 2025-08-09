import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminNotificationsPage = () => {
	const { addToast } = useToast();
	const [formData, setFormData] = useState({
		title: '',
		description: '',
		level: 'Informational',
		targetType: 'ALL',
		targetId: ''
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
	const [isWarningConfirmModalOpen, setIsWarningConfirmModalOpen] = useState(false);

	const fetchEvents = useCallback(() => apiClient.get('/events'), []);
	const fetchMeetings = useCallback(() => apiClient.get('/meetings?courseId=0'), []); // Placeholder to fetch all

	const { data: events, loading: eventsLoading } = useApi(fetchEvents);
	const { data: meetings, loading: meetingsLoading } = useApi(fetchMeetings);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleSubmit = (e) => {
		e.preventDefault();
		setError('');
		if (!formData.title || !formData.description) {
			setError('Titel und Beschreibung sind erforderlich.');
			return;
		}
		setIsConfirmModalOpen(true);
	};

	const handleConfirmSend = () => {
		setIsConfirmModalOpen(false);
		if (formData.level === 'Warning') {
			setIsWarningConfirmModalOpen(true);
		} else {
			sendNotification();
		}
	};

	const handleWarningConfirmSend = () => {
		setIsWarningConfirmModalOpen(false);
		sendNotification();
	};

	const sendNotification = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const payload = { ...formData, targetId: formData.targetId ? parseInt(formData.targetId, 10) : null };
			const result = await apiClient.post('/admin/notifications', payload);
			if (result.success) {
				addToast(result.message, 'success');
				setFormData({ title: '', description: '', level: 'Informational', targetType: 'ALL', targetId: '' });
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Senden fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const getTargetName = () => {
		switch (formData.targetType) {
			case 'ALL': return 'Alle Benutzer';
			case 'EVENT': {
				const event = events?.find(e => e.id.toString() === formData.targetId);
				return `Alle Teilnehmer von Event: ${event?.name || 'Unbekannt'}`;
			}
			case 'MEETING': {
				const meeting = meetings?.find(m => m.id.toString() === formData.targetId);
				return `Alle Teilnehmer von Meeting: ${meeting?.name || 'Unbekannt'}`;
			}
			default: return 'Unbekannt';
		}
	};

	return (
		<div>
			<h1><i className="fas fa-bullhorn"></i> Benachrichtigungen senden</h1>
			<p>Erstellen und versenden Sie hier systemweite Benachrichtigungen an Benutzergruppen.</p>

			<div className="card">
				<h2 className="card-title">Neue Benachrichtigung</h2>
				{error && <p className="error-message">{error}</p>}
				<form onSubmit={handleSubmit}>
					<div className="form-group">
						<label htmlFor="title">Titel</label>
						<input type="text" id="title" name="title" value={formData.title} onChange={handleChange} required maxLength="100" />
					</div>
					<div className="form-group">
						<label htmlFor="description">Beschreibung</label>
						<textarea id="description" name="description" value={formData.description} onChange={handleChange} required rows="4"></textarea>
					</div>
					<div className="responsive-dashboard-grid">
						<div className="form-group">
							<label htmlFor="level">Stufe</label>
							<select id="level" name="level" value={formData.level} onChange={handleChange}>
								<option value="Informational">Informational</option>
								<option value="Important">Important</option>
								<option value="Warning">Warning (Notfall)</option>
							</select>
						</div>
						<div className="form-group">
							<label htmlFor="targetType">Zielgruppe</label>
							<select id="targetType" name="targetType" value={formData.targetType} onChange={handleChange}>
								<option value="ALL">Alle Benutzer</option>
								<option value="EVENT">Event-Teilnehmer</option>
								<option value="MEETING">Meeting-Teilnehmer</option>
							</select>
						</div>
					</div>

					{(formData.targetType === 'EVENT' || formData.targetType === 'MEETING') && (
						<div className="form-group">
							<label htmlFor="targetId">Spezifisches Ziel auswählen</label>
							<select id="targetId" name="targetId" value={formData.targetId} onChange={handleChange} required>
								<option value="">-- Bitte auswählen --</option>
								{formData.targetType === 'EVENT' && !eventsLoading && events?.map(e => <option key={e.id} value={e.id}>{e.name}</option>)}
								{formData.targetType === 'MEETING' && !meetingsLoading && meetings?.map(m => <option key={m.id} value={m.id}>{m.parentCourseName}: {m.name}</option>)}
							</select>
						</div>
					)}

					<button type="submit" className="btn btn-success" disabled={isSubmitting}>
						{isSubmitting ? 'Wird gesendet...' : 'Benachrichtigung prüfen & senden'}
					</button>
				</form>
			</div>

			<Modal isOpen={isConfirmModalOpen} onClose={() => setIsConfirmModalOpen(false)} title="Benachrichtigung bestätigen">
				<h4>Bitte bestätigen Sie die folgenden Details:</h4>
				<ul className="details-list">
					<li><strong>Titel:</strong> {formData.title}</li>
					<li><strong>Beschreibung:</strong> {formData.description}</li>
					<li><strong>Stufe:</strong> {formData.level}</li>
					<li><strong>Zielgruppe:</strong> {getTargetName()}</li>
				</ul>
				<div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem', marginTop: '1.5rem' }}>
					<button onClick={() => setIsConfirmModalOpen(false)} className="btn btn-secondary">Abbrechen</button>
					<button onClick={handleConfirmSend} className="btn btn-success">Senden</button>
				</div>
			</Modal>

			<Modal isOpen={isWarningConfirmModalOpen} onClose={() => setIsWarningConfirmModalOpen(false)} title="WARNUNG: Notfall-Benachrichtigung">
				<div style={{ textAlign: 'center' }}>
					<i className="fas fa-exclamation-triangle" style={{ fontSize: '3rem', color: 'var(--danger-color)' }}></i>
					<p style={{ marginTop: '1rem' }}>Sie sind im Begriff, eine <strong>Warn-Benachrichtigung</strong> zu senden. Diese Stufe sollte nur für echte Notfälle oder äußerst wichtige, dringende Informationen verwendet werden.</p>
					<p><strong>Sind Sie sicher, dass Sie fortfahren möchten?</strong></p>
				</div>
				<div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginTop: '1.5rem' }}>
					<button onClick={() => setIsWarningConfirmModalOpen(false)} className="btn btn-secondary">Abbrechen</button>
					<button onClick={handleWarningConfirmSend} className="btn btn-danger">Ja, Notfall-Benachrichtigung senden</button>
				</div>
			</Modal>
		</div>
	);
};

export default AdminNotificationsPage;