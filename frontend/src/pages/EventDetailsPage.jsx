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
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, borders } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

// TaskList sub-component adapted for React Native
const TaskList = ({ title, tasks, onToggle, isCollapsed, event, user, canManageTasks, isParticipant, onOpenModal, onAction }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    if (!tasks || tasks.length === 0) return null;

    return (
        <View style={{ marginBottom: 16 }}>
            <TouchableOpacity onPress={onToggle} style={styles.accordionHeader}>
                <Text style={styles.cardTitle}>{title} ({tasks.length})</Text>
                <Icon name={isCollapsed ? 'chevron-right' : 'chevron-down'} size={16} />
            </TouchableOpacity>
            {!isCollapsed && tasks.map(task => {
                const isAssigned = task.assignedUsers.some(u => u.id === user.id);
                return (
                    <View key={task.id} style={styles.card}>
                        <Text style={styles.cardTitle}>{task.description}</Text>
                        <MarkdownDisplay>{task.details || ''}</MarkdownDisplay>
                        <Text>Zugewiesen an: {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</Text>
                        <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 8}}>
                           {canManageTasks && <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onOpenModal(task)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>}
                           {!isAssigned && isParticipant && task.assignedUsers.length === 0 && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => onAction(task.id, 'claim')}><Text style={styles.buttonText}>Übernehmen</Text></TouchableOpacity>}
                           {isAssigned && <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => onAction(task.id, 'unclaim')}><Text style={styles.buttonText}>Verlassen</Text></TouchableOpacity>}
                        </View>
                    </View>
                );
            })}
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
	
    const [activeTab, setActiveTab] = useState('details');
    const [collapsedSections, setCollapsedSections] = useState({ open: false, inProgress: false, done: false });
    const [isTaskModalOpen, setIsTaskModalOpen] = useState(false);
    const [editingTask, setEditingTask] = useState(null);

	useEffect(() => {
		if (lastUpdatedEvent && lastUpdatedEvent.id === parseInt(eventId, 10)) {
			reloadEventDetails();
		}
	}, [lastUpdatedEvent, eventId, reloadEventDetails]);

    const handleToggleSection = (section) => {
        setCollapsedSections(prev => ({ ...prev, [section]: !prev[section] }));
    };

    const handleOpenTaskModal = (task = null) => {
        setEditingTask(task);
        setIsTaskModalOpen(true);
    };

    const handleTaskAction = async (taskId, action) => {
        try {
            const result = await apiClient.post(`/events/${eventId}/tasks/${taskId}/action`, { action });
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
    const tasks = event.eventTasks || [];
    const openTasks = tasks.filter(t => t.status === 'OFFEN');
    const inProgressTasks = tasks.filter(t => t.status === 'IN_ARBEIT');
    const doneTasks = tasks.filter(t => t.status === 'ERLEDIGT');


    const renderTabContent = () => {
        switch(activeTab) {
            case 'details':
                 return (
                    <View style={styles.card}>
                        <Text style={styles.cardTitle}>Beschreibung</Text>
                        <MarkdownDisplay>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                        <Text style={[styles.cardTitle, {marginTop: 16}]}>Details</Text>
                        <Text>Ort: {event.location || 'N/A'}</Text>
                        <Text>Leitung: {event.leaderUsername || 'N/A'}</Text>
                    </View>
                );
            case 'team':
                return isAdmin ? <AdminEventTeamTab event={event} onTeamUpdate={reloadEventDetails} /> : <Text>Teamansicht in Kürze verfügbar.</Text>;
            case 'tasks':
                return (
                    <View>
                        {canManageTasks && <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleOpenTaskModal()}><Text style={styles.buttonText}>Neue Aufgabe</Text></TouchableOpacity>}
                        <TaskList title="Offen" tasks={openTasks} isCollapsed={collapsedSections.open} onToggle={() => handleToggleSection('open')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} />
                        <TaskList title="In Arbeit" tasks={inProgressTasks} isCollapsed={collapsedSections.inProgress} onToggle={() => handleToggleSection('inProgress')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} />
                        <TaskList title="Erledigt" tasks={doneTasks} isCollapsed={collapsedSections.done} onToggle={() => handleToggleSection('done')} event={event} user={user} canManageTasks={canManageTasks} isParticipant={isParticipant} onOpenModal={handleOpenTaskModal} onAction={handleTaskAction} />
                    </View>
                );
            case 'checklist':
                return <ChecklistTab event={event} user={user} />;
            case 'gallery':
                return <EventGalleryTab event={event} user={user} />;
            case 'chat':
                 return <Text>Chat in Kürze hier verfügbar.</Text>; // Placeholder
            default:
                return null;
        }
    };


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
                <TouchableOpacity style={[styles.tabButton, activeTab === 'checklist' && styles.activeTab]} onPress={() => setActiveTab('checklist')}><Text style={[styles.tabText, activeTab === 'checklist' && styles.activeTabText]}>Checkliste</Text></TouchableOpacity>
                {event.status === 'LAUFEND' && <TouchableOpacity style={[styles.tabButton, activeTab === 'chat' && styles.activeTab]} onPress={() => setActiveTab('chat')}><Text style={[styles.tabText, activeTab === 'chat' && styles.activeTabText]}>Chat</Text></TouchableOpacity>}
                {event.status === 'ABGESCHLOSSEN' && <TouchableOpacity style={[styles.tabButton, activeTab === 'gallery' && styles.activeTab]} onPress={() => setActiveTab('gallery')}><Text style={[styles.tabText, activeTab === 'gallery' && styles.activeTabText]}>Galerie</Text></TouchableOpacity>}
            </ScrollView>

            <View style={styles.contentContainer}>
                {renderTabContent()}
            </View>
		</ScrollView>
        <TaskModal
            isOpen={isTaskModalOpen}
            onClose={() => setIsTaskModalOpen(false)}
            onSuccess={() => { setIsTaskModalOpen(false); reloadEventDetails(); }}
            event={event}
            task={editingTask}
            allUsers={event.assignedAttendees}
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
        accordionHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', padding: spacing.md, backgroundColor: colors.background, borderBottomWidth: 1, borderColor: colors.border }
    });
};

export default EventDetailsPage;