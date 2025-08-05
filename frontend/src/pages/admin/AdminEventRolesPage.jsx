import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminEventRolesPage = () => {
    const apiCall = useCallback(() => apiClient.get('/admin/event-roles'), []);
    const { data: roles, loading, error, reload } = useApi(apiCall);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingRole, setEditingRole] = useState(null);
    const { addToast } = useToast();

    const openModal = (role = null) => {
        setEditingRole(role);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setEditingRole(null);
        setIsModalOpen(false);
    };

    const handleSuccess = () => {
        closeModal();
        reload();
    };

    const handleDelete = async (role) => {
        if (window.confirm(`Rolle "${role.name}" wirklich löschen? Sie wird von allen aktuellen Zuweisungen entfernt.`)) {
            try {
                const result = await apiClient.delete(`/admin/event-roles/${role.id}`);
                if (result.success) {
                    addToast('Rolle erfolgreich gelöscht', 'success');
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
            <h1><i className="fas fa-user-tag"></i> Event-Rollen verwalten</h1>
            <p>Definieren Sie hier wiederverwendbare Rollen, die Benutzern bei Events zugewiesen werden können.</p>
            <div className="table-controls">
                <button onClick={() => openModal()} className="btn btn-success">
                    <i className="fas fa-plus"></i> Neue Rolle
                </button>
            </div>
            <div className="desktop-table-wrapper">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Icon</th>
                            <th>Name</th>
                            <th>Beschreibung</th>
                            <th>Aktionen</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading && <tr><td colSpan="4">Lade Rollen...</td></tr>}
                        {error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
                        {roles?.map(role => (
                            <tr key={role.id}>
                                <td><i className={`fas ${role.iconClass}`} style={{ fontSize: '1.5rem' }}></i></td>
                                <td>{role.name}</td>
                                <td>{role.description}</td>
                                <td>
                                    <button onClick={() => openModal(role)} className="btn btn-small">Bearbeiten</button>
                                    <button onClick={() => handleDelete(role)} className="btn btn-small btn-danger" style={{ marginLeft: '0.5rem' }}>Löschen</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            {isModalOpen && (
                <RoleModal
                    isOpen={isModalOpen}
                    onClose={closeModal}
                    onSuccess={handleSuccess}
                    role={editingRole}
                />
            )}
        </div>
    );
};

const RoleModal = ({ isOpen, onClose, onSuccess, role }) => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError('');
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData.entries());

        try {
            const result = role
                ? await apiClient.put(`/admin/event-roles/${role.id}`, data)
                : await apiClient.post('/admin/event-roles', data);
            if (result.success) {
                addToast(`Rolle erfolgreich ${role ? 'aktualisiert' : 'erstellt'}.`, 'success');
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
        <Modal isOpen={isOpen} onClose={onClose} title={role ? 'Rolle bearbeiten' : 'Neue Rolle erstellen'}>
            <form onSubmit={handleSubmit}>
                {error && <p className="error-message">{error}</p>}
                <div className="form-group">
                    <label htmlFor="modal-name">Name der Rolle</label>
                    <input id="modal-name" name="name" defaultValue={role?.name} required />
                </div>
                <div className="form-group">
                    <label htmlFor="modal-desc">Beschreibung</label>
                    <textarea id="modal-desc" name="description" defaultValue={role?.description} rows="3"></textarea>
                </div>
                <div className="form-group">
                    <label htmlFor="modal-icon">Font Awesome Icon-Klasse</label>
                    <input id="modal-icon" name="iconClass" defaultValue={role?.iconClass || 'fa-user-tag'} placeholder="z.B. fa-user-tie" required />
                </div>
                <button type="submit" className="btn" disabled={isSubmitting}>
                    {isSubmitting ? 'Speichern...' : 'Speichern'}
                </button>
            </form>
        </Modal>
    );
};

export default AdminEventRolesPage;