import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import UserModal from '../../components/admin/users/UserModal';
import useAdminData from '../../hooks/useAdminData';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';

const SuspendUserModal = ({ isOpen, onClose, user, onSuccess }) => {
	const [duration, setDuration] = useState('7d');
	const [reason, setReason] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/users/${user.id}/suspend`, { duration, reason });
			if (result.success) {
				addToast(`Benutzer ${user.username} wurde gesperrt.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Sperren fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};


	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Benutzer sperren: ${user.username}`}>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className="form-group">
					<label htmlFor="duration">Dauer</label>
					<input type="text" id="duration" value={duration} onChange={e => setDuration(e.target.value)} placeholder="z.B. 1h, 7d, indefinite" />
					<small>Einheiten: h (Stunden), d (Tage), w (Wochen). Leer lassen für unbegrenzt.</small>
				</div>
				<div className="form-group">
					<label htmlFor="reason">Grund</label>
					<textarea id="reason" value={reason} onChange={e => setReason(e.target.value)} rows="3"></textarea>
				</div>
				<button type="submit" className="btn btn-danger" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gesperrt...' : 'Benutzer sperren'}
				</button>
			</form>
		</Modal>
	);
};

const AdminUsersPage = () => {
	const apiCall = useCallback(() => apiClient.get('/users'), []);
	const { data: users, loading, error, reload } = useApi(apiCall);
	const adminFormData = useAdminData();
	const { addToast } = useToast();

	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingUser, setEditingUser] = useState(null);
	const [suspendingUser, setSuspendingUser] = useState(null);
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
		setSuspendingUser(null);
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

	const handleUnsuspend = async (user) => {
		if (window.confirm(`Benutzer '${user.username}' wirklich entsperren? Dies hebt auch eine eventuelle Sperre durch zu viele Login-Versuche auf.`)) {
			try {
				const result = await apiClient.post(`/admin/users/${user.id}/unsuspend`);
				if (result.success) {
					addToast('Benutzer erfolgreich entsperrt.', 'success');
					reload();
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(`Entsperren fehlgeschlagen: ${err.message}`, 'error');
			}
		}
	};

	const copyToClipboard = () => {
		if (navigator.clipboard && window.isSecureContext) {
			// Modern, secure way
			navigator.clipboard.writeText(resetPasswordInfo.password)
				.then(() => addToast('Passwort in die Zwischenablage kopiert.', 'info'))
				.catch(err => addToast('Kopieren fehlgeschlagen.', 'error'));
		} else {
			// Fallback for insecure contexts (like HTTP) or older browsers
			const textArea = document.createElement("textarea");
			textArea.value = resetPasswordInfo.password;
			document.body.appendChild(textArea);
			textArea.focus();
			textArea.select();
			try {
				document.execCommand('copy');
				addToast('Passwort in die Zwischenablage kopiert.', 'info');
			} catch (err) {
				addToast('Kopieren fehlgeschlagen.', 'error');
			}
			document.body.removeChild(textArea);
		}
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
							<th>Status</th>
							<th>Aktionen</th>
						</tr>
					</thead>
					<tbody>
						{loading && <tr><td colSpan="5">Lade Benutzer...</td></tr>}
						{error && <tr><td colSpan="5" className="error-message">{error}</td></tr>}
						{users?.map(user => {
							const isLocked = user.isLocked || user.status === 'SUSPENDED';
							return (
								<tr key={user.id}>
									<td>{user.id}</td>
									<td>{user.username}</td>
									<td>{user.roleName}</td>
									<td>
										{isLocked
											? <span className="status-badge status-danger">Gesperrt</span>
											: <span className="status-badge status-ok">Aktiv</span>
										}
									</td>
									<td style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
										<button onClick={() => handleOpenEditModal(user)} className="btn btn-small">Bearbeiten</button>
										<Link to={`/team/${user.id}`} className="btn btn-small btn-info">Profil ansehen</Link>
										<button onClick={() => handleResetPassword(user)} className="btn btn-small btn-secondary">Passwort Reset</button>
										{isLocked
											? <button onClick={() => handleUnsuspend(user)} className="btn btn-small btn-success">Entsperren</button>
											: <button onClick={() => setSuspendingUser(user)} className="btn btn-small btn-warning" disabled={user.roleName === 'ADMIN'}>Sperren</button>
										}
										<button onClick={() => handleDelete(user)} className="btn btn-small btn-danger">Löschen</button>
									</td>
								</tr>
							);
						})}
					</tbody>
				</table>
			</div>

			<div className="mobile-card-list">
				{loading && <p>Lade Benutzer...</p>}
				{error && <p className="error-message">{error}</p>}
				{users?.map(user => {
					const isLocked = user.isLocked || user.status === 'SUSPENDED';
					return (
						<div key={user.id} className="list-item-card">
							<h3 className="card-title">{user.username}</h3>
							<div className="card-row"><strong>ID:</strong> <span>{user.id}</span></div>
							<div className="card-row"><strong>Rolle:</strong> <span>{user.roleName}</span></div>
							<div className="card-row"><strong>Status:</strong>
								{isLocked
									? <span className="status-badge status-danger">Gesperrt</span>
									: <span className="status-badge status-ok">Aktiv</span>
								}
							</div>
							<div className="card-actions">
								<button onClick={() => handleOpenEditModal(user)} className="btn btn-small">Bearbeiten</button>
								<Link to={`/team/${user.id}`} className="btn btn-small btn-info">Profil</Link>
								<button onClick={() => handleResetPassword(user)} className="btn btn-small btn-secondary">Reset</button>
								{isLocked
									? <button onClick={() => handleUnsuspend(user)} className="btn btn-small btn-success">Entsperren</button>
									: <button onClick={() => setSuspendingUser(user)} className="btn btn-small btn-warning" disabled={user.roleName === 'ADMIN'}>Sperren</button>
								}
								<button onClick={() => handleDelete(user)} className="btn btn-small btn-danger">Löschen</button>
							</div>
						</div>
					);
				})}
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

			{suspendingUser && (
				<SuspendUserModal
					isOpen={!!suspendingUser}
					onClose={handleCloseModal}
					onSuccess={handleSuccess}
					user={suspendingUser}
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