import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useParams } from 'react-router-dom';
import useApi from '../../hooks/useApi';
import useWebSocket from '../../hooks/useWebSocket';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import ManageParticipantsModal from './ManageParticipantsModal';
import MessageStatus from './MessageStatus';
import './MessageView.css';
import { useToast } from '../../context/ToastContext';

const MessageView = ({ conversationId }) => {
	const user = useAuthStore(state => state.user);
	const messagesEndRef = useRef(null);
	const fileInputRef = useRef(null);
	const [newMessage, setNewMessage] = useState('');
	const [messages, setMessages] = useState([]);
	const [conversation, setConversation] = useState(null);
	const [isManageModalOpen, setIsManageModalOpen] = useState(false);
	const [isUploading, setIsUploading] = useState(false);
	const [editingMessageId, setEditingMessageId] = useState(null);
	const [editingText, setEditingText] = useState('');
	const { addToast } = useToast();


	const messagesApiCall = useCallback(() => apiClient.get(`/public/chat/conversations/${conversationId}/messages`), [conversationId]);
	const { data: initialMessages, loading: messagesLoading, error: messagesError, reload: reloadMessages } = useApi(messagesApiCall);

	const conversationApiCall = useCallback(() => apiClient.get(`/public/chat/conversations/${conversationId}`), [conversationId]);
	const { data: currentConversation, reload: reloadConversation } = useApi(conversationApiCall);

	useEffect(() => {
		if (currentConversation) {
			setConversation(currentConversation);
		}
	}, [currentConversation]);

	const handleWebSocketMessage = useCallback((message) => {
		if (message.type === 'new_message') {
			setMessages(prev => [message.payload, ...prev]);
		} else if (message.type === 'messages_status_updated') {
			const { messageIds, newStatus } = message.payload;
			setMessages(prev => prev.map(msg =>
				messageIds.includes(msg.id) ? { ...msg, status: newStatus } : msg
			));
		} else if (message.type === 'message_updated') {
			setMessages(prev => prev.map(msg =>
				msg.id === message.payload.messageId
					? { ...msg, messageText: message.payload.newText, edited: true }
					: msg
			));
		} else if (message.type === 'message_deleted') {
			setMessages(prev => prev.map(msg =>
				msg.id === message.payload.messageId
					? { ...msg, isDeleted: true, deletedByUsername: message.payload.deletedByUsername }
					: msg
			));
		}
	}, []);

	const websocketUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/dm/${conversationId}`;
	const { sendMessage } = useWebSocket(websocketUrl, handleWebSocketMessage);

	useEffect(() => {
		if (initialMessages) {
			setMessages(initialMessages);
		}
	}, [initialMessages]);

	useEffect(() => {
		// When messages load or a new one arrives, check for unread messages to mark as read
		const unreadMessageIds = messages
			.filter(msg => msg.senderId !== user.id && msg.status !== 'READ')
			.map(msg => msg.id);

		if (unreadMessageIds.length > 0) {
			sendMessage({ type: 'mark_as_read', payload: { messageIds: unreadMessageIds } });
		}
	}, [messages, user.id, sendMessage]);

	useEffect(() => {
		messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
	}, [messages]);

	const handleSubmit = (e) => {
		e.preventDefault();
		if (newMessage.trim()) {
			sendMessage({ type: 'new_message', messageText: newMessage });
			setNewMessage('');
		}
	};

	const handleFileUpload = async (event) => {
		const file = event.target.files[0];
		if (!file) return;

		setIsUploading(true);
		const formData = new FormData();
		formData.append('file', file);

		try {
			const result = await apiClient.post('/public/chat/upload', formData);
			if (result.success) {
				const fileUrl = `/api/v1/public/files/download/${result.data.id}`;
				const messageText = `Datei hochgeladen: [${result.data.filename}](${fileUrl})`;
				sendMessage({ type: 'new_message', messageText });
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message || 'Datei-Upload fehlgeschlagen.', 'error');
		} finally {
			setIsUploading(false);
		}
	};

	const renderMessageContent = (msg) => {
		const text = msg.messageText;
		const isSentByMe = msg.senderId === user.id;
		const imageRegex = /\[(.*?)\]\((.*?)\.(png|jpg|jpeg|gif)\)/i;
		const match = text.match(imageRegex);

		if (match) {
			const altText = match[1];
			const imageUrl = match[2] + '.' + match[3];
			return <img src={imageUrl} alt={altText} style={{ maxWidth: '100%', borderRadius: '12px' }} />;
		}

		const fileRegex = /\[(.*?)\]\((.*?)\)/;
		const fileMatch = text.match(fileRegex);

		if (fileMatch) {
			const fileName = fileMatch[1];
			const fileUrl = fileMatch[2];
			return <a href={fileUrl} target="_blank" rel="noopener noreferrer" style={{ color: isSentByMe ? '#fff' : '#000' }}><i className="fas fa-file-alt"></i> {fileName}</a>;
		}

		return text;
	};

	const handleEditClick = (msg) => {
		setEditingMessageId(msg.id);
		setEditingText(msg.messageText);
	};

	const handleCancelEdit = () => {
		setEditingMessageId(null);
		setEditingText('');
	};

	const handleEditSubmit = () => {
		if (editingText.trim()) {
			sendMessage({
				type: 'update_message',
				payload: { messageId: editingMessageId, newText: editingText }
			});
			handleCancelEdit();
		}
	};

	const handleDeleteClick = (msg) => {
		if (window.confirm('Nachricht wirklich löschen?')) {
			sendMessage({
				type: 'delete_message',
				payload: { messageId: msg.id }
			});
		}
	};


	const handleAddUsers = async (userIds) => {
		setIsManageModalOpen(false);
		try {
			const result = await apiClient.post(`/public/chat/conversations/${conversationId}/participants`, { userIds });
			if (result.success) {
				addToast('Mitglieder erfolgreich hinzugefügt.', 'success');
				reloadConversation();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message, 'error');
		}
	};

	const getHeaderText = () => {
		if (!conversation) return 'Lade...';
		if (conversation.groupChat) return conversation.name;

		// Find the other participant in a 1-on-1 chat
		const otherParticipant = conversation.participants?.find(p => p.id !== user.id);
		return otherParticipant ? otherParticipant.username : 'Unbekannt';
	};

	return (
		<div className="message-view-container">
			<div className="message-view-header">
				<Link to="/chat" className="back-button">
					<i className="fas fa-arrow-left"></i>
				</Link>
				<h3>{getHeaderText()}</h3>
				{conversation?.groupChat && conversation.creatorId === user.id && (
					<button onClick={() => setIsManageModalOpen(true)} className="btn btn-small manage-members-btn" title="Mitglieder verwalten">
						<i className="fas fa-user-plus"></i>
					</button>
				)}
			</div>
			<div className="message-list">
				{messagesLoading && <p>Lade Nachrichten...</p>}
				{messagesError && <p className="error-message">{messagesError}</p>}
				{[...messages].reverse().map(msg => {
					const isSentByMe = msg.senderId === user.id;
					const canEdit = !msg.isDeleted && isSentByMe;
					const canDelete = !msg.isDeleted && (isSentByMe || (conversation?.groupChat && conversation.creatorId === user.id));
					const isEditing = editingMessageId === msg.id;
					return (
						<div key={msg.id} className={`message-bubble-container ${isSentByMe ? 'sent' : 'received'}`}>
							<div
								className="message-bubble"
								style={!isSentByMe ? { backgroundColor: msg.chatColor } : {}}
							>
								{!isSentByMe && <div className="message-sender">{msg.senderUsername}</div>}
								{!msg.isDeleted ? (
									isEditing ? (
										<div>
											<textarea value={editingText} onChange={(e) => setEditingText(e.target.value)} className="chat-edit-input" autoFocus />
											<div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.5rem' }}>
												<button onClick={handleEditSubmit} className="btn btn-small btn-success">Speichern</button>
												<button onClick={handleCancelEdit} className="btn btn-small btn-secondary">Abbrechen</button>
											</div>
										</div>
									) : (
										<>
											{renderMessageContent(msg)}
											<div className="message-meta">
												<span>{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</span>
												{msg.edited && <em style={{ opacity: 0.8 }}>(bearbeitet)</em>}
												<MessageStatus status={msg.status} isSentByMe={isSentByMe} />
											</div>
										</>
									)
								) : (
									<em style={{ opacity: 0.7 }}>Nachricht von {msg.deletedByUsername} gelöscht.</em>
								)}
							</div>
							{!msg.isDeleted && !isEditing && (canEdit || canDelete) && (
								<div className="chat-options">
									{canEdit && <button onClick={() => handleEditClick(msg)} className="chat-option-btn" title="Bearbeiten"><i className="fas fa-pencil-alt"></i></button>}
									{canDelete && <button onClick={() => handleDeleteClick(msg)} className="chat-option-btn" title="Löschen"><i className="fas fa-trash"></i></button>}
								</div>
							)}
						</div>
					)
				})}
				<div ref={messagesEndRef} />
			</div>
			<div className="message-input-container">
				<form onSubmit={handleSubmit} className="message-input-form">
					<input
						type="file"
						ref={fileInputRef}
						onChange={handleFileUpload}
						style={{ display: 'none' }}
						accept="image/*,application/pdf"
					/>
					<button type="button" className="btn" onClick={() => fileInputRef.current.click()} disabled={isUploading} title="Datei anhängen">
						{isUploading ? <i className="fas fa-spinner fa-spin"></i> : <i className="fas fa-paperclip"></i>}
					</button>
					<input
						type="text"
						value={newMessage}
						onChange={(e) => setNewMessage(e.target.value)}
						placeholder="Nachricht schreiben..."
						className="message-input"
					/>
					<button type="submit" className="btn">Senden</button>
				</form>
			</div>
			{isManageModalOpen && conversation && (
				<ManageParticipantsModal
					isOpen={isManageModalOpen}
					onClose={() => setIsManageModalOpen(false)}
					onAddUsers={handleAddUsers}
					conversation={conversation}
				/>
			)}
		</div>
	);
};

export default MessageView;