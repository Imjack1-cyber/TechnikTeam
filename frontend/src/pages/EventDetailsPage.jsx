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
import { getThemeColors } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

// TaskList sub-component adapted for React Native
const TaskList = ({ title, tasks, onToggle, isCollapsed, event, user, canManageTasks, isParticipant, onOpenModal, onAction }) => {
    const styles = getCommonStyles();
    const colors = getThemeColors();
    if (tasks.length === 0) return null;

    return (
        <View style={{ marginBottom: 16 }}>
            <TouchableOpacity onPress={onToggle} style={styles.accordionHeader}>
                <Text style={styles.cardTitle}>{title} ({tasks.length})</Text>
                <Icon name={isCollapsed ? 'chevron-right' : 'chevron-down'} size={16} />
            </TouchableOpacity>
            {!isCollapsed && tasks.map(task => (
                <View key={task.id} style={styles.card}>
                    <Text style={styles.cardTitle}>{task.description}</Text>
                    <MarkdownDisplay>{task.details || ''}</MarkdownDisplay>
                    <Text>Zugewiesen an: {task.assignedUsers.map(u => u.username).join(', ') || 'Niemand'}</Text>
                    {/* Add action buttons here */}
                </View>
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
	
    const [activeTab, setActiveTab] = useState('details');

	useEffect(() => {
		if (lastUpdatedEvent && lastUpdatedEvent.id === parseInt(eventId, 10)) {
			reloadEventDetails();
		}
	}, [lastUpdatedEvent, eventId, reloadEventDetails]);

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!event) return <View style={styles.centered}><Text>Event nicht gefunden.</Text></View>;
    
    const isParticipant = event.userAttendanceStatus === 'ANGEMELDET' || event.userAttendanceStatus === 'ZUGEWIESEN';

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Text style={styles.title}>{event.name}</Text>
				<StatusBadge status={event.status} />
			</View>
			<Text style={styles.subtitle}>
				{new Date(event.eventDateTime).toLocaleString('de-DE')}
			</Text>

            {/* Tab Buttons */}
			<ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tabContainer}>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'details' && styles.activeTab]} onPress={() => setActiveTab('details')}><Text style={styles.tabText}>Details</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'team' && styles.activeTab]} onPress={() => setActiveTab('team')}><Text style={styles.tabText}>Team</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'tasks' && styles.activeTab]} onPress={() => setActiveTab('tasks')}><Text style={styles.tabText}>Aufgaben</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.tabButton, activeTab === 'checklist' && styles.activeTab]} onPress={() => setActiveTab('checklist')}><Text style={styles.tabText}>Checkliste</Text></TouchableOpacity>
                {event.status === 'LAUFEND' && <TouchableOpacity style={[styles.tabButton, activeTab === 'chat' && styles.activeTab]} onPress={() => setActiveTab('chat')}><Text style={styles.tabText}>Chat</Text></TouchableOpacity>}
                {event.status === 'ABGESCHLOSSEN' && <TouchableOpacity style={[styles.tabButton, activeTab === 'gallery' && styles.activeTab]} onPress={() => setActiveTab('gallery')}><Text style={styles.tabText}>Galerie</Text></TouchableOpacity>}
            </ScrollView>

            {/* Tab Content */}
            <View style={styles.contentContainer}>
                {activeTab === 'details' && (
                    <View>
                        <Text style={styles.cardTitle}>Beschreibung</Text>
                        <MarkdownDisplay>{event.description || 'Keine Beschreibung.'}</MarkdownDisplay>
                        <Text style={styles.cardTitle}>Details</Text>
                        <Text>Ort: {event.location || 'N/A'}</Text>
                        <Text>Leitung: {event.leaderUsername || 'N/A'}</Text>
                    </View>
                )}
                {activeTab === 'team' && <AdminEventTeamTab event={event} onTeamUpdate={reloadEventDetails} />}
                {activeTab === 'checklist' && <ChecklistTab event={event} user={user} />}
                {activeTab === 'gallery' && <EventGalleryTab event={event} user={user} />}
                {/* Other tabs would be implemented similarly */}
            </View>
		</ScrollView>
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
    });
};

export default EventDetailsPage;