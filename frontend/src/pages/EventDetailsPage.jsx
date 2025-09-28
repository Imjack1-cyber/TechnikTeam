import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Alert, Platform, FlatList, TextInput } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
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

const Tab = createMaterialTopTabNavigator();

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
    const isInProgress = task.status === 'IN_PROGRESS';
    
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
                {!isAssigned && isParticipant && task.status === 'OPEN' && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Starten</Text></TouchableOpacity>}
                {!isAssigned && isParticipant && isInProgress && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Beitreten</Text></TouchableOpacity>}
                {isAssigned && <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => onAction(task.id, 'unclaim')}><Text style={styles.buttonText}>Verlassen</Text></TouchableOpacity>}
                {isAssigned && <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => onAction(task.id, 'updateStatus', 'DONE')}><Text style={styles.buttonText}>Abschließen</Text></TouchableOpacity>}
            </View>
        </View>
    );
};

const UserKanbanColumn = ({ category, tasks, ...props }) => (
    <View style={props.styles.kanbanColumn}>
        <Text style={props.styles.columnTitle}>{category.name}</Text>
        <ScrollView>
            {tasks.map(task => (
                <UserTaskCard key={task.id} task={task} {...props} />
            ))}
        </ScrollView>
    </View>
);

const UserTaskView = ({ event, user, canManageTasks, isParticipant, onOpenModal, onAction, showDoneTasks, onShowDoneTasksToggle, styles, colors }) => {
    const categoriesApiCall = useCallback(() => apiClient.get(`/admin/events/${event.id}/task-categories`), [event.id]);
    const { data: categories, loading: categoriesLoading } = useApi(categoriesApiCall);

    const categorizedTasks = useMemo(() => {
        if (!event.eventTasks) return {};
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

    if (categoriesLoading) {
        return <ActivityIndicator />;
    }

    return (
        <View style={{ flex: 1 }}>
             <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: spacing.md, paddingHorizontal: spacing.md, paddingTop: spacing.md }}>
                <BouncyCheckbox isChecked={showDoneTasks} onPress={onShowDoneTasksToggle} />
                <Text>Erledigte Aufgaben anzeigen</Text>
            </View>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                <View style={styles.kanbanBoard}>
                    {categorizedTasks.map(categoryData => (
                        <UserKanbanColumn
                            key={categoryData.id}
                            category={categoryData}
                            tasks={categoryData.tasks}
                            event={event}
                            user={user}
                            canManageTasks={canManageTasks}
                            isParticipant={isParticipant}
                            onOpenModal={onOpenModal}
                            onAction={onAction}
                            styles={styles}
                            colors={colors}
                        />
                    ))}
                </View>
            </ScrollView>
        </View>
    );
};

const DetailsTab = ({ route }) => {
    const { event } = route.params;
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    return (
        <ScrollableContent style={styles.container} contentContainerStyle={styles.contentContainer}>
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

const TeamTab = ({ route }) => {
    const { isAdmin } = route.params;
    if (isAdmin) {
        return <AdminEventTeamTab />;
    }
    return <View style={getCommonStyles().centered}><Text>Teamansicht in Kürze verfügbar.</Text></View>;
};

const TasksTab = ({ route }) => {
    const { event, user, canManageTasks, isParticipant, handleOpenTaskModal, handleTaskAction } = route.params;
    const [showDoneTasks, setShowDoneTasks] = useState(false);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    return <UserTaskView event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} showDoneTasks={showDoneTasks} onShowDoneTasksToggle={() => setShowDoneTasks(!showDoneTasks)} styles={styles} colors={colors} />;
};

const EventChatTab = ({ route }) => {
    const { eventId } = route.params;
    const user = useAuthStore(state => state.user);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');

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
        if (newMessage.trim()) {
            sendMessage({ type: 'new_message', payload: { messageText: newMessage } });
            setNewMessage('');
        }
    };

    if (messagesLoading) return <View style={styles.centered}><ActivityIndicator /></View>;
    if (messagesError) return <View style={styles.centered}><Text style={styles.errorText}>{messagesError}</Text></View>;

    return (
        <View style={{ flex: 1, backgroundColor: colors.surface }}>
            <FlatList
                data={messages}
                inverted
                keyExtractor={item => item.id.toString()}
                renderItem={({ item: msg }) => {
                    const isSentByMe = msg.userId === user.id;
                    return (
                        <View style={[styles.bubbleContainer, isSentByMe ? styles.sent : styles.received]}>
                            <View style={[styles.bubble, isSentByMe ? { backgroundColor: colors.primary } : { backgroundColor: msg.chatColor || colors.background }]}>
                                {!isSentByMe && <Text style={[styles.sender, { color: colors.primary }]}>{msg.username}</Text>}
                                {msg.isDeleted ? (
                                    <Text style={{ fontStyle: 'italic', color: isSentByMe ? colors.white : colors.textMuted }}>Nachricht gelöscht</Text>
                                ) : (
                                    <MarkdownDisplay style={{ body: { color: isSentByMe ? colors.white : colors.text } }}>{msg.messageText}</MarkdownDisplay>
                                )}
                                <View style={styles.metaContainer}>
                                    <Text style={[styles.timestamp, isSentByMe && { color: 'rgba(255,255,255,0.7)' }]}>{new Date(msg.sentAt).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</Text>
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
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit}>
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

	const apiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading, error, reload: reloadEventDetails } = useApi(apiCall);
	
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
    const [isStarting, setIsStarting] = useState(false);
    const [isStartConfirmModalOpen, setIsStartConfirmModalOpen] = useState(false);

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


	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!event) return <View style={styles.centered}><Text>Event nicht gefunden.</Text></View>;
    
    const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';
    const canManageTasks = isAdmin || user.id === event.leaderUserId;
    
	return (
        <View style={styles.container}>
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
                </View>
            </View>
            <Text style={styles.subtitle}>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text>
            
            <Tab.Navigator
                screenOptions={{
                    tabBarScrollEnabled: true,
                    tabBarItemStyle: { width: 'auto' },
                    tabBarIndicatorStyle: { backgroundColor: colors.primary },
                    tabBarLabelStyle: { fontFamily: 'System', fontSize: 14, textTransform: 'capitalize', fontWeight: '500' },
                }}
            >
                <Tab.Screen name="Details" component={DetailsTab} initialParams={{ event }} />
                <Tab.Screen name="Team" component={TeamTab} initialParams={{ event, onUpdate: reloadEventDetails, isAdmin }} />
                <Tab.Screen name="Aufgaben" component={TasksTab} initialParams={{ event, user, canManageTasks, isParticipant, handleOpenTaskModal, handleTaskAction }} />
                {canManageTasks && <Tab.Screen name="Aufgaben (Admin)" component={AdminEventTasksTab} initialParams={{ event, onUpdate: reloadEventDetails }} />}
                <Tab.Screen name="Checkliste" component={ChecklistTab} initialParams={{ event }} />
                {event.status === 'LAUFEND' && <Tab.Screen name="Chat" component={EventChatTab} initialParams={{ eventId: event.id }}/>}
                {event.status === 'ABGESCHLOSSEN' && <Tab.Screen name="Galerie" component={EventGalleryTab} initialParams={{ event, user }} />}
            </Tab.Navigator>
            
            <TaskModal
                isOpen={isTaskModalOpen}
                onClose={() => setIsTaskModalOpen(false)}
                onSuccess={() => { setIsTaskModalOpen(false); reloadEventDetails(); }}
                event={event}
                task={editingTask}
                allUsers={event.assignedAttendees}
                categories={[]}
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
        </View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingTop: 16 },
        tabContainer: { flexDirection: 'row', paddingHorizontal: 16, paddingVertical: 8, borderBottomWidth: 1, borderColor: colors.border },
        tabButton: { paddingVertical: 8, paddingHorizontal: 12, marginRight: 8, borderRadius: 8 },
        activeTab: { backgroundColor: colors.primary },
        tabText: { color: colors.text },
        activeTabText: { color: colors.white },
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
        kanbanBoard: { flexDirection: 'row', gap: spacing.md, padding: spacing.md },
        kanbanColumn: { width: 300, backgroundColor: colors.background, borderRadius: borders.radius, padding: spacing.sm, height: '100%'},
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', padding: spacing.sm, color: colors.heading },
        taskCard: { backgroundColor: colors.surface, borderRadius: borders.radius, padding: spacing.md, marginBottom: spacing.sm, borderWidth: 1, borderColor: colors.border, ...shadows.sm },
        // Chat styles
        bubbleContainer: { flexDirection: 'row', maxWidth: '80%', marginVertical: spacing.xs },
		sent: { alignSelf: 'flex-end', justifyContent: 'flex-end' },
		received: { alignSelf: 'flex-start', justifyContent: 'flex-start' },
		bubble: { padding: spacing.sm, borderRadius: 18, flexShrink: 1 },
		sender: { fontWeight: 'bold', fontSize: typography.small, marginBottom: 2 },
		timestamp: { fontSize: typography.caption, color: colors.textMuted },
		inputContainer: { flexDirection: 'row', padding: spacing.sm, borderTopWidth: 1, borderColor: colors.border, backgroundColor: colors.surface, gap: spacing.sm },
		chatInput: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 20, paddingHorizontal: spacing.md, backgroundColor: colors.background, maxHeight: 120, paddingVertical: 10 }
    });
};

export default EventDetailsPage;