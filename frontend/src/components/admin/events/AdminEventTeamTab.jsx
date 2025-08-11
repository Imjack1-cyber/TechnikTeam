import React, { useState, useCallback, useMemo } from 'react';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';

const AdminEventTeamTab = ({ event, onTeamUpdate }) => {
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [assignedUsers, setAssignedUsers] = useState(event.assignedAttendees || []);

	const rolesApiCall = useCallback(() => apiClient.get('/admin/event-roles'), []);
	const { data: allRoles } = useApi(rolesApiCall);

	const availableUsersApiCall = useCallback(() => apiClient.get(`/users?eventId=${event.id}`), [event.id]);
	const { data: availableUsers } = useApi(availableUsersApiCall);

	const assignedUserIds = useMemo(() => new Set(assignedUsers.map(u => u.id)), [assignedUsers]);
	const unassignedAvailableUsers = useMemo(() => availableUsers?.filter(u => !assignedUserIds.has(u.id)) || [], [availableUsers, assignedUserIds]);

	const handleAssignUser = (user, roleId = null) => {
		setAssignedUsers(prev => [...prev, { ...user, assignedEventRoleId: roleId }]);
	};

	const handleUnassignUser = (userId) => {
		setAssignedUsers(prev => prev.filter(u => u.id !== userId));
	};

	const handleRoleChange = (userId, newRoleId) => {
		setAssignedUsers(prev => prev.map(u => u.id === userId ? { ...u, assignedEventRoleId: newRoleId } : u));
	};

	const handleSaveTeam = async () => {
		setIsSubmitting(true);
		const payload = assignedUsers.map(u => ({
			userId: u.id,
			roleId: u.assignedEventRoleId || null
		}));

		try {
			const result = await apiClient.post(`/events/${event.id}/assignments`, payload);
			if (result.success) {
				addToast('Team erfolgreich gespeichert!', 'success');
				onTeamUpdate(); // This will trigger a reload of the event data
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(`Fehler beim Speichern: ${err.message}`, 'error');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<div>
			<div className="table-controls" style={{ justifyContent: 'flex-end' }}>
				<button onClick={handleSaveTeam} className="btn btn-success" disabled={isSubmitting}>
					{isSubmitting ? 'Wird gespeichert...' : 'Team speichern'}
				</button>
			</div>
			<div className="responsive-dashboard-grid" style={{ alignItems: 'flex-start' }}>
				<div className="card">
					<h3 className="card-title">Angemeldete & qualifizierte Mitglieder</h3>
					<ul className="details-list">
						{unassignedAvailableUsers.map(user => (
							<li key={user.id}>
								<span>{user.username}</span>
								<button onClick={() => handleAssignUser(user)} className="btn btn-small btn-success">
									<i className="fas fa-plus"></i> Hinzufügen
								</button>
							</li>
						))}
					</ul>
				</div>
				<div className="card">
					<h3 className="card-title">Zugewiesenes Team</h3>
					<ul className="details-list">
						{assignedUsers.map(user => (
							<li key={user.id} style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
								<button onClick={() => handleUnassignUser(user.id)} className="btn btn-small btn-danger-outline" title="Entfernen">
									<i className="fas fa-times"></i>
								</button>
								<span style={{ flexGrow: 1 }}>{user.username}</span>
								<select
									value={user.assignedEventRoleId || ''}
									onChange={(e) => handleRoleChange(user.id, e.target.value ? parseInt(e.target.value) : null)}
									className="form-group"
									style={{ marginBottom: 0 }}
								>
									<option value="">(Unzugewiesen)</option>
									{allRoles?.map(role => (
										<option key={role.id} value={role.id}>{role.name}</option>
									))}
								</select>
							</li>
						))}
					</ul>
				</div>
			</div>
		</div>
	);
};

export default AdminEventTeamTab;