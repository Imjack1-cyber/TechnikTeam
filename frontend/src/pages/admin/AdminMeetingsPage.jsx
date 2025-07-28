import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import Modal from '@/components/ui/Modal';

const AdminMeetingsPage = () => {
    const { courseId } = useParams();
    const { data: meetingsData, loading, error, reload } = useApi(() => apiClient.get(`/meetings?courseId=${courseId}`));
    const { data: adminData } = useApi(() => apiClient.get('/users')); // Fetch all users for leader dropdown

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingMeeting, setEditingMeeting] = useState(null);
    const [formError, setFormError] = useState('');

    const courseName = meetingsData?.[0]?.parentCourseName || 'Lehrgang';

    const handleOpenNewModal = () => {
        setEditingMeeting(null);
        setIsModalOpen(true);
    };

    const handleOpenEditModal = (meeting) => {
        setEditingMeeting(meeting);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingMeeting(null);
        setFormError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        
        try {
            const result = editingMeeting
                ? await apiClient.post(`/meetings/${editingMeeting.id}`, formData)
                : await apiClient.post('/meetings', formData);
            
            if (result.success) {
                handleCloseModal();
                reload();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setFormError(err.message || 'Ein Fehler ist aufgetreten.');
        }
    };

    const handleDelete = async (meeting) => {
        if (window.confirm(`Meeting '${meeting.name}' wirklich löschen?`)) {
            try {
                await apiClient.delete(`/meetings/${meeting.id}`);
                reload();
            } catch (err) {
                alert(`Fehler: ${err.message}`);
            }
        }
    };
    
    return (
        <div>
            <h1>Meetings für "{courseName}"</h1>
            <Link to="/admin/lehrgaenge" style={{ marginBottom: '1rem', display: 'inline-block' }}>
                <i className="fas fa-arrow-left"></i> Zurück zu allen Vorlagen
            </Link>

            <div className="table-controls">
                <button onClick={handleOpenNewModal} className="btn btn-success">
                    <i className="fas fa-plus"></i> Neues Meeting planen
                </button>
            </div>

            <div className="desktop-table-wrapper">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Meeting-Name</th>
                            <th>Datum & Uhrzeit</th>
                            <th>Leitung</th>
                            <th>Aktionen</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading && <tr><td colSpan="4">Lade Meetings...</td></tr>}
                        {error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
                        {meetingsData?.map(meeting => (
                            <tr key={meeting.id}>
                                <td>{meeting.name}</td>
                                <td>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</td>
                                <td>{meeting.leaderUsername || 'N/A'}</td>
                                <td style={{ display: 'flex', gap: '0.5rem' }}>
                                    <button onClick={() => handleOpenEditModal(meeting)} className="btn btn-small">Bearbeiten & Anhänge</button>
                                    <button onClick={() => handleDelete(meeting)} className="btn btn-small btn-danger">Löschen</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {isModalOpen && (
                <Modal isOpen={isModalOpen} onClose={handleCloseModal} title={editingMeeting ? "Meeting bearbeiten" : "Neues Meeting planen"}>
                    <form onSubmit={handleSubmit} encType="multipart/form-data">
                        {formError && <p className="error-message">{formError}</p>}
                        <input type="hidden" name="courseId" value={courseId} />
                        <div className="form-group">
                            <label htmlFor="name-modal">Name des Meetings</label>
                            <input type="text" id="name-modal" name="name" defaultValue={editingMeeting?.name} required />
                        </div>
                        <div className="responsive-dashboard-grid">
                            <div className="form-group">
                                <label htmlFor="meetingDateTime-modal">Beginn</label>
                                <input type="datetime-local" id="meetingDateTime-modal" name="meetingDateTime" defaultValue={editingMeeting?.meetingDateTime.substring(0, 16)} required />
                            </div>
                            <div className="form-group">
                                <label htmlFor="endDateTime-modal">Ende (optional)</label>
                                <input type="datetime-local" id="endDateTime-modal" name="endDateTime" defaultValue={editingMeeting?.endDateTime?.substring(0, 16)} />
                            </div>
                        </div>
                        <div className="form-group">
                            <label htmlFor="leader-modal">Leitende Person</label>
                            <select name="leaderUserId" id="leader-modal" defaultValue={editingMeeting?.leaderUserId}>
                                <option value="">(Keine)</option>
                                {adminData?.data.map(user => <option key={user.id} value={user.id}>{user.username}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label htmlFor="attachment-modal">Neuen Anhang hochladen</label>
                            <input type="file" name="attachment" id="attachment-modal" />
                        </div>
                        <button type="submit" className="btn"><i className="fas fa-save"></i> Speichern</button>
                    </form>
                </Modal>
            )}
        </div>
    );
};

export default AdminMeetingsPage;