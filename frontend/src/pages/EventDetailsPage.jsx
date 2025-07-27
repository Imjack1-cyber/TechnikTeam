import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import useWebSocket from '../hooks/useWebSocket';
import { useAuthStore } from '../store/authStore';
import StatusBadge from '../components/ui/StatusBadge';

const EventDetailsPage = () => {
	const { eventId } = useParams();
	const { user } = useAuthStore();
	const { data: event, loading, error, reload } = useApi(() => apiClient.get(`/public/events/${eventId}`));

	const [chatMessages, setChatMessages] = useState([]);
	const [chatInput, setChatInput] = useState('');

	const websocketProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
	const websocketUrl = event && event.status === 'LAUFEND'
		? `${websocketProtocol}//${window.location.host}/TechnikTeam/ws/chat/${eventId}`
		: null;

	const handleChatMessage = (message) => {
		setChatMessages(prevMessages => [...prevMessages, message.payload]);
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

	if (loading) return <div>Lade Event-Details...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!event) return <div className="error-message">Event nicht gefunden.</div>;

	const renderMarkdown = (content) => {
		// In a real app, use a library like `marked` or `react-markdown`
		return { __html: content.replace(/\n/g, '<br />') };
	};

	return (
		<div>
			<div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
				<h1>{event.name}</h1>
				<StatusBadge status={event.status} />
			</div>
			<p className="details-subtitle">
				<strong>Zeitraum:</strong> {new Date(event.eventDateTime).toLocaleString('de-DE')}
				{event.endDateTime && ` - ${new Date(event.endDateTime).toLocaleString('de-DE')}`}
			</p>

			<div className="responsive-dashboard-grid">
				<div className="card">
					<h2 className="card-title">Beschreibung</h2>
					<div className="markdown-content" dangerouslySetInnerHTML={renderMarkdown(event.description || 'Keine Beschreibung.')} />
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
						event.eventTasks.map(task => (
							<div key={task.id} className="card" style={{ marginBottom: '1rem' }}>
								<div style={{ display: 'flex', justifyContent: 'space-between' }}>
									<h4 style={{ margin: 0 }}>{task.description}</h4>
									<StatusBadge status={task.status} />
								</div>
								<div className="markdown-content" dangerouslySetInnerHTML={renderMarkdown(task.details || '')} />
								<p><strong>Zugewiesen an:</strong> {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</p>
							</div>
						))
					) : (
						<p>Für dieses Event wurden noch keine Aufgaben erstellt.</p>
					)}
				</div>

				{event.status === 'LAUFEND' && (
					<div className="card" style={{ gridColumn: '1 / -1' }}>
						<h2 className="card-title">Event-Chat</h2>
						<div id="chat-box" style={{ height: '300px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem', marginBottom: '1rem', background: 'var(--bg-color)' }}>
							{chatMessages.map(msg => (
								<div key={msg.id} className={`chat-message-container ${msg.userId === user.id ? 'current-user' : ''}`}>
									<div className="chat-bubble" style={{ backgroundColor: msg.userId === user.id ? 'var(--primary-color)' : msg.chatColor }}>
										{!msg.isDeleted ? (
											<>
												{msg.userId !== user.id && <strong className="chat-username">{msg.username}</strong>}
												<span className="chat-text" dangerouslySetInnerHTML={renderMarkdown(msg.messageText)} />
												<span className="chat-timestamp">{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</span>
											</>
										) : (
											<span className="chat-deleted-info">Nachricht gelöscht</span>
										)}
									</div>
								</div>
							))}
						</div>
						<form id="chat-form" onSubmit={handleChatSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
							<input
								type="text"
								id="chat-message-input"
								className="form-group"
								style={{ flexGrow: 1, margin: 0 }}
								placeholder="Nachricht eingeben..."
								value={chatInput}
								onChange={(e) => setChatInput(e.target.value)}
								autoComplete="off"
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