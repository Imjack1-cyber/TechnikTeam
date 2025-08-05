import React, { useCallback, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import './ConversationList.css';
import Modal from '../ui/Modal';
import GroupChatModal from './GroupChatModal';
import { useToast } from '../../context/ToastContext';

const UserSearchModal = ({ isOpen, onClose, onSelectUser }) => {
	const { data: users, loading } = useApi(useCallback(() => apiClient.get('/users'), []));

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neues Gespräch starten">
			<div className="user-search-list">
				{loading && <p>Lade Benutzer...</p>}
				{users?.map(user => (
					<div key={user.id} className="user-search-item" onClick={() => onSelectUser(user.id)}>
						{user.username}
					</div>
				))}
			</div>
		</Modal>
	);
};


const ConversationList = ({ selectedConversationId }) => {
	const apiCall = useCallback(() => apiClient.get('/public/chat/conversations'), []);
	const { data: conversations, loading, error, reload } = useApi(apiCall);
	const [isUserSearchModalOpen, setIsUserSearchModalOpen] = useState(false);
	const [isGroupChatModalOpen, setIsGroupChatModalOpen] = useState(false);
	const navigate = useNavigate();
	const { addToast } = useToast();

	const handleSelectUser = async (userId) => {
		setIsUserSearchModalOpen(false);
		try {
			const result = await apiClient.post('/public/chat/conversations', { userId });
			if (result.success && result.data.conversationId) {
				navigate(`/chat/${result.data.conversationId}`);
			} else {
				throw new Error(result.message || 'Gespräch konnte nicht gestartet werden.');
			}
		} catch (err) {
			addToast(err.message, 'error');
		}
	};

	const handleCreateGroup = async (name, participantIds) => {
		setIsGroupChatModalOpen(false);
		try {
			const result = await apiClient.post('/public/chat/conversations/group', { name, participantIds });
			if (result.success && result.data.conversationId) {
				addToast('Gruppe erfolgreich erstellt!', 'success');
				reload();
				navigate(`/chat/${result.data.conversationId}`);
			} else {
				throw new Error(result.message || 'Gruppe konnte nicht erstellt werden.');
			}
		} catch (err) {
			addToast(err.message, 'error');
		}
	};

	return (
		<div className="conversation-list-container">
			<div className="conversation-list-header">
				<h3>Gespräche</h3>
				<div className="conversation-actions">
					<button onClick={() => setIsUserSearchModalOpen(true)} className="btn btn-small" title="Neues Einzelgespräch">
						<i className="fas fa-user-plus"></i>
					</button>
					<button onClick={() => setIsGroupChatModalOpen(true)} className="btn btn-small" title="Neue Gruppe">
						<i className="fas fa-users"></i>
					</button>
				</div>
			</div>
			<div className="conversation-list">
				{loading && <p className="loading-text">Lade Gespräche...</p>}
				{error && <p className="error-message">{error}</p>}
				{!loading && conversations?.length === 0 && (
					<div className="empty-conversations-message">
						<p>Keine Gespräche vorhanden.</p>
						<p>Starte ein neues Gespräch oder eine neue Gruppe!</p>
					</div>
				)}
				{conversations?.map(conv => (
					<Link to={`/chat/${conv.id}`} key={conv.id} className={`conversation-item ${conv.id.toString() === selectedConversationId ? 'active' : ''}`}>
						<div className="conversation-icon">
							<i className={`fas ${conv.groupChat ? 'fa-users' : 'fa-user'}`}></i>
						</div>
						<div className="conversation-details">
							<span className="conversation-username">{conv.groupChat ? conv.name : conv.otherParticipantUsername}</span>
							<span className="conversation-snippet">{conv.lastMessage}</span>
						</div>
					</Link>
				))}
			</div>
			{isUserSearchModalOpen && <UserSearchModal isOpen={isUserSearchModalOpen} onClose={() => setIsUserSearchModalOpen(false)} onSelectUser={handleSelectUser} />}
			{isGroupChatModalOpen && <GroupChatModal isOpen={isGroupChatModalOpen} onClose={() => setIsGroupChatModalOpen(false)} onCreateGroup={handleCreateGroup} />}
		</div>
	);
};

export default ConversationList;