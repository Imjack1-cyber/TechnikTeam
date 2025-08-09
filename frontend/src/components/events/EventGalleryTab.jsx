import React, { useCallback, useState } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Modal from '../ui/Modal';
import Lightbox from '../ui/Lightbox';
import './EventGallery.css';

const PhotoUploadModal = ({ isOpen, onClose, onSuccess, eventId }) => {
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [file, setFile] = useState(null);
	const [caption, setCaption] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		if (!file) {
			setError('Bitte wählen Sie eine Bilddatei aus.');
			return;
		}

		setIsSubmitting(true);
		setError('');

		const formData = new FormData();
		formData.append('file', file);
		formData.append('caption', caption);

		try {
			const result = await apiClient.post(`/public/events/${eventId}/gallery`, formData);
			if (result.success) {
				addToast('Foto erfolgreich hochgeladen!', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Upload fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neues Foto hochladen">
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="photo-file">Bilddatei</label>
					<input type="file" id="photo-file" name="file" onChange={e => setFile(e.target.files[0])} accept="image/jpeg,image/png,image/gif" required />
				</div>
				<div className="form-group">
					<label htmlFor="photo-caption">Bildunterschrift (optional)</label>
					<input type="text" id="photo-caption" name="caption" value={caption} onChange={e => setCaption(e.target.value)} maxLength="255" />
				</div>
				<button type="submit" className="btn" disabled={isSubmitting}>
					{isSubmitting ? 'Wird hochgeladen...' : 'Hochladen'}
				</button>
			</form>
		</Modal>
	);
};

const EventGalleryTab = ({ event, user }) => {
	const apiCall = useCallback(() => apiClient.get(`/public/events/${event.id}/gallery`), [event.id]);
	const { data: photos, loading, error, reload } = useApi(apiCall);
	const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
	const [lightboxSrc, setLightboxSrc] = useState('');
	const { addToast } = useToast();

	const isParticipant = event.assignedAttendees?.some(attendee => attendee.id === user.id);

	const handleDelete = async (photoId) => {
		if (window.confirm("Dieses Foto wirklich löschen?")) {
			try {
				const result = await apiClient.delete(`/public/events/gallery/${photoId}`);
				if (result.success) {
					addToast("Foto gelöscht", "success");
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	const getImagePath = (path) => {
		// The path from the DB might be like "event_galleries/1/xyz.jpg"
		// The API endpoint is /api/v1/public/files/images/{filename}
		// We only need the filename part.
		const filename = path.split('/').pop();
		return `/api/v1/public/files/images/${filename}`;
	};

	return (
		<div>
			{isParticipant && (
				<div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '1rem' }}>
					<button onClick={() => setIsUploadModalOpen(true)} className="btn btn-success">
						<i className="fas fa-upload"></i> Foto hochladen
					</button>
				</div>
			)}

			{loading && <p>Lade Galerie...</p>}
			{error && <p className="error-message">{error}</p>}

			{photos?.length === 0 ? (
				<p>Für diese Veranstaltung wurden noch keine Fotos hochgeladen.</p>
			) : (
				<div className="photo-gallery-grid">
					{photos?.map(photo => {
						const canDelete = user.isAdmin || user.id === event.leaderUserId || user.id === photo.uploaderUserId;
						const imageUrl = getImagePath(photo.filepath);
						return (
							<div key={photo.id} className="photo-card">
								<img src={imageUrl} alt={photo.caption || 'Event-Foto'} onClick={() => setLightboxSrc(imageUrl)} />
								<div className="photo-card-caption">
									<p>{photo.caption}</p>
									<small>Von: {photo.uploaderUsername}</small>
									{canDelete && <button className="delete-photo-btn" onClick={() => handleDelete(photo.id)} title="Löschen">×</button>}
								</div>
							</div>
						);
					})}
				</div>
			)}

			{isUploadModalOpen && (
				<PhotoUploadModal
					isOpen={isUploadModalOpen}
					onClose={() => setIsUploadModalOpen(false)}
					onSuccess={() => { setIsUploadModalOpen(false); reload(); }}
					eventId={event.id}
				/>
			)}
			{lightboxSrc && <Lightbox src={lightboxSrc} onClose={() => setLightboxSrc('')} />}
		</div>
	);
};

export default EventGalleryTab;