import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminVenuesPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/venues'), []);
	const { data: venues, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingVenue, setEditingVenue] = useState(null);
	const { addToast } = useToast();

	const openModal = (venue = null) => {
		setEditingVenue(venue);
		setIsModalOpen(true);
	};

	const closeModal = () => {
		setEditingVenue(null);
		setIsModalOpen(false);
	};

	const handleSuccess = () => {
		closeModal();
		reload();
	};

	const handleDelete = async (venue) => {
		if (window.confirm(`Veranstaltungsort "${venue.name}" wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/admin/venues/${venue.id}`);
				if (result.success) {
					addToast('Ort erfolgreich gelöscht', 'success');
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
			<h1><i className="fas fa-map-marked-alt"></i> Veranstaltungsorte verwalten</h1>
			<p>Verwalten Sie hier die Orte und die zugehörigen Raumpläne.</p>
			<div className="table-controls">
				<button onClick={() => openModal()} className="btn btn-success">
					<i className="fas fa-plus"></i> Neuer Ort
				</button>
			</div>

			{loading && <p>Lade Orte...</p>}
			{error && <p className="error-message">{error}</p>}

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>Name</th>
							<th>Adresse</th>
							<th>Kartenbild</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{venues?.map(venue => (
							<tr key={venue.id}>
								<td>{venue.name}</td>
								<td>{venue.address}</td>
								<td>{venue.mapImagePath ? 'Ja' : 'Nein'}</td>
								<td>
									<button onClick={() => openModal(venue)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleDelete(venue)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>
			<div className="mobile-card-list">
				{venues?.map(venue => (
					<div className="list-item-card" key={venue.id}>
						<h3 className="card-title">{venue.name}</h3>
						<div className="card-row"><strong>Adresse:</strong> <span>{venue.address || '-'}</span></div>
						<div className="card-row"><strong>Karte:</strong> <span>{venue.mapImagePath ? 'Ja' : 'Nein'}</span></div>
						<div className="card-actions">
							<button onClick={() => openModal(venue)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleDelete(venue)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>
			{isModalOpen && (
				<VenueModal
					isOpen={isModalOpen}
					onClose={closeModal}
					onSuccess={handleSuccess}
					venue={editingVenue}
				/>
			)}
		</div>
	);
};

const VenueModal = ({ isOpen, onClose, onSuccess, venue }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const formData = new FormData();
		const venueData = {
			name: e.target.name.value,
			address: e.target.address.value,
			notes: e.target.notes.value,
			mapImagePath: venue?.mapImagePath // Preserve existing image path
		};
		formData.append('venue', new Blob([JSON.stringify(venueData)], { type: 'application/json' }));
		if (e.target.mapImage.files[0]) {
			formData.append('mapImage', e.target.mapImage.files[0]);
		}

		try {
			const endpoint = venue ? `/admin/venues/${venue.id}` : '/admin/venues';
			// Since apiClient doesn't have a dedicated `put` for FormData, we use `request`
			const result = await apiClient.request(endpoint, {
				method: venue ? 'PUT' : 'POST',
				body: formData
			});

			if (result.success) {
				addToast(`Ort erfolgreich ${venue ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			}
			else throw new Error(result.message);
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={venue ? 'Ort bearbeiten' : 'Neuen Ort erstellen'}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label>Name des Ortes</label>
					<input name="name" defaultValue={venue?.name} required />
				</div>
				<div className="form-group">
					<label>Adresse (optional)</label>
					<input name="address" defaultValue={venue?.address} />
				</div>
				<div className="form-group">
					<label>Notizen (z.B. Kontaktperson, Besonderheiten)</label>
					<textarea name="notes" defaultValue={venue?.notes} rows="3"></textarea>
				</div>
				<div className="form-group">
					<label>Raumplan / Kartenbild (optional)</label>
					<input type="file" name="mapImage" accept="image/*" />
					{venue?.mapImagePath && <small>Aktuelles Bild wird überschrieben, wenn eine neue Datei ausgewählt wird.</small>}
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Speichern...' : 'Speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default AdminVenuesPage;