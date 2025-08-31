import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import StatusBadge from '../components/ui/StatusBadge';
import { useToast } from '../context/ToastContext';
import { getCommonStyles } from '../styles/commonStyles';

const EventsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/events'), []);
	const { data: events, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const navigation = useNavigation();
    const styles = getCommonStyles(); // Using default theme

	const [modalState, setModalState] = useState({ isOpen: false, type: null, event: null });
	const [isSubmitting, setIsSubmitting] = useState(false);

	const openModal = (type, event) => setModalState({ isOpen: true, type, event });
	const closeModal = () => setModalState({ isOpen: false, type: null, event: null });

	const handleAction = async (action, eventId, reason = '') => {
		setIsSubmitting(true);
		try {
			const result = await apiClient.post(`/public/events/${eventId}/${action}`, { reason });
			if (result.success) {
				addToast(`Erfolgreich ${action === 'signup' ? 'an' : 'ab'}gemeldet!`, 'success');
				closeModal();
				reload();
			} else { throw new Error(result.message); }
		} catch (err) {
			addToast(err.message, 'error');
		} finally {
			setIsSubmitting(false);
		}
	};

    const renderItem = ({ item: event }) => {
        const action = event.userAttendanceStatus === 'OFFEN' || event.userAttendanceStatus === 'ABGEMELDET' ? 'signup' : 'signoff';
        return (
            <View style={styles.card}>
                <TouchableOpacity onPress={() => navigation.navigate('EventDetails', { eventId: event.id })}>
                    <Text style={styles.cardTitle}>{event.name}</Text>
                </TouchableOpacity>
                <View style={styles.detailRow}><Text style={styles.label}>Wann:</Text><Text>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text></View>
                <View style={styles.detailRow}><Text style={styles.label}>Status:</Text><StatusBadge status={event.status} /></View>
                <View style={styles.detailRow}><Text style={styles.label}>Dein Status:</Text><Text>{event.userAttendanceStatus}</Text></View>
                <View style={styles.cardActions}>
                    <TouchableOpacity 
                        style={[styles.button, action === 'signup' ? styles.successButton : styles.dangerButton]} 
                        onPress={() => handleAction(action, event.id)}
                        disabled={action === 'signup' && !event.userQualified}
                    >
                        <Text style={styles.buttonText}>{action === 'signup' ? 'Anmelden' : 'Abmelden'}</Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
        <FlatList
            data={events}
            renderItem={renderItem}
            keyExtractor={item => item.id.toString()}
            contentContainerStyle={styles.contentContainer}
            ListHeaderComponent={<Text style={styles.title}>Anstehende Veranstaltungen</Text>}
            ListEmptyComponent={<View style={styles.card}><Text>Keine anstehenden Veranstaltungen geplant.</Text></View>}
        />
	);
};

export default EventsPage;