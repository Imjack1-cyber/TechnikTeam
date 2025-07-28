import React, { useState, useEffect } from 'react';
import Modal from '../../ui/Modal';
import PermissionsTab from './PermissionTab';
import apiClient from '../../../services/apiClient';

const UserModal = ({ isOpen, onClose, onSuccess, user, roles, groupedPermissions, isLoadingData }) => {
	const [activeTab, setActiveTab] = useState('general');
	const [formData, setFormData] = useState({});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const isEditMode = !!user;

	useEffect(() => {
		if (isEditMode && user) {
			const fetchUserData = async () => {
				try {
					const result = await apiClient.get(`/users/${user.id}`);
					if (result.success) {
						setFormData({
							username: result.data.username || '',
							roleId: result.data.roleId || '',
							classYear: result.data.classYear || '',
							className: result.data.className || '',
							email: result.data.email || '',
							permissionIds: new Set(result.data.permissions.map(p => p.id))
						});
					}
				} catch (err) {
					setError('Could not load user details.');
				}
			};
			fetchUserData();
		} else {
			setFormData({
				username: '',
				password: '',
				roleId: roles.find(r => r.roleName === 'NUTZER')?.id || '',
				classYear: '',
				className: '',
				email: '',
				permissionIds: new Set()
			});
		}
	}, [user, isEditMode, roles]);

	const handleChange = (e) => {
		setFormData({ ...formData, [e.target.name]: e.target.value });
	};

	const handlePermissionChange = (permissionId) => {
		setFormData(prev => {
			const newPermissionIds = new Set(prev.permissionIds);
			if (newPermissionIds.has(permissionId)) {
				newPermissionIds.delete(permissionId);
			} else {
				newPermissionIds.add(permissionId);
			}
			return { ...prev, permissionIds: newPermissionIds };
		});
	};

	const handleSubmit = async (e) => {
		e.preventDefault();
		setIsSubmitting(true);
		setError('');

		const payload = {
			...formData,
			permissionIds: Array.from(formData.permissionIds)
		};

		if (!isEditMode && !payload.password) {
			setError('Password is required for a new user.');
			setIsSubmitting(false);
			return;
		}

		try {
			const result = isEditMode
				? await apiClient.put(`/users/${user.id}`, payload)
				: await apiClient.post('/users', payload);

			if (result.success) {
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'An error occurred.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? `Benutzer bearbeiten: ${user.username}` : 'Neuen Benutzer anlegen'}>
			<div className="modal-tabs">
				<button className={`modal-tab-button ${activeTab === 'general' ? 'active' : ''}`} onClick={() => setActiveTab('general')}>Allgemein</button>
				<button className={`modal-tab-button ${activeTab === 'permissions' ? 'active' : ''}`} onClick={() => setActiveTab('permissions')}>Berechtigungen</button>
			</div>
			<form onSubmit={handleSubmit}>
				{error && <p className="error-message">{error}</p>}
				<div className={`modal-tab-content ${activeTab === 'general' ? 'active' : ''}`}>
					<div className="form-group">
						<label htmlFor="username-modal">Benutzername</label>
						<input type="text" id="username-modal" name="username" value={formData.username || ''} onChange={handleChange} required />
					</div>
					{!isEditMode && (
						<div className="form-group">
							<label htmlFor="password-modal">Passwort</label>
							<input type="password" id="password-modal" name="password" value={formData.password || ''} onChange={handleChange} minLength="10" />
							<small className="text-muted">Muss 10+ Zeichen, Groß/Kleinbuchstaben, Zahlen & Sonderzeichen enthalten.</small>
						</div>
					)}
					<div className="form-group">
						<label htmlFor="role-modal">Rolle</label>
						<select name="roleId" id="role-modal" value={formData.roleId || ''} onChange={handleChange} required>
							{roles.map(role => <option key={role.id} value={role.id}>{role.roleName}</option>)}
						</select>
					</div>
					<div className="form-group">
						<label htmlFor="email-modal">E-Mail</label>
						<input type="email" id="email-modal" name="email" value={formData.email || ''} onChange={handleChange} />
					</div>
				</div>

				<div className={`modal-tab-content ${activeTab === 'permissions' ? 'active' : ''}`}>
					<PermissionsTab
						groupedPermissions={groupedPermissions}
						assignedIds={formData.permissionIds || new Set()}
						onPermissionChange={handlePermissionChange}
						isLoading={isLoadingData}
					/>
				</div>

				<button type="submit" className="btn" style={{ marginTop: '1.5rem' }} disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Benutzer speichern'}
				</button>
			</form>
		</Modal>
	);
};

export default UserModal;