import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import UserModal from '../../components/admin/users/UserModal';
import useAdminData from '../../hooks/useAdminData';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const AdminUsersPage = () => {
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error, reload } = useApi(apiCall);
	const adminFormData = useAdminData();
	const { addToast } = useToast();

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingUser, setEditingUser] = useState(null);
	const [resetPasswordInfo, setResetPasswordInfo] = useState({ user: null, password: '' });

	const handleOpenNewUserModal = () => {
		setEditingUser(null);
		setIsModalOpen(true);
	};

	const handleOpenEditModal = (user) => {
		setEditingUser(user);
		setIsModalOpen(true);
	};

	const handleCloseModal = () => {
		setIsModalOpen(false);
		setEditingUser(null);
	};

	const handleClosePasswordModal = () => {
		setResetPasswordInfo({ user: null, password: '' });
	};

	const handleSuccess = () => {
		handleCloseModal();
		reload();
	};

	const handleDelete = async (user) => {
		if (window.confirm(`Benutzer '${user.username}' wirklich löschen?`)) {
			try {
				const result = await apiClient.delete(`/users/${user.id}`);
				if (result.success) {
					addToast('Benutzer erfolgreich gelöscht.', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	const handleResetPassword = async (user) => {
		if (window.confirm(`Passwort für '${user.username}' wirklich zurücksetzen?`)) {
			try {
				const result = await apiClient.post(`/users/${user.id}/reset-password`);
				if (result.success) {
					setResetPasswordInfo({ user: user, password: result.data.newPassword });
					addToast('Passwort zurückgesetzt.', 'success');
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Fehler: ${err.message}`, 'error');
			}
		}
	};

	const copyToClipboard = () => {
		navigator.clipboard.writeText(resetPasswordInfo.password);
		addToast('Passwort in die Zwischenablage kopiert.', 'info');
	};

	return (
		<div>
			<h1><i className="fas fa-users-cog"></i> Benutzerverwaltung</h1>
			<p>Verwalten Sie hier alle Benutzerkonten und deren individuelle Berechtigungen.</p>

			<div className="table-controls">
				<button onClick={handleOpenNewUserModal} className="btn btn-success">
					<i className="fas fa-user-plus"></i> Neuen Benutzer anlegen
				</button>
			</div>

			<div className="desktop-table-wrapper">
				<table className="data-table">
					<thead>
						<tr>
							<th>ID</th>
							<th>Benutzername</th>
							<th>Rolle</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="4">Lade Benutzer...</td></tr>}
						{error && <tr><td colSpan="4" className="error-message">{error}</td></tr>}
						{users?.map(user => (
							<tr key={user.id}>
								<td>{user.id}</td>
								<td>{user.username}</td>
								<td>{user.roleName}</td>
								<td style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
									<button onClick={() => handleOpenEditModal(user)} className="btn btn-small">Bearbeiten</button>
									<button onClick={() => handleResetPassword(user)} className="btn btn-small btn-warning">Passwort Reset</button>
									<button onClick={() => handleDelete(user)} className="btn btn-small btn-danger">Löschen</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{loading && <p>Lade Benutzer...</p>}
				{error && <p className="error-message">{error}</p>}
				{users?.map(user => (
					<div key={user.id} className="list-item-card">
						<h3 className="card-title">{user.username}</h3>
						<div className="card-row"><strong>ID:</strong> <span>{user.id}</span></div>
						<div className="card-row"><strong>Rolle:</strong> <span>{user.roleName}</span></div>
						<div className="card-actions">
							<button onClick={() => handleOpenEditModal(user)} className="btn btn-small">Bearbeiten</button>
							<button onClick={() => handleResetPassword(user)} className="btn btn-small btn-warning">Passwort Reset</button>
							<button onClick={() => handleDelete(user)} className="btn btn-small btn-danger">Löschen</button>
						</div>
					</div>
				))}
			</div>

			{isModalOpen && (
				<UserModal
					isOpen={isModalOpen}
					onClose={handleCloseModal}
					onSuccess={handleSuccess}
					user={editingUser}
					roles={adminFormData.roles}
					groupedPermissions={adminFormData.groupedPermissions}
					isLoadingData={adminFormData.loading}
				/>
			)}

			{resetPasswordInfo.user && (
				<Modal isOpen={!!resetPasswordInfo.user} onClose={handleClosePasswordModal} title="Passwort wurde zurückgesetzt">
					<p>Das neue, temporäre Passwort für <strong>{resetPasswordInfo.user.username}</strong> ist:</p>
					<div style={{ background: 'var(--bg-color)', padding: '1rem', borderRadius: 'var(--border-radius)', fontFamily: 'monospace', margin: '1rem 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
						<span>{resetPasswordInfo.password}</span>
						<button className="btn btn-small" onClick={copyToClipboard} title="In die Zwischenablage kopieren">
							<i className="fas fa-copy"></i>
						</button>
					</div>
					<p className="text-danger" style={{ fontWeight: 'bold' }}>Dieses Passwort wird nur einmal angezeigt! Bitte geben Sie es sicher an den Benutzer weiter.</p>
				</Modal>
			)}
		</div>
	);
};

export default AdminUsersPage;