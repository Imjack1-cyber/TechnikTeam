import React, { useState, useRef, useCallback, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import ConversationList from '../components/chat/ConversationList';
import MessageView from '../components/chat/MessageView';
import './ChatPage.css';

const ChatPage = () => {
	const { conversationId } = useParams();
	const [sidebarWidth, setSidebarWidth] = useState(320);
	const isResizing = useRef(false);

	const handleMouseDown = (e) => {
		e.preventDefault();
		isResizing.current = true;
		document.addEventListener('mousemove', handleMouseMove);
		document.addEventListener('mouseup', handleMouseUp);
	};

	const handleMouseMove = useCallback((e) => {
		if (!isResizing.current) return;
		const newWidth = e.clientX;
		// Add constraints for min/max width
		if (newWidth > 200 && newWidth < 600) {
			setSidebarWidth(newWidth);
		}
	}, []);

	const handleMouseUp = useCallback(() => {
		isResizing.current = false;
		document.removeEventListener('mousemove', handleMouseMove);
		document.removeEventListener('mouseup', handleMouseUp);
	}, [handleMouseMove]);

	useEffect(() => {
		return () => {
			// Cleanup listeners when the component unmounts
			document.removeEventListener('mousemove', handleMouseMove);
			document.removeEventListener('mouseup', handleMouseUp);
		};
	}, [handleMouseMove, handleMouseUp]);


	// On mobile, the presence of a conversationId dictates which pane is visible.
	const containerClass = `chat-page-container ${conversationId ? 'message-view-visible' : 'conversation-list-visible'}`;

	return (
		<div className={containerClass}>
			<aside className="chat-sidebar" style={{ width: `${sidebarWidth}px` }}>
				<ConversationList selectedConversationId={conversationId} />
			</aside>
			<div
				className={`chat-resizer ${isResizing.current ? 'resizing' : ''}`}
				onMouseDown={handleMouseDown}
			/>
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