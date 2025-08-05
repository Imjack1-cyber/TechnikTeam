import React from 'react';
import { useParams } from 'react-router-dom';
import ConversationList from '../components/chat/ConversationList';
import MessageView from '../components/chat/MessageView';
import './ChatPage.css';

const ChatPage = () => {
	const { conversationId } = useParams();

	return (
		<div className="chat-page-container">
			<aside className="chat-sidebar">
				<ConversationList selectedConversationId={conversationId} />
			</aside>
			<main className="chat-main">
				{conversationId ? (
					<MessageView conversationId={conversationId} key={conversationId} />
				) : (
					<div className="chat-welcome-view">
						<i className="fas fa-comments"></i>
						<h2>Willkommen im Chat</h2>
						<p>Wähle links ein Gespräch aus oder starte ein neues, um zu beginnen.</p>
					</div>
				)}
			</main>
		</div>
	);
};

export default ChatPage;