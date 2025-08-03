import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import StatusBadge from '../components/ui/StatusBadge';
import { useToast } from '../context/ToastContext';

const EventsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/events'), []);
	const { data: events, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();

	const [modalState, setModalState] = useState({
		isOpen: false,
		type: null,
		event: null,
		customFields: [],
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [formError, setFormError] = useState('');

	const openSignupModal = async (event) => {
		try {
			const result = await apiClient.get(`/public/events/${event.id}/custom-fields`);
			setModalState({
				isOpen: true,
				type: 'signup',
				event,
				customFields: result.success ? result.data : [],
			});
		} catch (e) {
			console.error("Benutzerdefinierte Felder konnten nicht geladen werden", e);
			setModalState({ isOpen: true, type: 'signup', event, customFields: [] });
		}
	};

	const openSignoffModal = (event) => {
		setModalState({ isOpen: true, type: 'signoff', event, customFields: [] });
	};

	const closeModal = () => {
		setModalState({ isOpen: false, type: null, event: null, customFields: [] });
		setFormError('');
	};

	const handleSignupSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setFormError('');
		const formData = new FormData(e.target);
		const customFieldData = Object.fromEntries(formData.entries());

		try {
			const result = await apiClient.post(`/public/events/${modalState.event.id}/signup`, customFieldData);
			if (result.success) {
				addToast('Erfolgreich angemeldet!', 'success');
				closeModal();
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setFormError(err.message || 'Anmeldung fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const handleSignoffSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setFormError('');
		const reason = new FormData(e.target).get('reason') || '';

		try {
			const result = await apiClient.post(`/public/events/${modalState.event.id}/signoff`, { reason });
			if (result.success) {
				addToast('Erfolgreich abgemeldet.', 'success');
				closeModal();
				reload();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setFormError(err.message || 'Abmeldung fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};


	const getAction = (event) => {
		if (event.userAttendanceStatus === 'OFFEN' || event.userAttendanceStatus === 'ABGEMELDET') {
			return (
				<button
					type="button"
					className="btn btn-small btn-success"
					onClick={() => openSignupModal(event)}
					disabled={!event.userQualified}
					title={!event.userQualified ? 'Du erfüllst die Anforderungen für dieses Event nicht.' : ''}
				>
					Anmelden
				</button>
			);
		}
		if (event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN') {
			return (
				<button type="button" className="btn btn-small btn-danger" onClick={() => openSignoffModal(event)}>
					Abmelden
				</button>
			);
		}
		return null;
	};

	const getUserStatusText = (status) => {
		if (status === 'ZUGEWIESEN') return <strong className="text-success">Zugewiesen</strong>;
		if (status === 'ANGEMELDET') return <strong style={{ color: 'var(--primary-color)' }}>Angemeldet</strong>;
		return status;
	};

	if (loading) return <div>Lade Veranstaltungen...</div>;
	if (error) return <div className="error-message">{error}</div>;

	return (
		<>
			<h1><i className="fas fa-calendar-check"></i> Anstehende Veranstaltungen</h1>
			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Veranstaltung</th>
							<th>Datum & Uhrzeit</th>
							<th>Event-Status</th>
							<th>Dein Status</th>
							<th>Aktion</th>
						</tr>
					</thead>
					<tbody>
						{events.map(event => (
							<tr key={event.id}>
								<td><Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link></td>
								<td>{new Date(event.eventDateTime).toLocaleString('de-DE')}</td>
								<td><StatusBadge status={event.status} /></td>
								<td>{getUserStatusText(event.userAttendanceStatus)}</td>
								<td>{getAction(event)}</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<Modal
				isOpen={modalState.isOpen}
				onClose={closeModal}
				title={modalState.type === 'signup' ? `Anmeldung für: ${modalState.event?.name}` : `Abmeldung von: ${modalState.event?.name}`}
			>
				{formError && <p className="error-message">{formError}</p>}
				{modalState.type === 'signup' && (
					<form onSubmit={handleSignupSubmit}>
						{modalState.customFields.map(field => (
							<div className="form-group" key={field.id}>
								<label htmlFor={`customfield_${field.id}`}>{field.fieldName}{field.required ? '*' : ''}</label>
								<input type="text" id={`customfield_${field.id}`} name={`customfield_${field.id}`} required={field.required} />
							</div>
						))}
						<button type="submit" className="btn btn-success" disabled={isSubmitting}>
							{isSubmitting ? 'Wird angemeldet...' : 'Verbindlich anmelden'}
						</button>
					</form>
				)}
				{modalState.type === 'signoff' && (
					<form onSubmit={handleSignoffSubmit}>
						{modalState.event?.status === 'LAUFEND' && (
							<>
								<p className="info-message">Da das Event bereits läuft, ist eine Begründung für die Abmeldung erforderlich.</p>
								<div className="form-group">
									<label htmlFor="signoff-reason">Begründung</label>
									<textarea id="signoff-reason" name="reason" rows="3" required></textarea>
								</div>
							</>
						)}
						{modalState.event?.status !== 'LAUFEND' && <p>Möchtest du dich wirklich von diesem Event abmelden?</p>}
						<button type="submit" className="btn btn-danger" disabled={isSubmitting}>
							{isSubmitting ? 'Wird abgemeldet...' : 'Jetzt abmelden'}
						</button>
					</form>
				)}
			</Modal>
		</>
	);
};

export default EventsPage;