import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

const AdminEventDebriefingPage = () => {
	const { eventId } = useParams();
	const { user } = useAuthStore();
	const { addToast } = useToast();

	// Fetch event details to get name and assigned crew
	const eventApiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading: eventLoading, error: eventError } = useApi(eventApiCall);

	// Fetch existing debriefing data
	const debriefingApiCall = useCallback(() => apiClient.get(`/admin/events/${eventId}/debriefing`), [eventId]);
	const { data: debriefing, loading: debriefingLoading, error: debriefingError, reload: reloadDebriefing } = useApi(debriefingApiCall);

	const [formData, setFormData] = useState({
		whatWentWell: '',
		whatToImprove: '',
		equipmentNotes: '',
		standoutCrewMemberIds: [],
	});
	const [isEditing, setIsEditing] = useState(false);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	useEffect(() => {
		if (debriefing) {
			setFormData({
				whatWentWell: debriefing.whatWentWell || '',
				whatToImprove: debriefing.whatToImprove || '',
				equipmentNotes: debriefing.equipmentNotes || '',
				standoutCrewMemberIds: debriefing.standoutCrewDetails?.map(u => u.id) || [],
			});
			setIsEditing(false); // Default to view mode if data exists
		} else {
			setIsEditing(true); // Default to edit mode if no data exists
		}
	}, [debriefing]);

	const handleChange = (e) => {
		const { name, value } = e.target;
		setFormData(prev => ({ ...prev, [name]: value }));
	};

	const handleMultiSelectChange = (e) => {
		const options = [...e.target.selectedOptions];
		const values = options.map(option => parseInt(option.value, 10));
		setFormData(prev => ({ ...prev, standoutCrewMemberIds: values }));
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/events/${eventId}/debriefing`, formData);
			if (result.success) {
				addToast('Debriefing erfolgreich gespeichert!', 'success');
				reloadDebriefing();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	if (eventLoading || debriefingLoading) return <div>Lade Daten...</div>;
	if (eventError) return <div className="error-message">{eventError}</div>;
	if (debriefingError) return <div className="error-message">{debriefingError}</div>;
	if (!event) return <div className="error-message">Event nicht gefunden.</div>;

	const canManage = user.isAdmin || user.id === event.leaderUserId;

	if (!isEditing && debriefing) {
		return (
			<div>
				<h1><i className="fas fa-clipboard-check"></i> Debriefing für: {event.name}</h1>
				<p className="details-subtitle">Eingereicht von {debriefing.authorUsername} am {new Date(debriefing.submittedAt).toLocaleString('de-DE')}</p>
				{canManage && <button className="btn" onClick={() => setIsEditing(true)}>Bearbeiten</button>}
				<div className="card" style={{ marginTop: '1.5rem' }}>
					<h3 className="card-title">Was lief gut?</h3>
					<div className="markdown-content"><ReactMarkdown rehypePlugins={[rehypeSanitize]}>{debriefing.whatWentWell}</ReactMarkdown></div>
				</div>
				<div className="card">
					<h3 className="card-title">Was kann verbessert werden?</h3>
					<div className="markdown-content"><ReactMarkdown rehypePlugins={[rehypeSanitize]}>{debriefing.whatToImprove}</ReactMarkdown></div>
				</div>
				<div className="card">
					<h3 className="card-title">Anmerkungen zum Material</h3>
					<div className="markdown-content"><ReactMarkdown rehypePlugins={[rehypeSanitize]}>{debriefing.equipmentNotes || 'Keine Anmerkungen.'}</ReactMarkdown></div>
				</div>
				<div className="card">
					<h3 className="card-title">Besonders hervorgehobene Mitglieder</h3>
					{debriefing.standoutCrewDetails?.length > 0 ? (
						<p>{debriefing.standoutCrewDetails.map(u => u.username).join(', ')}</p>
					) : <p>Niemand wurde besonders hervorgehoben.</p>}
				</div>
				<Link to="/admin/veranstaltungen" className="btn btn-secondary" style={{ marginTop: '1rem' }}>Zurück zur Event-Übersicht</Link>
			</div>
		);
	}


	if (!canManage) {
		return <div className="error-message">Sie haben keine Berechtigung, dieses Debriefing zu bearbeiten.</div>
	}

	return (
		<div>
			<h1><i className="fas fa-clipboard-check"></i> Debriefing für: {event.name}</h1>
			<p className="details-subtitle">Fassen Sie die wichtigsten Punkte der Veranstaltung zusammen.</p>
			{error && <p className="error-message">{error}</p>}
			<div className="card">
				<form onSubmit={handleSubmit}>
					<div className="form-group">
						<label htmlFor="whatWentWell">Was lief gut?</label>
						<textarea id="whatWentWell" name="whatWentWell" value={formData.whatWentWell} onChange={handleChange} rows="6" required></textarea>
					</div>
					<div className="form-group">
						<label htmlFor="whatToImprove">Was kann verbessert werden?</label>
						<textarea id="whatToImprove" name="whatToImprove" value={formData.whatToImprove} onChange={handleChange} rows="6" required></textarea>
					</div>
					<div className="form-group">
						<label htmlFor="equipmentNotes">Anmerkungen zum Material</label>
						<textarea id="equipmentNotes" name="equipmentNotes" value={formData.equipmentNotes} onChange={handleChange} rows="4"></textarea>
					</div>
					<div className="form-group">
						<label htmlFor="standoutCrewMemberIds">Besonders hervorgehobene Mitglieder (optional)</label>
						<select id="standoutCrewMemberIds" name="standoutCrewMemberIds" value={formData.standoutCrewMemberIds} onChange={handleMultiSelectChange} multiple style={{ height: '150px' }}>
							{event.assignedAttendees?.map(member => (
								<option key={member.id} value={member.id}>{member.username}</option>
							))}
						</select>
						<small>Halten Sie Strg (oder Cmd auf Mac) gedrückt, um mehrere Mitglieder auszuwählen.</small>
					</div>
					<div style={{ display: 'flex', gap: '1rem' }}>
						<button type="submit" className="btn btn-success" disabled={isSubmitting}>
							{isSubmitting ? 'Speichern...' : 'Debriefing speichern'}
						</button>
						{debriefing && <button type="button" className="btn btn-secondary" onClick={() => setIsEditing(false)} disabled={isSubmitting}>Abbrechen</button>}
					</div>
				</form>
			</div>
		</div>
	);
};

export default AdminEventDebriefingPage;