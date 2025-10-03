import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Alert, Platform, FlatList, TextInput } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import { useAuthStore } from '../store/authStore';
import StatusBadge from '../components/ui/StatusBadge';
import MarkdownDisplay from 'react-native-markdown-display';
import { useToast } from '../context/ToastContext';
import ChecklistTab from '../components/events/ChecklistTab';
import EventGalleryTab from '../components/events/EventGalleryTab';
import TaskModal from '../components/events/TaskModal';
import AdminEventTeamTab from '../components/admin/events/AdminEventTeamTab';
import AdminEventTasksTab from '../components/admin/events/AdminEventTasksTab';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders, shadows } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import ScrollableContent from '../components/ui/ScrollableContent';
import AdminModal from '../components/ui/AdminModal';
import useWebSocket from '../hooks/useWebSocket';
import AccordionSection from '../components/ui/AccordionSection';
import { format, isToday, isYesterday, formatDistanceToNowStrict } from 'date-fns';
import { de } from 'date-fns/locale';

const UserTaskCard = ({ task, user, canManageTasks, isParticipant, onOpenModal, onAction, styles, colors }) => {
    const getTaskCardStyle = () => {
        const needsHelp = task.status === 'IN_PROGRESS' && task.assignedUsers.length < task.requiredPersons;
        if (task.status === 'LOCKED') return styles.lockedTask;
        if (task.status === 'DONE') return styles.doneTask;
        if (task.isImportant || needsHelp) return styles.importantTask;
        if (task.status === 'IN_PROGRESS') return styles.inProgressTask;
        return {}; // Default for OPEN
    };
    const isDone = task.status === 'DONE';
    const isAssigned = task.assignedUsers.some(u => u.id === user.id);
    const isActionable = task.status === 'OPEN' || task.status === 'IN_PROGRESS';
    
    return (
        <View style={[styles.taskCard, getTaskCardStyle()]}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Text style={[styles.taskName, isDone && styles.doneTaskText]}>{task.name}</Text>
                <Text style={styles.displayOrder}>#{task.displayOrder}</Text>
            </View>
             <View style={styles.metaContainer}>
                <View style={styles.metaItem}>
                    <Icon name="users" size={12} color={colors.textMuted} />
                    <Text style={styles.metaText}>{task.assignedUsers.length} / {task.requiredPersons}</Text>
                </View>
            </View>
            {task.assignedUsers.length > 0 && (
                <View style={styles.assigneeList}>
                    <Text style={styles.assigneeText}>Aktiv: {task.assignedUsers.map(u => u.username).join(', ')}</Text>
                </View>
            )}
             <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 8}}>
                {canManageTasks && <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onOpenModal(task)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>}
                {!isAssigned && isParticipant && isActionable && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Mitmachen</Text></TouchableOpacity>}
                {isAssigned && <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => onAction(task.id, 'unclaim')}><Text style={styles.buttonText}>Verlassen</Text></TouchableOpacity>}
                {isAssigned && <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => onAction(task.id, 'updateStatus', 'DONE')}><Text style={styles.buttonText}>Abschließen</Text></TouchableOpacity>}
            </View>
        </View>
    );
};

const UserKanbanColumn = ({ item, ...props }) => (
    <View style={props.styles.kanbanColumn}>
        <Text style={props.styles.columnTitle}>{item.name}</Text>
        <FlatList
            data={item.tasks}
            renderItem={({ item: task }) => <UserTaskCard task={task} {...props} />}
            keyExtractor={task => task.id.toString()}
        />
    </View>
);

const UserTaskView = ({ event, user, categories, canManageTasks, isParticipant, onOpenModal, onAction, showDoneTasks, onShowDoneTasksToggle, styles, colors }) => {
    const categorizedTasks = useMemo(() => {
        if (!event.eventTasks) return [];
        const filteredTasks = event.eventTasks.filter(task => showDoneTasks || task.status !== 'DONE');
        
        const tasksByCat = filteredTasks.reduce((acc, task) => {
            const categoryId = task.categoryId || 0;
            if (!acc[categoryId]) acc[categoryId] = [];
            acc[categoryId].push(task);
            return acc;
        }, {});

        const allCategories = categories ? [...categories, {id: 0, name: 'Unkategorisiert'}] : [{id: 0, name: 'Unkategorisiert'}];
        
        return allCategories.map(cat => ({
            ...cat,
            tasks: (tasksByCat[cat.id] || []).sort((a,b) => a.displayOrder - b.displayOrder)
        })).filter(cat => cat.tasks.length > 0);

    }, [event.eventTasks, showDoneTasks, categories]);

    return (
        <View style={{ flex: 1 }}>
             <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: spacing.md, paddingTop: spacing.md }}>
                <BouncyCheckbox isChecked={showDoneTasks} onPress={onShowDoneTasksToggle} />
                <Text>Erledigte Aufgaben anzeigen</Text>
            </View>
            <FlatList
                horizontal
                data={categorizedTasks}
                renderItem={({ item }) => (
                    <UserKanbanColumn
                        item={item}
                        event={event}
                        user={user}
                        canManageTasks={canManageTasks}
                        isParticipant={isParticipant}
                        onOpenModal={onOpenModal}
                        onAction={onAction}
                        styles={styles}
                        colors={colors}
                    />
                )}
                keyExtractor={item => item.id.toString()}
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={styles.kanbanBoard}
            />
        </View>
    );
};

const DetailsTab = ({ event }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    return (
        <ScrollableContent contentContainerStyle={styles.contentContainer}>
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Beschreibung</Text>
                <MarkdownDisplay>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                <Text style={[styles.cardTitle, {marginTop: 16}]}>Details</Text>
                <Text>Ort: {event.location || 'N/A'}</Text>
                <Text>Leitung: {event.leaderUsername || 'N/A'}</Text>
            </View>
        </ScrollableContent>
    );
};

const TeamTab = ({ event, onUpdate, isAdmin }) => {
    if (isAdmin) {
        return <AdminEventTeamTab event={event} onTeamUpdate={onUpdate} />;
    }
    return <View style={getCommonStyles().centered}><Text>Teamansicht in Kürze verfügbar.</Text></View>;
};

const TasksTab = ({ event, user, categories, canManageTasks, isParticipant, handleOpenTaskModal, handleTaskAction }) => {
    const [showDoneTasks, setShowDoneTasks] = useState(false);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    return <UserTaskView event={event} user={user} categories={categories} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} showDoneTasks={showDoneTasks} onShowDoneTasksToggle={() => setShowDoneTasks(!showDoneTasks)} styles={styles} colors={colors} />;
};

const EventChatTab = ({ eventId }) => {
    const user = useAuthStore(state => state.user);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [isSending, setIsSending] = useState(false);

    const messagesApiCall = useCallback(() => apiClient.get(`/public/events/${eventId}/chat/messages`), [eventId]);
    const { data: initialMessages, loading: messagesLoading, error: messagesError } = useApi(messagesApiCall);

    useEffect(() => {
        if (initialMessages) {
            setMessages(initialMessages.slice().reverse());
        }
    }, [initialMessages]);

    const handleWebSocketMessage = useCallback((message) => {
        if (message.type === 'new_message') {
            setMessages(prev => [message.payload, ...prev]);
        } else if (message.type === 'message_updated' || message.type === 'message_soft_deleted') {
            setMessages(prev => prev.map(msg => msg.id === message.payload.id ? message.payload : msg));
        }
    }, []);

    const { sendMessage } = useWebSocket(`/ws/chat/${eventId}`, handleWebSocketMessage, [eventId]);

    const handleSubmit = () => {
        if (newMessage.trim() && !isSending) {
            setIsSending(true);
            sendMessage({ type: 'new_message', payload: { messageText: newMessage } });
            setNewMessage('');
            setIsSending(false);
        }
    };

    const formatTimestamp = (date) => {
        const d = new Date(date);
        if (isToday(d)) return `Heute, ${format(d, 'HH:mm')}`;
        if (isYesterday(d)) return `Gestern, ${format(d, 'HH:mm')}`;
        return format(d, 'dd.MM.yyyy, HH:mm');
    };

    return (
        <View style={{ flex: 1, backgroundColor: colors.surface }}>
            <FlatList
                data={messages}
                inverted
                keyExtractor={item => item.id.toString()}
                initialNumToRender={15}
                removeClippedSubviews={true}
                renderItem={({ item: msg }) => {
                    const isSentByMe = msg.userId === user.id;
                    return (
                        <View style={[styles.bubbleContainer, isSentByMe ? styles.sent : styles.received]}>
                            <View style={[styles.bubble, isSentByMe ? styles.sentBubble : { backgroundColor: msg.chatColor || colors.background }]}>
                                {!isSentByMe && <Text style={[styles.sender, { color: colors.primary }]}>{msg.username}</Text>}
                                {msg.isDeleted ? (
                                    <Text style={[styles.deletedText, isSentByMe && styles.sentText]}>Nachricht gelöscht</Text>
                                ) : (
                                    <MarkdownDisplay style={{ body: isSentByMe ? styles.sentText : styles.receivedText }}>{msg.messageText}</MarkdownDisplay>
                                )}
                                <View style={styles.metaContainer}>
                                    <Text style={[styles.timestamp, isSentByMe && { color: 'rgba(255,255,255,0.7)' }]}>{formatTimestamp(msg.sentAt)}</Text>
                                </View>
                            </View>
                        </View>
                    );
                }}
                contentContainerStyle={{ padding: spacing.md }}
            />
            <View style={styles.inputContainer}>
                <TextInput
                    style={styles.chatInput}
                    value={newMessage}
                    onChangeText={setNewMessage}
                    placeholder="Nachricht schreiben..."
                    multiline
                    maxLength={1024}
                />
                <TouchableOpacity style={[styles.button, styles.primaryButton, isSending && styles.disabledButton]} onPress={handleSubmit} disabled={isSending}>
                    <Text style={styles.buttonText}>Senden</Text>
                </TouchableOpacity>
            </View>
        </View>
    );
};

const EventDetailsPage = () => {
	const route = useRoute();
    const navigation = useNavigation();
	const { eventId } = route.params;
	const { user, isAdmin, lastUpdatedEvent } = useAuthStore();
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const eventApiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading: eventLoading, error: eventError, reload: reloadEventDetails } = useApi(eventApiCall);
    const categoriesApiCall = useCallback(() => eventId ? apiClient.get(`/admin/events/${eventId}/task-categories`) : null, [eventId]);
    const { data: categories, loading: categoriesLoading } = useApi(categoriesApiCall);
	
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const [isStarting, setIsStarting] = useState(false);
    const [isStopping, setIsStopping] = useState(false);
    const [isStartConfirmModalOpen, setIsStartConfirmModalOpen] = useState(false);
    const [isStopConfirmModalOpen, setIsStopConfirmModalOpen] = useState(false);
    const [showDoneTasks, setShowDoneTasks] = useState(false);

	useEffect(() => {
		if (lastUpdatedEvent && lastUpdatedEvent.id === parseInt(eventId, 10)) {
			reloadEventDetails();
		}
	}, [lastUpdatedEvent, eventId, reloadEventDetails]);


    const handleOpenTaskModal = (task = null) => {
        setEditingTask(task);
        setIsTaskModalOpen(true);
    };

    const handleTaskAction = async (taskId, action, status = null) => {
        try {
            const result = await apiClient.post(`/events/${eventId}/tasks/${taskId}/action`, { action, status });
            if (result.success) {
                addToast('Aktion erfolgreich.', 'success');
                reloadEventDetails();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
    };

    const handleStartEvent = () => {
        setIsStartConfirmModalOpen(true);
    };

    const performStartEvent = async () => {
        setIsStarting(true);
        try {
            const result = await apiClient.post(`/events/${eventId}/start`);
            if (result.success) {
                addToast('Event erfolgreich gestartet.', 'success');
                reloadEventDetails();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler beim Starten des Events: ${err.message}`, 'error');
        } finally {
            setIsStarting(false);
            setIsStartConfirmModalOpen(false);
        }
    };

    const handleStopEvent = () => {
        setIsStopConfirmModalOpen(true);
    };

    const performStopEvent = async () => {
        setIsStopping(true);
        try {
            const result = await apiClient.post(`/events/${eventId}/stop`);
            if (result.success) {
                addToast('Event erfolgreich beendet.', 'success');
                reloadEventDetails();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler beim Beenden des Events: ${err.message}`, 'error');
        } finally {
            setIsStopping(false);
            setIsStopConfirmModalOpen(false);
        }
    };


	if (eventLoading || categoriesLoading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (eventError) return <View style={styles.centered}><Text style={styles.errorText}>{eventError}</Text></View>;
	if (!event) return <View style={styles.centered}><Text>Event nicht gefunden.</Text></View>;
    
    const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';
    const canManageTasks = isAdmin || user.id === event.leaderUserId;
    
	return (
        <ScrollableContent style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>{event.name}</Text>
                <View style={{flexDirection: 'row', alignItems: 'center', gap: spacing.sm}}>
                    <StatusBadge status={event.status} />
                    {canManageTasks && event.status === 'GEPLANT' && (
                        <TouchableOpacity 
                            style={[styles.button, styles.successButton, {paddingVertical: 6, paddingHorizontal: 12}]}
                            onPress={handleStartEvent}
                            disabled={isStarting}
                        >
                            {isStarting 
                                ? <ActivityIndicator color={colors.white} size="small" /> 
                                : (
                                    <>
                                        <Icon name="play" size={14} color={colors.white} />
                                        <Text style={styles.buttonText}> Starten</Text>
                                    </>
                                )
                            }
                        </TouchableOpacity>
                    )}
                    {canManageTasks && event.status === 'LAUFEND' && (
                        <TouchableOpacity 
                            style={[styles.button, styles.dangerButton, {paddingVertical: 6, paddingHorizontal: 12}]}
                            onPress={handleStopEvent}
                            disabled={isStopping}
                        >
                            {isStopping 
                                ? <ActivityIndicator color={colors.white} size="small" /> 
                                : (
                                    <>
                                        <Icon name="stop" size={14} color={colors.white} />
                                        <Text style={styles.buttonText}> Beenden</Text>
                                    </>
                                )
                            }
                        </TouchableOpacity>
                    )}
                </View>
            </View>
            <Text style={styles.subtitle}>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text>
            
            <View style={styles.contentContainer}>
                <AccordionSection title="Details">
                    <MarkdownDisplay>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                    <Text style={{fontWeight: 'bold', marginTop: 16}}>Ort:</Text>
                    <Text>{event.location || 'N/A'}</Text>
                    <Text style={{fontWeight: 'bold', marginTop: 8}}>Leitung:</Text>
                    <Text>{event.leaderUsername || 'N/A'}</Text>
                </AccordionSection>

                <AccordionSection title="Team">
                    {isAdmin ? <AdminEventTeamTab event={event} onTeamUpdate={reloadEventDetails} /> : <Text>Teamansicht in Kürze verfügbar.</Text>}
                </AccordionSection>

                <AccordionSection title="Aufgaben">
                    <UserTaskView event={event} user={user} categories={categories || []} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} showDoneTasks={showDoneTasks} onShowDoneTasksToggle={() => setShowDoneTasks(!showDoneTasks)} styles={styles} colors={colors} />
                </AccordionSection>
                
                {canManageTasks && (
                    <AccordionSection title="Aufgaben (Admin)">
                        <AdminEventTasksTab event={event} onUpdate={reloadEventDetails} />
                    </AccordionSection>
                )}
                
                <AccordionSection title="Checkliste">
                    <ChecklistTab event={event} />
                </AccordionSection>

                {event.status === 'LAUFEND' && (
                    <AccordionSection title="Chat">
                        <EventChatTab eventId={event.id} />
                    </AccordionSection>
                )}

                {event.status === 'ABGESCHLOSSEN' && (
                    <AccordionSection title="Galerie">
                        <EventGalleryTab event={event} user={user} />
                    </AccordionSection>
                )}
            </View>

            <TaskModal
                isOpen={isTaskModalOpen}
                onClose={() => setIsTaskModalOpen(false)}
                onSuccess={() => { setIsTaskModalOpen(false); reloadEventDetails(); }}
                event={event}
                task={editingTask}
                allUsers={event.assignedAttendees || []}
                categories={categories || []}
            />

            <AdminModal
                isOpen={isStartConfirmModalOpen}
                onClose={() => setIsStartConfirmModalOpen(false)}
                onSubmit={performStartEvent}
                title="Event starten?"
                submitText="Starten"
                submitButtonVariant="success"
                isSubmitting={isStarting}
            >
                <Text style={styles.bodyText}>
                    Möchten Sie das Event "{event.name}" wirklich starten? Alle zugewiesenen Mitglieder werden benachrichtigt.
                </Text>
            </AdminModal>
            
            <AdminModal
                isOpen={isStopConfirmModalOpen}
                onClose={() => setIsStopConfirmModalOpen(false)}
                onSubmit={performStopEvent}
                title="Event beenden?"
                submitText="Beenden"
                submitButtonVariant="danger"
                isSubmitting={isStopping}
            >
                <Text style={styles.bodyText}>
                    Möchten Sie das Event "{event.name}" wirklich beenden? Der Status wird auf "Abgeschlossen" gesetzt und alle Teilnehmer werden benachrichtigt.
                </Text>
            </AdminModal>
        </ScrollableContent>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingTop: 16 },
        categoryContainer: { marginBottom: spacing.lg },
        categoryTitle: { fontSize: typography.h3, fontWeight: 'bold', marginBottom: spacing.md, borderBottomWidth: 1, borderColor: colors.border, paddingBottom: spacing.sm },
        lockedTask: { opacity: 0.7 },
        importantTask: { backgroundColor: 'rgba(255, 193, 7, 0.1)', borderColor: colors.warning },
        inProgressTask: { backgroundColor: 'rgba(40, 167, 69, 0.1)', borderColor: colors.success },
        doneTask: { backgroundColor: colors.background, opacity: 0.6 },
        doneTaskText: { textDecorationLine: 'line-through', color: colors.textMuted },
        taskName: { fontWeight: 'bold', marginBottom: spacing.xs, color: colors.text, fontSize: typography.body },
        displayOrder: { fontSize: typography.caption, color: colors.textMuted, fontWeight: 'bold' },
        metaContainer: { flexDirection: 'row', gap: spacing.md, marginVertical: spacing.sm },
        metaItem: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs },
        metaText: { color: colors.textMuted, fontSize: typography.small },
        assigneeList: { marginTop: spacing.sm, borderTopWidth: 1, borderColor: colors.border, paddingTop: spacing.sm },
        assigneeText: { color: colors.textMuted, fontSize: typography.small },
        // Kanban styles
        kanbanBoard: { paddingVertical: spacing.sm },
        kanbanColumn: { width: 300, backgroundColor: colors.background, borderRadius: borders.radius, padding: spacing.sm, marginRight: spacing.md, height: 500 },
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', padding: spacing.sm, color: colors.heading },
        taskCard: { backgroundColor: colors.surface, borderRadius: borders.radius, padding: spacing.md, marginBottom: spacing.sm, borderWidth: 1, borderColor: colors.border, ...shadows.sm },
        // Chat styles
        bubbleContainer: { flexDirection: 'row', maxWidth: '80%', marginVertical: spacing.xs },
		sent: { alignSelf: 'flex-end', justifyContent: 'flex-end' },
		received: { alignSelf: 'flex-start', justifyContent: 'flex-start' },
		bubble: { padding: spacing.sm, borderRadius: 18, flexShrink: 1 },
        sentBubble: { backgroundColor: colors.primary },
        sentText: { color: colors.white },
        receivedText: { color: colors.text },
        deletedText: { fontStyle: 'italic' },
		sender: { fontWeight: 'bold', fontSize: typography.small, marginBottom: 2 },
		timestamp: { fontSize: typography.caption, color: colors.textMuted },
		inputContainer: { flexDirection: 'row', padding: spacing.sm, borderTopWidth: 1, borderColor: colors.border, backgroundColor: colors.surface, gap: spacing.sm },
		chatInput: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 20, paddingHorizontal: spacing.md, backgroundColor: colors.background, maxHeight: 120, paddingVertical: 10 }
    });
};

export default EventDetailsPage;