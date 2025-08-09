import React, { useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import useAdminData from '../../hooks/useAdminData';
import Modal from '../../components/ui/Modal';
import StatusBadge from '../../components/ui/StatusBadge';
import EventModal from '../../components/admin/events/EventModal';
import { useToast } from '../../context/ToastContext';

const AdminEventsPage = () => {
	const eventsApiCall = useCallback(() => apiClient.get('/events'), []);
	const templatesApiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);

	const { data: events, loading: eventsLoading, error: eventsError, reload } = useApi(eventsApiCall);
	const { data: templates, loading: templatesLoading } = useApi(templatesApiCall);
	const adminFormData = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingEvent, setEditingEvent] = useState(null);
	const { addToast } = useToast();
	const navigate = useNavigate();

	const openModal = (event = null) => {
		setEditingEvent(event);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingEvent(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (event) => {
		if (window.confirm(`Event '${event.name}' wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/events/${event.id}`);
				if (result.success) {
					addToast('Event erfolgreich gelöscht', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	const handleClone = async (event) => {
		if (window.confirm(`Event '${event.name}' klonen? Ein neues Event wird erstellt und Sie werden zur Bearbeitungsseite weitergeleitet.`)) {
			try {
				const result = await apiClient.post(`/events/${event.id}/clone`);
				if (result.success) {
					addToast('Event erfolgreich geklont.', 'success');
					// Redirect to the edit page of the new event
					// This requires the backend to return the new event object
					// For now, just reload the list. A redirect would be better.
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Klonen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	const loading = eventsLoading || adminFormData.loading || templatesLoading;

	return (
		<div>
			<h1><i className="fas fa-calendar-plus"></i> Event-Verwaltung</h1>
			<p>Hier können alle Veranstaltungen geplant, bearbeitet und verwaltet werden.</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neues Event erstellen
				</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Event-Name</th>
							<th>Datum & Uhrzeit</th>
							<th>Status</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="4">Lade Events...</td></tr>}
						{eventsError && <tr><td colSpan="4" className="error-message">{eventsError}</td></tr>}
						{events?.map(event => (
							<tr key={event.id}>
								<td><Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link></td>
								<td>{new Date(event.eventDateTime).toLocaleString('de-DE')}</td>
								<td><StatusBadge status={event.status} /></td>
								<td style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
									<button onClick={() => openModal(event)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleClone(event)} className="btn btn-small btn-secondary">Klonen</button>
									{event.status === 'ABGESCHLOSSEN' && (
										<Link to={`/admin/veranstaltungen/${event.id}/debriefing`} className="btn btn-small btn-info">
											Debriefing
										</Link>
									)}
									<button onClick={() => handleDelete(event)} className="btn btn-small btn-danger">Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{loading && <p>Lade Events...</p>}
				{eventsError && <p className="error-message">{eventsError}</p>}
				{events?.map(event => (
					<div className="list-item-card" key={event.id}>
						<h3 className="card-title"><Link to={`/veranstaltungen/details/${event.id}`}>{event.name}</Link></h3>
						<div className="card-row"><strong>Datum:</strong> <span>{new Date(event.eventDateTime).toLocaleString('de-DE')}</span></div>
						<div className="card-row"><strong>Status:</strong> <StatusBadge status={event.status} /></div>
						<div className="card-actions">
							<button onClick={() => openModal(event)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleClone(event)} className="btn btn-small btn-secondary">Klonen</button>
							{event.status === 'ABGESCHLOSSEN' && (
								<Link to={`/admin/veranstaltungen/${event.id}/debriefing`} className="btn btn-small btn-info">
									Debriefing
								</Link>
							)}
							<button onClick={() => handleDelete(event)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>

			{/* Prevent modal from rendering before its required data is available */}
			{isModalOpen && !loading && (
				<EventModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					event={editingEvent}
					adminFormData={adminFormData}
					checklistTemplates={templates || []}
				/>
			)}
		</div>
	);
};

export default AdminEventsPage;