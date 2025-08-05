import React, { useState, useEffect, useCallback, useRef } from 'react';
import useApi from '../../hooks/useApi';
import useWebSocket from '../../hooks/useWebSocket';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import './MessageView.css';

const MessageView = ({ conversationId }) => {
	const user = useAuthStore(state => state.user);
	const messagesEndRef = useRef(null);
	const [newMessage, setNewMessage] = useState('');
	const [messages, setMessages] = useState([]);

	const apiCall = useCallback(() => apiClient.get(`/public/chat/conversations/${conversationId}/messages`), [conversationId]);
	const { data: initialMessages, loading, error } = useApi(apiCall);

	const handleNewMessage = useCallback((message) => {
		setMessages(prev => [message, ...prev]);
	}, []);

	const websocketUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}/ws/dm/${conversationId}`;
	const { sendMessage } = useWebSocket(websocketUrl, handleNewMessage);

	useEffect(() => {
		if (initialMessages) {
			setMessages(initialMessages);
		}
	}, [initialMessages]);

	useEffect(() => {
		messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
	}, [messages]);

	const handleSubmit = (e) => {
		e.preventDefault();
		if (newMessage.trim()) {
			sendMessage({ messageText: newMessage });
			setNewMessage('');
		}
	};

	return (
		<div className="message-view-container">
			<div className="message-list">
				{loading && <p>Lade Nachrichten...</p>}
				{error && <p className="error-message">{error}</p>}
				{[...messages].reverse().map(msg => (
					<div key={msg.id} className={`message-bubble-container ${msg.senderId === user.id ? 'sent' : 'received'}`}>
						<div className="message-bubble">
							{msg.messageText}
						</div>
					</div>
				))}
				<div ref={messagesEndRef} />
			</div>
			<div className="message-input-container">
				<form onSubmit={handleSubmit} className="message-input-form">
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
		</div>
	);
};

export default MessageView;