import React, { useState, useCallback } from 'react';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import './GroupChatModal.css';

const GroupChatModal = ({ isOpen, onClose, onCreateGroup }) => {
	const { data: users, loading } = useApi(useCallback(() => apiClient.get('/users'), []));
	const [groupName, setGroupName] = useState('');
	const [selectedUsers, setSelectedUsers] = useState(new Set());

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
		if (groupName && selectedUsers.size > 0) {
			onCreateGroup(groupName, Array.from(selectedUsers));
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neue Gruppe erstellen">
			<div className="form-group">
				<label htmlFor="group-name">Gruppenname</label>
				<input
					type="text"
					id="group-name"
					value={groupName}
					onChange={(e) => setGroupName(e.target.value)}
					placeholder="Name der neuen Gruppe"
				/>
			</div>
			<h4>Mitglieder auswählen</h4>
			<div className="user-selection-list">
				{loading && <p>Lade Benutzer...</p>}
				{users?.map(user => (
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
				<button className="btn" onClick={handleSubmit} disabled={!groupName || selectedUsers.size === 0}>
					Gruppe erstellen
				</button>
			</div>
		</Modal>
	);
};

export default GroupChatModal;