import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Alert, Platform, FlatList, TextInput, useWindowDimensions } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import { useAuthStore } from '../store/authStore';
import StatusBadge from '../components/ui/StatusBadge';
import MarkdownDisplay from 'react-native-markdown-display';
import { useToast } from '../context/ToastContext';
import ChecklistTab from '../components/events/ChecklistTab';
import EventGalleryTab from '../components/events/EventGalleryTab';
import TaskModal from '../components/admin/events/TaskModal';
import AdminEventTeamTab from '../components/admin/events/AdminEventTeamTab';
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
import TaskBoard from '../components/events/TaskBoard';

const EventChatTab = ({ eventId, styles, colors }) => {
    const user = useAuthStore(state => state.user);

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
                    placeholderTextColor={colors.textMuted}
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
	const { user, isAdmin } = useAuthStore();
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { width } = useWindowDimensions();
    const isLargeScreen = width >= 768;


	const eventApiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: initialEvent, loading: eventLoading, error: eventError, reload } = useApi(eventApiCall);

    const categoriesApiCall = useCallback(() => {
        // Only admins fetch the full list of categories to manage them.
        // Regular users derive categories from the tasks they receive.
        if (isAdmin && eventId) {
            return apiClient.get(`/admin/events/${eventId}/task-categories`);
        }
        return null;
    }, [eventId, isAdmin]);
    const { data: categories, loading: categoriesLoading, reload: reloadCategories } = useApi(categoriesApiCall);
	
    const [event, setEvent] = useState(null);
    const [activeTab, setActiveTab] = useState('tasks');
    const [isStarting, setIsStarting] = useState(false);
    const [isStopping, setIsStopping] = useState(false);
    const [isStartConfirmModalOpen, setIsStartConfirmModalOpen] = useState(false);
    const [isStopConfirmModalOpen, setIsStopConfirmModalOpen] = useState(false);

    useEffect(() => {
        if (initialEvent) {
            setEvent(initialEvent);
            // Context-aware default tab
            if (initialEvent.status === 'GEPLANT') {
                setActiveTab('checklist');
            } else if (initialEvent.status === 'LAUFEND') {
                setActiveTab('tasks');
            } else {
                setActiveTab('details');
            }
        }
    }, [initialEvent]);

    const handleWebSocketMessage = useCallback((message) => {
        if (message.type === 'EVENT_FULL_UPDATE' && message.payload?.id === parseInt(eventId, 10)) {
            setEvent(message.payload);
        }
    }, [eventId]);

    useWebSocket(`/ws/event/${eventId}`, handleWebSocketMessage, [eventId]);

    const handleStartEvent = () => {
        setIsStartConfirmModalOpen(true);
    };

    const performStartEvent = async () => {
        setIsStarting(true);
        try {
            const result = await apiClient.post(`/events/${eventId}/start`);
            if (result.success) {
                addToast('Event erfolgreich gestartet.', 'success');
                // No reload needed
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
                // No reload needed
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

    const handleUpdate = () => {
        reload();
        reloadCategories();
    };

	if (eventLoading || categoriesLoading || !event) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (eventError) return <View style={styles.centered}><Text style={styles.errorText}>{eventError}</Text></View>;
    
    const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';
    const canManage = isAdmin || user.id === event.leaderUserId;

    const renderTabContent = () => {
        switch (activeTab) {
            case 'tasks':
                return <TaskBoard event={event} user={user} categories={categories || []} canManageTasks={canManage} isParticipant={isParticipant} onUpdate={handleUpdate} />;
            case 'checklist':
                return <ChecklistTab event={event} canManage={canManage} />;
            case 'chat':
                return <EventChatTab eventId={event.id} styles={styles} colors={colors} />;
            case 'gallery':
                return <EventGalleryTab event={event} user={user} />;
            case 'details':
            default:
                return (
                    <View style={styles.detailsTabContent}>
                        <AccordionSection title="Details">
                            <MarkdownDisplay style={{body: {color: colors.text}}}>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                            <Text style={{fontWeight: 'bold', marginTop: 16, color: colors.text}}>Ort:</Text>
                            <Text style={{color: colors.text}}>{event.location || 'N/A'}</Text>
                            <Text style={{fontWeight: 'bold', marginTop: 8, color: colors.text}}>Leitung:</Text>
                            <Text style={{color: colors.text}}>{event.leaderUsername || 'N/A'}</Text>
                        </AccordionSection>
                        <AccordionSection title="Team">
                            {canManage ? <AdminEventTeamTab event={event} onTeamUpdate={handleUpdate} /> : <Text style={{color: colors.text}}>Teamansicht in Kürze verfügbar.</Text>}
                        </AccordionSection>
                    </View>
                );
        }
    };
    
    const tabs = ['details', 'tasks', 'checklist'];
    if (event.status === 'LAUFEND' || isLargeScreen) tabs.push('chat');
    if (event.status === 'ABGESCHLOSSEN') tabs.push('gallery');
    
    const tabLabels = { details: 'Details', tasks: 'Aufgaben', checklist: 'Checkliste', chat: 'Chat', gallery: 'Galerie' };

	return (
        <ScrollableContent style={styles.container}>
            <View style={styles.header}>
                <Text style={styles.title}>{event.name}</Text>
                <View style={{flexDirection: 'row', alignItems: 'center', gap: spacing.sm}}>
                    <StatusBadge status={event.status} />
                    {canManage && event.status === 'GEPLANT' && (
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
                    {canManage && event.status === 'LAUFEND' && (
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
            
            <View style={styles.tabContainer}>
                {tabs.map(tab => (
                    <TouchableOpacity
                        key={tab}
                        style={[styles.tabButton, activeTab === tab && styles.activeTab]}
                        onPress={() => setActiveTab(tab)}
                    >
                        <Text style={[styles.tabText, activeTab === tab && styles.activeTabText]}>{tabLabels[tab]}</Text>
                    </TouchableOpacity>
                ))}
            </View>

            <View style={[styles.mainArea, isLargeScreen && styles.mainAreaLarge]}>
                <View style={[isLargeScreen && styles.mainContentPanel]}>
                    {renderTabContent()}
                </View>
                {isLargeScreen && (activeTab === 'tasks' || activeTab === 'checklist') && (
                    <View style={styles.sideContentPanel}>
                         <EventChatTab eventId={event.id} styles={styles} colors={colors} />
                    </View>
                )}
            </View>

            <AdminModal isOpen={isStartConfirmModalOpen} onClose={() => setIsStartConfirmModalOpen(false)} onSubmit={performStartEvent} title="Event starten?" submitText="Starten" submitButtonVariant="success" isSubmitting={isStarting}>
                <Text style={styles.bodyText}>Möchten Sie das Event "{event.name}" wirklich starten? Alle zugewiesenen Mitglieder werden benachrichtigt.</Text>
            </AdminModal>
            
            <AdminModal isOpen={isStopConfirmModalOpen} onClose={() => setIsStopConfirmModalOpen(false)} onSubmit={performStopEvent} title="Event beenden?" submitText="Beenden" submitButtonVariant="danger" isSubmitting={isStopping}>
                <Text style={styles.bodyText}>Möchten Sie das Event "{event.name}" wirklich beenden? Der Status wird auf "Abgeschlossen" gesetzt und alle Teilnehmer werden benachrichtigt.</Text>
            </AdminModal>
        </ScrollableContent>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 16, paddingTop: 16, flexWrap: 'wrap' },
        tabContainer: { flexDirection: 'row', paddingHorizontal: spacing.md, borderBottomWidth: 1, borderColor: colors.border, backgroundColor: colors.surface },
        tabButton: { paddingVertical: spacing.md, paddingHorizontal: spacing.sm, marginRight: spacing.md, borderBottomWidth: 3, borderBottomColor: 'transparent' },
        activeTab: { borderBottomColor: colors.primary },
        tabText: { color: colors.textMuted, fontWeight: '500' },
        activeTabText: { color: colors.primary },
        detailsTabContent: { padding: spacing.md },
        mainArea: {},
        mainAreaLarge: { flexDirection: 'row', flex: 1 },
        mainContentPanel: { flex: 2 },
        sideContentPanel: { flex: 1, borderLeftWidth: 1, borderColor: colors.border },
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
		metaContainer: { flexDirection: 'row', alignSelf: 'flex-end', alignItems: 'center', gap: spacing.xs, marginTop: 4 },
		timestamp: { fontSize: typography.caption, color: colors.textMuted },
		inputContainer: { flexDirection: 'row', padding: spacing.sm, borderTopWidth: 1, borderColor: colors.border, backgroundColor: colors.surface, gap: spacing.sm, alignItems: 'center' },
		chatInput: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 20, paddingHorizontal: spacing.md, backgroundColor: colors.background, maxHeight: 120, paddingVertical: 10, color: colors.text }
    });
};

export default EventDetailsPage;