import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import './GroupChatModal.css'; // Reuse styles
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';

const ManageParticipantsModal = ({ isOpen, onClose, onAddUsers, onRemoveUser, conversation }) => {
	const user = useAuthStore(state => state.user);
	const { data: allUsers, loading } = useApi(useCallback(() => apiClient.get('/users'), []));
	const [selectedUsers, setSelectedUsers] = useState(new Set());
	const { addToast } = useToast();


	const existingParticipantIds = new Set(conversation.participants.map(p => p.id));
	const usersToAdd = allUsers?.filter(u => !existingParticipantIds.has(u.id));

	const handleToggleUser = (userId) => {
		setSelectedUsers(prev => {
			const newSet = new Set(prev);
			if (newSet.has(userId)) {
				newSet.delete(userId);
			} else {
				newSet.add(userId);
			}
			return newSet;
		});
	};

	const handleSubmit = () => {
		if (selectedUsers.size > 0) {
			onAddUsers(Array.from(selectedUsers));
		}
	};

	const handleRemoveClick = async (participant) => {
		if (window.confirm(`${participant.username} wirklich aus der Gruppe entfernen?`)) {
			try {
				const result = await apiClient.delete(`/public/chat/conversations/${conversation.id}/participants/${participant.id}`);
				if (result.success) {
					addToast(`${participant.username} wurde entfernt.`, 'success');
					onRemoveUser(); // This should trigger a reload in the parent component
				} else {
					throw new Error(result.message);
				}
			} catch (err) {
				addToast(err.message, 'error');
			}
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`"${conversation.name}" verwalten`}>
			<h4>Aktuelle Mitglieder</h4>
			<ul className="participant-list">
				{conversation.participants.map(p => (
					<li key={p.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
						<span>{p.username} {p.id === conversation.creatorId && '(Ersteller)'}</span>
						{user.id !== p.id && ( // Can't remove yourself
							<button onClick={() => handleRemoveClick(p)} className="btn btn-small btn-danger-outline" title="Entfernen">
								<i className="fas fa-times"></i>
							</button>
						)}
					</li>
				))}
			</ul>
			<hr />
			<h4>Mitglieder hinzufügen</h4>
			<div className="user-selection-list">
				{loading && <p>Lade Benutzer...</p>}
				{usersToAdd?.length === 0 && <p>Alle Benutzer sind bereits in dieser Gruppe.</p>}
				{usersToAdd?.map(user => (
					<label key={user.id} className="user-selection-item">
						<input
							type="checkbox"
							checked={selectedUsers.has(user.id)}
							onChange={() => handleToggleUser(user.id)}
						/>
						{user.username}
					</label>
				))}
			</div>
			<div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'flex-end' }}>
				<button className="btn" onClick={handleSubmit} disabled={selectedUsers.size === 0}>
					Ausgewählte hinzufügen
				</button>
			</div>
		</Modal>
	);
};

export default ManageParticipantsModal;