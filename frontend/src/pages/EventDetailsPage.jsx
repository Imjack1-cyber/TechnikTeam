import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
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
import { getThemeColors, spacing, typography } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import BouncyCheckbox from "react-native-bouncy-checkbox";

// TaskList sub-component adapted for React Native
const TaskList = ({ category, tasks, event, user, canManageTasks, isParticipant, onOpenModal, onAction }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    if (!tasks || tasks.length === 0) return null;

    const taskStatusMap = useMemo(() => {
        const map = new Map();
        event.eventTasks.forEach(task => map.set(task.id, task.status));
        return map;
    }, [event.eventTasks]);

    // Sort tasks by their display_order
    const sortedTasks = [...tasks].sort((a, b) => a.displayOrder - b.displayOrder);

    return (
        <View style={styles.categoryContainer}>
            <Text style={styles.categoryTitle}>{category?.name || 'Unkategorisiert'}</Text>
            {sortedTasks.map(task => {
                const isAssigned = task.assignedUsers.some(u => u.id === user.id);
                const isBlocked = task.status === 'LOCKED';
                const isInProgress = task.status === 'IN_PROGRESS';
                const needsHelp = isInProgress && task.assignedUsers.length < task.requiredPersons;
                
                let cardStyle = styles.card;
                if (isBlocked) cardStyle = { ...cardStyle, ...styles.lockedTask };
                else if (task.isImportant || needsHelp) cardStyle = { ...cardStyle, ...styles.importantTask };
                else if (isInProgress) cardStyle = { ...cardStyle, ...styles.inProgressTask };

                return (
                    <View key={task.id} style={cardStyle}>
                        <View style={{flexDirection: 'row', alignItems: 'center', gap: spacing.sm}}>
                            {isBlocked && <Icon name="lock" size={16} color={colors.textMuted}/>}
                            <Text style={styles.cardTitle}>{task.name}</Text>
                        </View>
                        <MarkdownDisplay>{task.description || ''}</MarkdownDisplay>
                        {task.dependsOn && task.dependsOn.length > 0 && (
                            <View style={{marginVertical: spacing.sm}}>
                                <Text style={{fontWeight: 'bold'}}>Benötigt:</Text>
                                {task.dependsOn.map(dep => (
                                    <Text key={dep.id}>- {taskStatusMap.get(dep.id) === 'DONE' ? '✅' : '⏳'} {dep.name || `Aufgabe #${dep.id}`}</Text>
                                ))}
                            </View>
                        )}
                        <Text>Zugewiesen an: {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</Text>
                        <View style={{flexDirection: 'row', justifyContent: 'space-between', marginTop: spacing.sm}}>
                            <Text>Benötigt: {task.requiredPersons} Person(en)</Text>
                            <Text>Aktiv: {task.assignedUsers.length}</Text>
                        </View>

                        <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 8}}>
                           {canManageTasks && <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onOpenModal(task)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>}
                           {!isAssigned && isParticipant && task.status === 'OPEN' && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Starten</Text></TouchableOpacity>}
                           {!isAssigned && isParticipant && isInProgress && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Beitreten</Text></TouchableOpacity>}
                           {isAssigned && <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => onAction(task.id, 'unclaim')}><Text style={styles.buttonText}>Verlassen</Text></TouchableOpacity>}
                           {isAssigned && <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => onAction(task.id, 'updateStatus', 'DONE')}><Text style={styles.buttonText}>Abschließen</Text></TouchableOpacity>}
                        </View>
                    </View>
                );
            })}
        </View>
    );
};


const UserTaskView = ({ event, user, canManageTasks, isParticipant, onOpenModal, onAction, showDoneTasks, onShowDoneTasksToggle, styles }) => {
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

        const orderedCategories = categories ? [...categories, {id: 0, name: 'Unkategorisiert'}] : [{id: 0, name: 'Unkategorisiert'}];
        
        return orderedCategories.map(cat => ({
            ...cat,
            tasks: tasksByCat[cat.id] || []
        })).filter(cat => cat.tasks.length > 0);

    }, [event.eventTasks, showDoneTasks, categories]);

    if (categoriesLoading) {
        return <ActivityIndicator />;
    }

    return (
        <View>
            <View style={{ flexDirection: 'row', alignItems: 'center', marginBottom: spacing.md }}>
                <BouncyCheckbox isChecked={showDoneTasks} onPress={onShowDoneTasksToggle} />
                <Text>Erledigte Aufgaben anzeigen</Text>
            </View>
            {categorizedTasks.map(categoryData => (
                <TaskList
                    key={categoryData.id}
                    category={categoryData}
                    tasks={categoryData.tasks}
                    event={event} user={user}
                    canManageTasks={canManageTasks}
                    isParticipant={isParticipant}
                    onOpenModal={onOpenModal}
                    onAction={onAction}
                />
            ))}
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
	
    const [activeTab, setActiveTab] = useState('tasks');
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);
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


	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!event) return <View style={styles.centered}><Text>Event nicht gefunden.</Text></View>;
    
    const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';
    const canManageTasks = isAdmin || user.id === event.leaderUserId;
    
	return (
        <>
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Text style={styles.title}>{event.name}</Text>
				<StatusBadge status={event.status} />
			</View>
			<Text style={styles.subtitle}>
				{new Date(event.eventDateTime).toLocaleString('de-DE')}
			</Text>

			<ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tabContainer}>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'details' && styles.activeTab]} onPress={() => setActiveTab('details')}><Text style={[styles.tabText, activeTab === 'details' && styles.activeTabText]}>Details</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'team' && styles.activeTab]} onPress={() => setActiveTab('team')}><Text style={[styles.tabText, activeTab === 'team' && styles.activeTabText]}>Team</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'tasks' && styles.activeTab]} onPress={() => setActiveTab('tasks')}><Text style={[styles.tabText, activeTab === 'tasks' && styles.activeTabText]}>Aufgaben</Text></TouchableOpacity>
                {canManageTasks && <TouchableOpacity style={[styles.tabButton, activeTab === 'tasks-admin' && styles.activeTab]} onPress={() => setActiveTab('tasks-admin')}><Text style={[styles.tabText, activeTab === 'tasks-admin' && styles.activeTabText]}>Aufgaben (Admin)</Text></TouchableOpacity>}
                <TouchableOpacity style={[styles.tabButton, activeTab === 'checklist' && styles.activeTab]} onPress={() => setActiveTab('checklist')}><Text style={[styles.tabText, activeTab === 'checklist' && styles.activeTabText]}>Checkliste</Text></TouchableOpacity>
                {event.status === 'LAUFEND' && <TouchableOpacity style={[styles.tabButton, activeTab === 'chat' && styles.activeTab]} onPress={() => setActiveTab('chat')}><Text style={[styles.tabText, activeTab === 'chat' && styles.activeTabText]}>Chat</Text></TouchableOpacity>}
                {event.status === 'ABGESCHLOSSEN' && <TouchableOpacity style={[styles.tabButton, activeTab === 'gallery' && styles.activeTab]} onPress={() => setActiveTab('gallery')}><Text style={[styles.tabText, activeTab === 'gallery' && styles.activeTabText]}>Galerie</Text></TouchableOpacity>}
            </ScrollView>

            <View style={styles.contentContainer}>
                {activeTab === 'details' && (
                    <View style={styles.card}>
                        <Text style={styles.cardTitle}>Beschreibung</Text>
                        <MarkdownDisplay>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                        <Text style={[styles.cardTitle, {marginTop: 16}]}>Details</Text>
                        <Text>Ort: {event.location || 'N/A'}</Text>
                        <Text>Leitung: {event.leaderUsername || 'N/A'}</Text>
                    </View>
                )}
                 {activeTab === 'team' && (isAdmin ? <AdminEventTeamTab event={event} onTeamUpdate={reloadEventDetails} /> : <Text>Teamansicht in Kürze verfügbar.</Text>)}
                 {activeTab === 'tasks-admin' && <AdminEventTasksTab event={event} onUpdate={reloadEventDetails} />}
                 {activeTab === 'tasks' && <UserTaskView event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} showDoneTasks={showDoneTasks} onShowDoneTasksToggle={() => setShowDoneTasks(!showDoneTasks)} styles={styles} />}
                 {activeTab === 'checklist' && <ChecklistTab event={event} user={user} />}
                 {activeTab === 'gallery' && <EventGalleryTab event={event} user={user} />}
                 {activeTab === 'chat' && <Text>Chat in Kürze hier verfügbar.</Text>}
            </View>

            <TouchableOpacity style={[styles.button, styles.secondaryButton, { margin: spacing.md, alignSelf: 'flex-start' }]} onPress={() => navigation.goBack()}>
                <Icon name="arrow-left" size={14} color={colors.white} />
                <Text style={styles.buttonText}>Zurück zur Event-Übersicht</Text>
            </TouchableOpacity>
		</ScrollView>
        <TaskModal
            isOpen={isTaskModalOpen}
            onClose={() => setIsTaskModalOpen(false)}
            onSuccess={() => { setIsTaskModalOpen(false); reloadEventDetails(); }}
            event={event}
            task={editingTask}
            allUsers={event.assignedAttendees}
            categories={[]}
        />
        </>
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
        lockedTask: { backgroundColor: colors.background, borderColor: colors.border, opacity: 0.7 },
        importantTask: { backgroundColor: 'rgba(220, 53, 69, 0.1)', borderColor: colors.danger },
        inProgressTask: { backgroundColor: 'rgba(255, 193, 7, 0.1)', borderColor: colors.warning },
    });
};

export default EventDetailsPage;