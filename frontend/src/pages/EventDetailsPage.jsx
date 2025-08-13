import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useParams, Link, useRevalidator } from 'react-router-dom';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import useWebSocket from '../hooks/useWebSocket';
import { useAuthStore } from '../store/authStore';
import StatusBadge from '../components/ui/StatusBadge';
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';
import { useToast } from '../context/ToastContext';
import ChecklistTab from '../components/events/ChecklistTab';
import EventGalleryTab from '../components/events/EventGalleryTab';
import TaskModal from '../components/events/TaskModal';
import AdminEventTeamTab from '../components/admin/events/AdminEventTeamTab';

const TaskList = ({ title, tasks, isCollapsed, onToggle, event, user, canManageTasks, isParticipant, onOpenModal, onAction }) => {
	if (tasks.length === 0) {
		return null; // Don't render the section if there are no tasks
	}

	const isTaskBlocked = (task) => {
		if (!task.dependsOn || task.dependsOn.length === 0) return false;
		return task.dependsOn.some(parent => parent.status !== 'ERLEDIGT');
	};

	return (
		<div style={{ marginBottom: '1rem' }}>
			<h3 onClick={onToggle} style={{ cursor: 'pointer', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: isCollapsed ? 0 : '1rem' }}>
				<span>{title} ({tasks.length})</span>
				<i className={`fas fa-chevron-down`} style={{ transition: 'transform 0.2s', transform: isCollapsed ? 'rotate(-90deg)' : 'rotate(0deg)' }}></i>
			</h3>
			{!isCollapsed && tasks.map(task => {
				const blocked = isTaskBlocked(task);
				const isAssignedToCurrentUser = task.assignedUsers.some(u => u.id === user.id);
				const canUpdateStatus = canManageTasks || isAssignedToCurrentUser;

				return (
					<div key={task.id} className="card" style={{ marginBottom: '1rem', opacity: blocked ? 0.6 : 1 }}>
						<div style={{ display: 'flex', justifyContent: 'space-between' }}>
							<h4 style={{ margin: 0 }}>{task.description}</h4>
							<div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
								{canUpdateStatus ? (
									<select
										value={task.status}
										onChange={(e) => onAction(task.id, 'updateStatus', { status: e.target.value })}
										className="form-group"
										style={{ marginBottom: 0, padding: '0.2rem' }}
										disabled={blocked}
									>
										<option value="OFFEN">Offen</option>
										<option value="IN_ARBEIT">In Arbeit</option>
										<option value="ERLEDIGT">Erledigt</option>
									</select>
								) : (
									<StatusBadge status={task.status} />
								)}
								{canManageTasks && <button className="btn btn-small btn-secondary" onClick={() => onOpenModal(task)}><i className="fas fa-edit"></i></button>}
							</div>
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
						<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
							<p><strong>Zugewiesen an:</strong> {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</p>
							<div>
								{task.assignedUsers.length === 0 && isParticipant && (
									<button onClick={() => onAction(task.id, 'claim')} className="btn btn-small btn-success">Übernehmen</button>
								)}
								{isAssignedToCurrentUser && (
									<button onClick={() => onAction(task.id, 'unclaim')} className="btn btn-small btn-danger-outline">Zurückgeben</button>
								)}
							</div>
						</div>
					</div>
				);
			})}
		</div>
	);
};


const EventDetailsPage = () => {
	const { eventId } = useParams();
	const { user, isAdmin, lastUpdatedEvent } = useAuthStore(state => ({
		user: state.user,
		isAdmin: state.isAdmin,
		lastUpdatedEvent: state.lastUpdatedEvent
	}));
	const apiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading, error, reload: reloadEventDetails } = useApi(apiCall);
	const { data: allUsers } = useApi(useCallback(() => (isAdmin || user?.id === event?.leaderUserId) ? apiClient.get('/users') : null, [isAdmin, user, event]));
	const { data: feedbackSummary } = useApi(useCallback(() => (event?.status === 'ABGESCHLOSSEN' && isAdmin) ? apiClient.get(`/admin/events/${eventId}/feedback-summary`) : null, [event, isAdmin, eventId]));
	const { addToast } = useToast();

	const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
	const [editingTask, setEditingTask] = useState(null);
	const [chatMessages, setChatMessages] = useState([]);
	const [chatInput, setChatInput] = useState('');
	const fileInputRef = useRef(null);
	const [isUploading, setIsUploading] = useState(false);
	const [editingMessageId, setEditingMessageId] = useState(null);
	const [editingText, setEditingText] = useState('');
	const longPressTimer = useRef();
	const [activeOptionsMessageId, setActiveOptionsMessageId] = useState(null);
	const [activeTab, setActiveTab] = useState('details');
	const [collapsedTasks, setCollapsedTasks] = useState({});

	const toggleTaskCategory = (category) => {
		setCollapsedTasks(prev => ({
			...prev,
			[category]: !prev[category]
		}));
	};

	useEffect(() => {
		if (event?.status === 'LAUFEND') {
			setActiveTab('cockpit');
		} else if (event?.status === 'ABGESCHLOSSEN') {
			setActiveTab('debriefing');
		} else if (isAdmin) {
			setActiveTab('team');
		} else {
			setActiveTab('details');
		}
	}, [event?.status, isAdmin]);


	useEffect(() => {
		if (lastUpdatedEvent && lastUpdatedEvent.id === parseInt(eventId, 10)) {
			console.log(`Event ${eventId} updated via notification, reloading details...`);
			reloadEventDetails();
		}
	}, [lastUpdatedEvent, eventId, reloadEventDetails]);


	const websocketUrl = event && (event.status === 'LAUFEND' || event.status === 'GEPLANT')
		? `/ws/chat/${eventId}`
		: null;

	const handleChatMessage = useCallback((message) => {
		if (message.type === 'new_message') {
			setChatMessages(prevMessages => [...prevMessages, message.payload]);
		} else if (message.type === 'message_soft_deleted' || message.type === 'message_updated') {
			setChatMessages(prev => prev.map(msg =>
				msg.id === message.payload.id ? message.payload : msg
			));
		}
	}, []);

	const { readyState, sendMessage } = useWebSocket(websocketUrl, handleChatMessage, [event?.status]);

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

	const openTaskModal = (task = null) => {
		setEditingTask(task);
		setIsTaskModalOpen(true);
	};

	const handleTaskAction = async (taskId, action, data = {}) => {
		try {
			const payload = { action, ...data };
			const result = await apiClient.post(`/events/${eventId}/tasks/${taskId}/action`, payload);
			if (result.success) {
				addToast('Aktion erfolgreich!', 'success');
				// The SSE notification will handle the reload via revalidator.
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			addToast(err.message, 'error');
		}
	};

	const handleFileUpload = async (e) => {
		const file = e.target.files[0];
		if (!file) return;

		setIsUploading(true);
		const formData = new FormData();
		formData.append('file', file);

		try {
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
			if (fileInputRef.current) fileInputRef.current.value = "";
		}
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
				payload: { messageId: msg.id, originalUserId: msg.userId, originalUsername: msg.username }
			});
		}
	};

	const handleTouchStart = (messageId) => {
		longPressTimer.current = setTimeout(() => {
			setActiveOptionsMessageId(messageId);
		}, 500);
	};

	const handleTouchEnd = () => {
		clearTimeout(longPressTimer.current);
	};

	const handleTouchMove = () => {
		clearTimeout(longPressTimer.current);
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

	const groupedTasks = useMemo(() => {
		if (!event?.eventTasks) {
			return { open: [], inProgress: [], done: [] };
		}
		return event.eventTasks.reduce((acc, task) => {
			if (task.status === 'IN_ARBEIT') acc.inProgress.push(task);
			else if (task.status === 'ERLEDIGT') acc.done.push(task);
			else acc.open.push(task);
			return acc;
		}, { open: [], inProgress: [], done: [] });
	}, [event?.eventTasks]);

	if (loading) return <div>Lade Event-Details...</div>;
	if (error) return <div className="error-message">{error}</div>;
	if (!event) return <div className="error-message">Event nicht gefunden.</div>;

	const canManageTasks = (isAdmin || user.permissions.includes('EVENT_MANAGE_TASKS') || user.id === event.leaderUserId) && (event.status === 'GEPLANT' || event.status === 'LAUFEND');
	const canManageDebriefing = (isAdmin || user.permissions.includes('EVENT_DEBRIEFING_MANAGE') || user.id === event.leaderUserId) && event.status === 'ABGESCHLOSSEN';
	const canManageTeam = isAdmin || user.permissions.includes('EVENT_MANAGE_ASSIGNMENTS') || user.id === event.leaderUserId;
	const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';

	const groupedAttendees = event.assignedAttendees?.reduce((acc, member) => {
		const role = member.assignedEventRole || 'Unzugewiesen';
		if (!acc[role]) {
			acc[role] = [];
		}
		acc[role].push(member);
		return acc;
	}, {});

	const averageRating = feedbackSummary?.data?.length > 0
		? (feedbackSummary.data.reduce((sum, fb) => sum + fb.rating, 0) / feedbackSummary.data.length).toFixed(1)
		: 'N/A';

	return (
		<div>
			<div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
				<div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
					<h1>{event.name}</h1>
					<StatusBadge status={event.status} />
				</div>
				{event.status === 'ABGESCHLOSSEN' && isParticipant && (
					<Link to={`/feedback/event/${event.id}`} className="btn btn-success">
						<i className="fas fa-star"></i> Feedback geben
					</Link>
				)}
			</div>
			<p className="details-subtitle">
				<strong>Zeitraum:</strong> {new Date(event.eventDateTime).toLocaleString('de-DE')}
				{event.endDateTime && ` - ${new Date(event.endDateTime).toLocaleString('de-DE')}`}
			</p>

			<div className="card" style={{ gridColumn: '1 / -1' }}>
				<div className="modal-tabs">
					{event.status === 'LAUFEND' && <button className={`modal-tab-button ${activeTab === 'cockpit' ? 'active' : ''}`} onClick={() => setActiveTab('cockpit')}>Live Cockpit</button>}
					<button className={`modal-tab-button ${activeTab === 'details' ? 'active' : ''}`} onClick={() => setActiveTab('details')}>Details</button>
					<button className={`modal-tab-button ${activeTab === 'team' ? 'active' : ''}`} onClick={() => setActiveTab('team')}>Team</button>
					<button className={`modal-tab-button ${activeTab === 'tasks' ? 'active' : ''}`} onClick={() => setActiveTab('tasks')}>Aufgaben</button>
					<button className={`modal-tab-button ${activeTab === 'checklist' ? 'active' : ''}`} onClick={() => setActiveTab('checklist')}>
						Inventar-Checkliste
						{canManageTasks && <Link to="/admin/veranstaltungen/checklist-templates" className="btn btn-small btn-secondary" style={{ marginLeft: '1rem' }} onClick={e => e.stopPropagation()} title="Vorlagen verwalten"><i className="fas fa-edit"></i></Link>}
					</button>
					<button className={`modal-tab-button ${activeTab === 'chat' ? 'active' : ''}`} onClick={() => setActiveTab('chat')}>Event-Chat</button>
					{event.status === 'ABGESCHLOSSEN' && <button className={`modal-tab-button ${activeTab === 'gallery' ? 'active' : ''}`} onClick={() => setActiveTab('gallery')}>Galerie</button>}
					{event.status === 'ABGESCHLOSSEN' && <button className={`modal-tab-button ${activeTab === 'debriefing' ? 'active' : ''}`} onClick={() => setActiveTab('debriefing')}>Debriefing & Feedback</button>}
				</div>

				<div className={`modal-tab-content ${activeTab === 'details' ? 'active' : ''}`}>
					<div className="responsive-dashboard-grid" style={{ gridTemplateColumns: '2fr 1fr' }}>
						<div>
							<h3>Beschreibung</h3>
							<div className="markdown-content">
								<ReactMarkdown rehypePlugins={[rehypeSanitize]}>{event.description || 'Keine Beschreibung.'}</ReactMarkdown>
							</div>
						</div>
						<div>
							<h3>Details</h3>
							<ul className="details-list">
								<li><strong>Ort:</strong> <span>{event.location || 'N/A'}</span></li>
								<li><strong>Leitung:</strong> <span>{event.leaderUsername ? <Link to={`/team/${event.leaderUserId}`}>{event.leaderUsername}</Link> : 'N/A'}</span></li>
							</ul>
							<h3 style={{ marginTop: '1rem' }}>Personalbedarf</h3>
							<ul className="details-list">
								{event.skillRequirements?.length > 0 ? (
									event.skillRequirements.map(req => <li key={req.requiredCourseId}><strong>{req.courseName}:</strong> <span>{req.requiredPersons} Person(en)</span></li>)
								) : (
									<li>Keine speziellen Qualifikationen benötigt.</li>
								)}
							</ul>
						</div>
					</div>
				</div>

				<div className={`modal-tab-content ${activeTab === 'team' ? 'active' : ''}`}>
					{canManageTeam ? (
						<AdminEventTeamTab event={event} onTeamUpdate={reloadEventDetails} />
					) : (
						<div className="card">
							<h2 className="card-title">Zugewiesenes Team</h2>
							{groupedAttendees && Object.keys(groupedAttendees).length > 0 ? (
								Object.entries(groupedAttendees).map(([role, members]) => (
									<div key={role} style={{ marginBottom: '1rem' }}>
										<h4 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '0.25rem' }}>{role}</h4>
										<ul className="details-list">
											{members.map(member => <li key={member.id} style={{ border: 'none', padding: '0.25rem 0' }}><Link to={`/team/${member.id}`}>{member.username}</Link></li>)}
										</ul>
									</div>
								))
							) : (
								<p>Noch kein Team zugewiesen.</p>
							)}
						</div>
					)}
				</div>

				<div className={`modal-tab-content ${activeTab === 'cockpit' ? 'active' : ''}`}>
					<div className="responsive-dashboard-grid">
						<div className="card">
							<h3 className="card-title">Meine Aufgaben</h3>
							<TaskList title="In Arbeit" tasks={groupedTasks.inProgress.filter(t => t.assignedUsers.some(u => u.id === user.id))} isCollapsed={false} onToggle={() => { }} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={openTaskModal} onAction={handleTaskAction} />
							<TaskList title="Offen" tasks={groupedTasks.open.filter(t => t.assignedUsers.some(u => u.id === user.id))} isCollapsed={false} onToggle={() => { }} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={openTaskModal} onAction={handleTaskAction} />
						</div>
						<div className="card">
							<h3 className="card-title">Event-Chat</h3>
							{/* Simplified chat view for cockpit */}
							<div id="chat-box-cockpit" style={{ height: '300px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem', marginBottom: '1rem', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column' }}>
								{chatMessages.map(msg => (
									<div key={msg.id} className={`chat-message-container ${msg.userId === user.id ? 'current-user' : ''}`}>
										<div className="chat-bubble" style={{ backgroundColor: msg.userId === user.id ? 'var(--primary-color)' : msg.chatColor || '#e9ecef', color: msg.userId === user.id ? '#fff' : 'var(--text-color)' }}>
											{!msg.isDeleted && <>
												{msg.userId !== user.id && <strong className="chat-username">{msg.username}</strong>}
												<span className="chat-text">{renderMessageText(msg)}</span>
												<span className="chat-timestamp">{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</span>
											</>}
										</div>
									</div>
								))}
							</div>
							<form onSubmit={handleChatSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
								<input type="text" className="form-group" style={{ flexGrow: 1, margin: 0 }} placeholder="Nachricht..." value={chatInput} onChange={(e) => setChatInput(e.target.value)} disabled={readyState !== WebSocket.OPEN} />
								<button type="submit" className="btn" disabled={readyState !== WebSocket.OPEN}>Senden</button>
							</form>
						</div>
					</div>
				</div>

				<div className={`modal-tab-content ${activeTab === 'tasks' ? 'active' : ''}`}>
					{canManageTasks && (
						<div className="table-controls">
							<button className="btn btn-success" onClick={() => openTaskModal()}>
								<i className="fas fa-plus"></i> Neue Aufgabe
							</button>
						</div>
					)}

					<TaskList title="Offen" tasks={groupedTasks.open} isCollapsed={collapsedTasks.open} onToggle={() => toggleTaskCategory('open')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={openTaskModal} onAction={handleTaskAction} />
					<TaskList title="In Arbeit" tasks={groupedTasks.inProgress} isCollapsed={collapsedTasks.inProgress} onToggle={() => toggleTaskCategory('inProgress')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={openTaskModal} onAction={handleTaskAction} />
					<TaskList title="Erledigt" tasks={groupedTasks.done} isCollapsed={collapsedTasks.done} onToggle={() => toggleTaskCategory('done')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={openTaskModal} onAction={handleTaskAction} />

					{event.eventTasks?.length === 0 && (
						<p>Für dieses Event wurden noch keine Aufgaben erstellt.</p>
					)}
				</div>

				<div className={`modal-tab-content ${activeTab === 'checklist' ? 'active' : ''}`}>
					<ChecklistTab event={event} user={user} />
				</div>

				<div className={`modal-tab-content ${activeTab === 'gallery' ? 'active' : ''}`}>
					<EventGalleryTab event={event} user={user} />
				</div>

				<div className={`modal-tab-content ${activeTab === 'debriefing' ? 'active' : ''}`}>
					{canManageDebriefing && <Link to={`/admin/veranstaltungen/${event.id}/debriefing`} className="btn"><i className="fas fa-edit"></i> Debriefing ansehen/bearbeiten</Link>}
					<div className="card" style={{ marginTop: '1.5rem' }}>
						<h3 className="card-title">User-Feedback Zusammenfassung</h3>
						<ul className="details-list">
							<li><strong>Durchschnittliche Bewertung:</strong> <span style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>{averageRating} / 5 ★</span></li>
						</ul>
						<div style={{ maxHeight: '400px', overflowY: 'auto', marginTop: '1rem' }}>
							{feedbackSummary?.data?.map(fb => (
								<div key={fb.id} className="card" style={{ background: 'var(--bg-color)' }}>
									<strong><Link to={`/team/${fb.userId}`}>{fb.username}</Link> ({fb.rating} ★):</strong>
									<p style={{ fontStyle: 'italic', margin: '0.5rem 0 0 0' }}>"{fb.comments}"</p>
								</div>
							))}
						</div>
					</div>
				</div>

				<div className={`modal-tab-content ${activeTab === 'chat' ? 'active' : ''}`}>
					{(event.status === 'LAUFEND' || event.status === 'GEPLANT') ? (
						<>
							<div id="chat-box" style={{ height: '300px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem', marginBottom: '1rem', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column' }}>
								{chatMessages.map(msg => {
									const isSentByMe = msg.userId === user.id;
									const isMessageEditable = () => {
										if (!msg.sentAt) return false;
										const sentAt = new Date(msg.sentAt);
										const now = new Date();
										return (now - sentAt) < 24 * 60 * 60 * 1000;
									};
									const canEdit = !msg.isDeleted && isSentByMe && isMessageEditable();
									const canDelete = !msg.isDeleted && (isSentByMe || isAdmin || user.id === event.leaderUserId);
									const isEditing = editingMessageId === msg.id;
									return (
										<div
											key={msg.id}
											className={`chat-message-container ${isSentByMe ? 'current-user' : ''} ${activeOptionsMessageId === msg.id ? 'options-visible' : ''}`}
											onTouchStart={() => handleTouchStart(msg.id)}
											onTouchEnd={handleTouchEnd}
											onTouchMove={handleTouchMove}
											onClick={(e) => { if (activeOptionsMessageId) e.stopPropagation() }}
										>
											<div className="chat-bubble" style={{ backgroundColor: isSentByMe ? 'var(--primary-color)' : msg.chatColor || '#e9ecef', color: isSentByMe ? '#fff' : 'var(--text-color)' }}>
												{!msg.isDeleted ? (
													<>
														{!isSentByMe && <strong className="chat-username">{msg.username}</strong>}
														{isEditing ? (
															<div>
																<textarea value={editingText} onChange={(e) => setEditingText(e.target.value)} className="chat-edit-input" />
																<div style={{ marginTop: '0.5rem', display: 'flex', gap: '0.5rem' }}>
																	<button onClick={handleEditSubmit} className="btn btn-small btn-success">Speichern</button>
																	<button onClick={handleCancelEdit} className="btn btn-small btn-secondary">Abbrechen</button>
																</div>
															</div>
														) : (
															<span className="chat-text">{renderMessageText(msg)}</span>
														)}
														<span className="chat-timestamp">{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} {msg.edited && <em style={{ opacity: 0.8 }} title={`Bearbeitet am ${new Date(msg.editedAt).toLocaleString('de-DE')}`}>(bearbeitet)</em>}</span>
													</>
												) : (
													<span className="chat-deleted-info" style={{ opacity: 0.7 }}>
														Diese Nachricht wurde von {msg.deletedByUsername} gelöscht.<br />
														<small>{new Date(msg.deletedAt).toLocaleString('de-DE')}</small>
													</span>
												)}
											</div>
											{!msg.isDeleted && !isEditing && (canEdit || canDelete) && (
												<div className="chat-options">
													{canEdit && <button className="chat-option-btn" title="Bearbeiten" onClick={() => handleEditClick(msg)}><i className="fas fa-pencil-alt"></i></button>}
													{canDelete && <button className="chat-option-btn" title="Löschen" onClick={() => handleDeleteClick(msg)}><i className="fas fa-trash"></i></button>}
												</div>
											)}
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
						</>
					) : (
						<p>Der Chat ist nur für geplante oder laufende Events verfügbar.</p>
					)}
				</div>
			</div>
			{isTaskModalOpen && (
				<TaskModal
					isOpen={isTaskModalOpen}
					onClose={() => setIsTaskModalOpen(false)}
					onSuccess={() => { setIsTaskModalOpen(false); reloadEventDetails(); }}
					event={event}
					task={editingTask}
					allUsers={event.assignedAttendees}
				/>
			)}
		</div>
	);
};

export default EventDetailsPage;