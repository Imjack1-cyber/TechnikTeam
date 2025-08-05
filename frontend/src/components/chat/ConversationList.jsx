import React, { useCallback, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import './ConversationList.css';
import Modal from '../ui/Modal';
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
	const { data: conversations, loading, error } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const navigate = useNavigate();
	const { addToast } = useToast();

	const handleSelectUser = async (userId) => {
		setIsModalOpen(false);
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

	return (
		<div className="conversation-list-container">
			<div className="conversation-list-header">
				<h3>Gespräche</h3>
				<button onClick={() => setIsModalOpen(true)} className="btn btn-small" title="Neues Gespräch">
					<i className="fas fa-plus"></i>
				</button>
			</div>
			<div className="conversation-list">
				{loading && <p>Lade...</p>}
				{error && <p className="error-message">{error}</p>}
				{conversations?.map(conv => (
					<Link to={`/chat/${conv.id}`} key={conv.id} className={`conversation-item ${conv.id.toString() === selectedConversationId ? 'active' : ''}`}>
						<div className="conversation-details">
							<span className="conversation-username">{conv.otherParticipantUsername}</span>
							<span className="conversation-snippet">{conv.lastMessage}</span>
						</div>
					</Link>
				))}
			</div>
			{isModalOpen && <UserSearchModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSelectUser={handleSelectUser} />}
		</div>
	);
};

export default ConversationList;