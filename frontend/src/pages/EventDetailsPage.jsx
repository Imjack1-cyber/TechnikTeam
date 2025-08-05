import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import useWebSocket from '../hooks/useWebSocket';
import { useAuthStore } from '../store/authStore';
import StatusBadge from '../components/ui/StatusBadge';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';
import { useToast } from '../../context/ToastContext';

const EventDetailsPage = () => {
	const { eventId } = useParams();
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const apiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading, error } = useApi(apiCall);
	const { addToast } = useToast();

	const [chatMessages, setChatMessages] = useState([]);
	const [chatInput, setChatInput] = useState('');
	const fileInputRef = useRef(null);
	const [isUploading, setIsUploading] = useState(false);
	const [editingMessageId, setEditingMessageId] = useState(null);
	const [editingText, setEditingText] = useState('');


	const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
	const websocketUrl = event && (event.status === 'LAUFEND' || event.status === 'GEPLANT')
		? `${websocketProtocol}//${window.location.host}/ws/chat/${eventId}`
		: null;

	const handleChatMessage = (message) => {
		if (message.type === 'new_message') {
			setChatMessages(prevMessages => [...prevMessages, message.payload]);
		} else if (message.type === 'message_soft_deleted') {
			setChatMessages(prev => prev.map(msg =>
				msg.id === message.payload.messageId
					? { ...msg, isDeleted: true, deletedByUsername: message.payload.deletedByUsername }
					: msg
			));
		} else if (message.type === 'message_updated') {
			setChatMessages(prev => prev.map(msg =>
				msg.id === message.payload.messageId
					? { ...msg, messageText: message.payload.newText, edited: true }
					: msg
			));
		}
	};

	const { readyState, sendMessage } = useWebSocket(websocketUrl, handleChatMessage);

	useEffect(() => {
		if (event?.chatMessages) {
			setChatMessages(event.chatMessages);
		}
	}, [event]);

	const handleChatSubmit = (e) => {
		e.preventDefault();
		if (chatInput.trim()) {
			sendMessage({ type: "new_message", payload: { messageText: chatInput } });
			setChatInput('');
		}
	};

	const handleFileUpload = async (e) => {
		const file = e.target.files[0];
		if (!file) return;

		setIsUploading(true);
		const formData = new FormData();
		formData.append('file', file);

		try {
			// This needs a dedicated endpoint as event chat files might have different context/permissions
			const result = await apiClient.post(`/public/events/${eventId}/chat/upload`, formData);
			if (result.success) {
				const fileUrl = `/api/v1/public/files/download/${result.data.id}`;
				const messageText = `Datei geteilt: [${result.data.filename}](${fileUrl})`;
				sendMessage({ type: "new_message", payload: { messageText } });
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message || 'Datei-Upload fehlgeschlagen.', 'error');
		} finally {
			setIsUploading(false);
			// Reset file input to allow uploading the same file again
			if (fileInputRef.current) fileInputRef.current.value = "";
		}
	};

	const handleEditClick = (msg) => {
		setEditingMessageId(msg.id);
		setEditingText(msg.messageText);
	};

	const handleEditSubmit = () => {
		if (editingText.trim()) {
			sendMessage({
				type: 'update_message',
				payload: { messageId: editingMessageId, newText: editingText }
			});
			setEditingMessageId(null);
			setEditingText('');
		}
	};

	const handleDeleteClick = (msg) => {
		if (window.confirm('Nachricht wirklich löschen?')) {
			sendMessage({
				type: 'delete_message',
				payload: { messageId: msg.id, originalUserId: msg.userId, originalUsername: msg.username }
			});
		}
	};

	const renderMessageText = (msg) => {
		const text = msg.messageText;
		const isSentByMe = msg.userId === user.id;

		const imageRegex = /\[(.*?)\]\((.*?)\.(png|jpg|jpeg|gif)\)/i;
		const imageMatch = text.match(imageRegex);
		if (imageMatch) {
			return <img src={imageMatch[2] + '.' + imageMatch[3]} alt={imageMatch[1]} style={{ maxWidth: '100%', borderRadius: '12px' }} />;
		}

		const fileRegex = /\[(.*?)\]\((.*?)\)/;
		const fileMatch = text.match(fileRegex);
		if (fileMatch) {
			return <a href={fileMatch[2]} target="_blank" rel="noopener noreferrer" style={{ color: isSentByMe ? '#fff' : '#000' }}><i className="fas fa-file-alt"></i> {fileMatch[1]}</a>;
		}

		return <ReactMarkdown rehypePlugins={[rehypeSanitize]}>{text}</ReactMarkdown>;
	};

	if (loading) return <div>Lade Event-Details...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!event) return <div className="error-message">Event nicht gefunden.</div>;

	const canManageDebriefing = isAdmin || user.id === event.leaderUserId;

	const isTaskBlocked = (task) => {
		if (!task.dependsOn || task.dependsOn.length === 0) return false;
		return task.dependsOn.some(parent => parent.status !== 'ERLEDIGT');
	};

	return (
		<div>
			<div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
				<div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
					<h1>{event.name}</h1>
					<StatusBadge status={event.status} />
				</div>
				{event.status === 'ABGESCHLOSSEN' && canManageDebriefing && (
					<Link to={`/admin/veranstaltungen/${event.id}/debriefing`} className="btn btn-secondary">
						<i className="fas fa-clipboard-check"></i> Debriefing ansehen/bearbeiten
					</Link>
				)}
			</div>
			<p className="details-subtitle">
				<strong>Zeitraum:</strong> {new Date(event.eventDateTime).toLocaleString('de-DE')}
				{event.endDateTime && ` - ${new Date(event.endDateTime).toLocaleString('de-DE')}`}
			</p>

			<div className="responsive-dashboard-grid">
				<div className="card">
					<h2 className="card-title">Beschreibung</h2>
					<div className="markdown-content">
						<ReactMarkdown rehypePlugins={[rehypeSanitize]}>
							{event.description || 'Keine Beschreibung.'}
						</ReactMarkdown>
					</div>
				</div>

				<div className="card">
					<h2 className="card-title">Details</h2>
					<ul className="details-list">
						<li><strong>Ort:</strong> <span>{event.location || 'N/A'}</span></li>
						<li><strong>Leitung:</strong> <span>{event.leaderUsername || 'N/A'}</span></li>
					</ul>
				</div>

				<div className="card">
					<h2 className="card-title">Benötigter Personalbedarf</h2>
					<ul className="details-list">
						{event.skillRequirements?.length > 0 ? (
							event.skillRequirements.map(req => <li key={req.requiredCourseId}><strong>{req.courseName}:</strong> <span>{req.requiredPersons} Person(en)</span></li>)
						) : (
							<li>Keine speziellen Qualifikationen benötigt.</li>
						)}
					</ul>
				</div>

				<div className="card">
					<h2 className="card-title">Zugewiesenes Team</h2>
					<ul className="details-list">
						{event.assignedAttendees?.length > 0 ? (
							event.assignedAttendees.map(attendee => <li key={attendee.id}>{attendee.username}</li>)
						) : (
							<li>Noch kein Team zugewiesen.</li>
						)}
					</ul>
				</div>

				<div className="card" style={{ gridColumn: '1 / -1' }}>
					<h2 className="card-title">Aufgaben</h2>
					{event.eventTasks?.length > 0 ? (
						event.eventTasks.map(task => {
							const blocked = isTaskBlocked(task);
							return (
								<div key={task.id} className="card" style={{ marginBottom: '1rem', opacity: blocked ? 0.6 : 1, pointerEvents: blocked ? 'none' : 'auto' }}>
									<div style={{ display: 'flex', justifyContent: 'space-between' }}>
										<h4 style={{ margin: 0 }}>{task.description}</h4>
										<StatusBadge status={task.status} />
									</div>
									{blocked && (
										<small className="text-danger" style={{ display: 'block', fontWeight: 'bold' }}>
											Blockiert durch: {task.dependsOn.map(t => t.description).join(', ')}
										</small>
									)}
									<div className="markdown-content">
										<ReactMarkdown rehypePlugins={[rehypeSanitize]}>
											{task.details || ''}
										</ReactMarkdown>
									</div>
									<p><strong>Zugewiesen an:</strong> {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</p>
								</div>
							);
						})
					) : (
						<p>Für dieses Event wurden noch keine Aufgaben erstellt.</p>
					)}
				</div>

				{(event.status === 'LAUFEND' || event.status === 'GEPLANT') && (
					<div className="card" style={{ gridColumn: '1 / -1' }}>
						<h2 className="card-title">Event-Chat</h2>
						<div id="chat-box" style={{ height: '300px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem', marginBottom: '1rem', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column' }}>
							{chatMessages.map(msg => {
								const canEdit = !msg.isDeleted && msg.userId === user.id;
								const canDelete = !msg.isDeleted && (msg.userId === user.id || isAdmin || user.id === event.leaderUserId);
								const isEditing = editingMessageId === msg.id;
								return (
									<div key={msg.id} className={`chat-message-container ${msg.userId === user.id ? 'current-user' : ''}`}>
										<div className="chat-bubble" style={{ backgroundColor: msg.userId === user.id ? 'var(--primary-color)' : msg.chatColor || '#e9ecef', color: msg.userId === user.id ? '#fff' : 'var(--text-color)' }}>
											{!msg.isDeleted ? (
												<>
													{msg.userId !== user.id && <strong className="chat-username">{msg.username}</strong>}
													{isEditing ? (
														<div>
															<textarea value={editingText} onChange={(e) => setEditingText(e.target.value)} className="chat-edit-input" />
															<button onClick={handleEditSubmit} className="btn btn-small btn-success">Speichern</button>
															<button onClick={() => setEditingMessageId(null)} className="btn btn-small btn-secondary">Abbrechen</button>
														</div>
													) : (
														<span className="chat-text">{renderMessageText(msg)}</span>
													)}
													<span className="chat-timestamp">{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} {msg.edited && <em className="chat-edited-marker">(bearbeitet)</em>}</span>
												</>
											) : (
												<span className="chat-deleted-info">Nachricht von {msg.deletedByUsername} gelöscht</span>
											)}
										</div>
										<div className="chat-options">
											{canEdit && <button className="chat-option-btn" title="Bearbeiten" onClick={() => handleEditClick(msg)}><i className="fas fa-pencil-alt"></i></button>}
											{canDelete && <button className="chat-option-btn" title="Löschen" onClick={() => handleDeleteClick(msg)}><i className="fas fa-trash"></i></button>}
										</div>
									</div>
								);
							})}
						</div>
						<form id="chat-form" onSubmit={handleChatSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
							<input type="file" ref={fileInputRef} onChange={handleFileUpload} style={{ display: 'none' }} accept="image/*,application/pdf" />
							<button type="button" className="btn" onClick={() => fileInputRef.current.click()} disabled={isUploading || readyState !== WebSocket.OPEN} title="Datei anhängen">
								{isUploading ? <i className="fas fa-spinner fa-spin"></i> : <i className="fas fa-paperclip"></i>}
							</button>
							<input
								type="text"
								id="chat-message-input"
								className="form-group"
								style={{ flexGrow: 1, margin: 0 }}
								placeholder="Nachricht eingeben..."
								value={chatInput}
								onChange={(e) => setChatInput(e.target.value)}
								autoComplete="off"
								disabled={readyState !== WebSocket.OPEN}
							/>
							<button type="submit" className="btn" disabled={readyState !== WebSocket.OPEN}>Senden</button>
						</form>
					</div>
				)}
			</div>
		</div>
	);
};

export default EventDetailsPage;