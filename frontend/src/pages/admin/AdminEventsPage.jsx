import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import useAdminData from '../../hooks/useAdminData';
import EventModal from '../../components/admin/events/EventModal';
import StatusBadge from '../../components/ui/StatusBadge';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const AdminEventsPage = () => {
    const navigation = useNavigation();
	const eventsApiCall = useCallback(() => apiClient.get('/events'), []);
	const templatesApiCall = useCallback(() => apiClient.get('/admin/checklist-templates'), []);

	const { data: events, loading: eventsLoading, error: eventsError, reload } = useApi(eventsApiCall, { subscribeTo: 'EVENT' });
	const { data: templates, loading: templatesLoading } = useApi(templatesApiCall);
	const adminFormData = useAdminData();
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingEvent, setEditingEvent] = useState(null);
    const [cloningEvent, setCloningEvent] = useState(null);
    const [deletingEvent, setDeletingEvent] = useState(null);
    const [isSubmittingAction, setIsSubmittingAction] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (event = null) => {
		setEditingEvent(event);
		setIsModalOpen(true);
	};

	const handleSuccess = () => { setIsModalOpen(false); setEditingEvent(null); reload(); };

	const confirmClone = async () => {
        if (!cloningEvent) return;
        setIsSubmittingAction(true);
        try {
            const result = await apiClient.post(`/events/${cloningEvent.id}/clone`);
            if (result.success) {
                addToast('Event erfolgreich geklont.', 'success');
                navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: result.data.id } });
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Klonen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingAction(false);
            setCloningEvent(null);
        }
	};

	const confirmDelete = async () => {
        if (!deletingEvent) return;
        setIsSubmittingAction(true);
        try {
            const result = await apiClient.delete(`/events/${deletingEvent.id}`);
            if (result.success) {
                addToast('Event erfolgreich gelöscht', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingAction(false);
            setDeletingEvent(null);
        }
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <TouchableOpacity onPress={() => navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: item.id } })}>
                <Text style={styles.cardTitle}>{item.name}</Text>
            </TouchableOpacity>
            <View style={styles.detailRow}><Text style={styles.label}>Datum:</Text><Text style={styles.value}>{new Date(item.eventDateTime).toLocaleString('de-DE')}</Text></View>
            <View style={styles.detailRow}><Text style={styles.label}>Status:</Text><StatusBadge status={item.status} /></View>
            <View style={styles.actionsContainer}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, {backgroundColor: colors.primaryLight}]} onPress={() => setCloningEvent(item)}><Text style={{color: colors.primary}}>Klonen</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, {backgroundColor: colors.info}]} onPress={() => navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: item.id } })}><Text style={styles.buttonText}>Details</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => setDeletingEvent(item)}><Text style={styles.dangerOutlineButtonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	const loading = eventsLoading || adminFormData.loading || templatesLoading;

	return (
		<View style={styles.container}>
			<TouchableOpacity style={[styles.button, styles.successButton, {margin: 16}]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neues Event erstellen</Text>
            </TouchableOpacity>
			{loading && <ActivityIndicator size="large" />}
			{eventsError && <Text style={styles.errorText}>{eventsError}</Text>}
			<FlatList
                data={events}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{paddingHorizontal: 16}}
            />
			{isModalOpen && !loading && (
				<EventModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} event={editingEvent} adminFormData={adminFormData} checklistTemplates={templates || []} />
			)}
            {cloningEvent && (
                <ConfirmationModal
                    isOpen={!!cloningEvent}
                    onClose={() => setCloningEvent(null)}
                    onConfirm={confirmClone}
                    title={`Event "${cloningEvent.name}" klonen?`}
                    message="Ein neues Event wird mit den Daten dieses Events als Vorlage erstellt und Sie werden zur Detailansicht des Klons weitergeleitet."
                    confirmText="Klonen"
                    confirmButtonVariant="primary"
                    isSubmitting={isSubmittingAction}
                />
            )}
            {deletingEvent && (
                 <ConfirmationModal
                    isOpen={!!deletingEvent}
                    onClose={() => setDeletingEvent(null)}
                    onConfirm={confirmDelete}
                    title={`Event "${deletingEvent.name}" löschen?`}
                    message="Diese Aktion kann nicht rückgängig gemacht werden."
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingAction}
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        actionsContainer: {
            flexDirection: 'row',
            flexWrap: 'wrap',
            gap: spacing.sm,
            marginTop: spacing.md,
            justifyContent: 'flex-end'
        },
    });
};

export default AdminEventsPage;