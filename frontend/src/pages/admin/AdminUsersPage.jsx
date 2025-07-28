import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import UserModal from '@/components/admin/users/UserModal';
import useAdminData from '@/hooks/useAdminData';

const AdminUsersPage = () => {
	const { data: users, loading, error, reload } = useApi(() => apiClient.get('/users'));
	const adminFormData = useAdminData(); // Fetch roles and permissions

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingUser, setEditingUser] = useState(null);

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

	const handleSuccess = () => {
		handleCloseModal();
		reload(); // Refresh the user list
	};

	const handleDelete = async (user) => {
		if (window.confirm(`Benutzer '${user.username}' wirklich löschen?`)) {
			try {
				await apiClient.delete(`/users/${user.id}`);
				reload();
			} catch (err) {
				alert(`Error: ${err.message}`);
			}
		}
	};

	const handleResetPassword = async (user) => {
		if (window.confirm(`Passwort für '${user.username}' wirklich zurücksetzen?`)) {
			try {
				const result = await apiClient.post(`/users/${user.id}/reset-password`);
				if (result.success) {
					alert(`Neues Passwort für ${user.username}: ${result.data.newPassword}`);
				}
			} catch (err) {
				alert(`Error: ${err.message}`);
			}
		}
	};

	const renderTable = () => {
		if (loading) return <tr><td colSpan="4">Lade Benutzer...</td></tr>;
		if (error) return <tr><td colSpan="4" className="error-message">{error}</td></tr>;
		if (!users || users.length === 0) return <tr><td colSpan="4">Keine Benutzer gefunden.</td></tr>;

		return users.map(user => (
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
		));
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
						{renderTable()}
					</tbody>
				</table>
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
		</div>
	);
};

export default AdminUsersPage;