import React, { useState, useEffect } from 'react';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import DynamicSkillRows from './DynamicSkillRows';
import DynamicItemRows from './DynamicItemRows';

const EventModal = ({ isOpen, onClose, onSuccess, event, adminFormData }) => {
	const isEditMode = !!event;
	const { users, courses, storageItems } = adminFormData;
	const [activeTab, setActiveTab] = useState('general');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const [formData, setFormData] = useState({
		name: '',
		eventDateTime: '',
		endDateTime: '',
		location: '',
		description: '',
		status: 'GEPLANT',
		leaderUserId: '',
		requiredRole: 'NUTZER',
	});
	const [skillRows, setSkillRows] = useState([{ requiredCourseId: '', requiredPersons: 1 }]);
	const [itemRows, setItemRows] = useState([{ itemId: '', quantity: 1 }]);
	const [file, setFile] = useState(null);

	useEffect(() => {
		if (isEditMode && event) {
			setFormData({
				name: event.name || '',
				eventDateTime: event.eventDateTime ? event.eventDateTime.substring(0, 16) : '',
				endDateTime: event.endDateTime ? event.endDateTime.substring(0, 16) : '',
				location: event.location || '',
				description: event.description || '',
				status: event.status || 'GEPLANT',
				leaderUserId: event.leaderUserId || '',
				requiredRole: 'NUTZER',
			});
			setSkillRows(event.skillRequirements?.length > 0 ? event.skillRequirements : []);
			setItemRows(event.reservedItems?.length > 0 ? event.reservedItems : []);
		}
	}, [event, isEditMode]);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const data = new FormData();
		const eventData = {
			...formData,
			requiredCourseIds: skillRows.map(r => r.requiredCourseId).filter(Boolean),
			requiredPersons: skillRows.map(r => r.requiredPersons).filter(Boolean),
			itemIds: itemRows.map(r => r.itemId).filter(Boolean),
			quantities: itemRows.map(r => r.quantity).filter(Boolean),
		};

		data.append('eventData', new Blob([JSON.stringify(eventData)], { type: 'application/json' }));
		if (file) {
			data.append('file', file);
		}

		try {
			const result = isEditMode
				? await apiClient.post(`/events/${event.id}`, data)
				: await apiClient.post('/events', data);
			if (result.success) {
				addToast(`Event erfolgreich ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Event bearbeiten" : "Neues Event erstellen"}>
			<div className="modal-tabs">
				<button className={`modal-tab-button ${activeTab === 'general' ? 'active' : ''}`} onClick={() => setActiveTab('general')}>Allgemein</button>
				<button className={`modal-tab-button ${activeTab === 'details' ? 'active' : ''}`} onClick={() => setActiveTab('details')}>Details & Bedarf</button>
			</div>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className={`modal-tab-content ${activeTab === 'general' ? 'active' : ''}`}>
					<div className="form-group"><label>Name</label><input name="name" value={formData.name} onChange={handleChange} required /></div>
					<div className="responsive-dashboard-grid">
						<div className="form-group"><label>Beginn</label><input type="datetime-local" name="eventDateTime" value={formData.eventDateTime} onChange={handleChange} required /></div>
						<div className="form-group"><label>Ende</label><input type="datetime-local" name="endDateTime" value={formData.endDateTime} onChange={handleChange} /></div>
					</div>
					<div className="form-group"><label>Ort</label><input name="location" value={formData.location} onChange={handleChange} /></div>
					<div className="form-group"><label>Beschreibung</label><textarea name="description" value={formData.description} onChange={handleChange} rows="4"></textarea></div>
				</div>

				<div className={`modal-tab-content ${activeTab === 'details' ? 'active' : ''}`}>
					<div className="form-group"><label>Status</label><select name="status" value={formData.status} onChange={handleChange}><option value="GEPLANT">Geplant</option><option value="LAUFEND">Laufend</option><option value="ABGESCHLOSSEN">Abgeschlossen</option><option value="ABGESAGT">Abgesagt</option></select></div>
					<div className="form-group"><label>Leitung</label><select name="leaderUserId" value={formData.leaderUserId} onChange={handleChange}><option value="">(Keine)</option>{users?.map(u => <option key={u.id} value={u.id}>{u.username}</option>)}</select></div>
					<div className="form-group"><label>Anhang (optional)</label><input type="file" name="file" onChange={(e) => setFile(e.target.files[0])} /></div>
					<h4>Personalbedarf</h4>
					<DynamicSkillRows rows={skillRows} setRows={setSkillRows} courses={courses} />
					<h4 style={{ marginTop: '1.5rem' }}>Materialreservierung</h4>
					<DynamicItemRows rows={itemRows} setRows={setItemRows} storageItems={storageItems} />
				</div>

				<button type="submit" className="btn" style={{ marginTop: '1.5rem' }} disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Event speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default EventModal;