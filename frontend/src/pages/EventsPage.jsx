import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, ActivityIndicator, TextInput } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import useApi from '../hooks/useApi';
import Modal from '../components/ui/Modal';
import StatusBadge from '../components/ui/StatusBadge';
import { useToast } from '../context/ToastContext';
import { getCommonStyles } from '../styles/commonStyles';
import { useAuthStore } from '../store/authStore';

const ActionConfirmationModal = ({ isOpen, onClose, onConfirm, event, action, isSubmitting }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [reason, setReason] = useState('');

    if (!isOpen) return null;

    const isRunningSignoff = action === 'signoff' && event.status === 'LAUFEND';
    const title = action === 'signup' ? `Anmelden für "${event.name}"?` : `Abmelden von "${event.name}"?`;
    let message = action === 'signup'
        ? "Möchten Sie sich wirklich für diese Veranstaltung anmelden?"
        : "Möchten Sie sich wirklich von dieser Veranstaltung abmelden?";
    if (isRunningSignoff) {
        message += " Da das Event bereits läuft, ist die Angabe eines Grundes erforderlich.";
    }

    const confirmButtonText = action === 'signup' ? 'Anmelden' : 'Abmelden';
    const confirmButtonStyle = action === 'signup' ? styles.successButton : styles.dangerButton;

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title}>
            <View>
                <Text style={styles.bodyText}>{message}</Text>
                {isRunningSignoff && (
                    <View style={styles.formGroup}>
                        <Text style={styles.label}>Grund für die Abmeldung</Text>
                        <TextInput
                            style={[styles.input, styles.textArea]}
                            value={reason}
                            onChangeText={setReason}
                            placeholder="z.B. Krankheitsbedingt"
                            multiline
                        />
                    </View>
                )}
                <View style={{ flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 24 }}>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={onClose} disabled={isSubmitting}>
                        <Text style={styles.buttonText}>Abbrechen</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                        style={[styles.button, confirmButtonStyle, (isRunningSignoff && !reason.trim()) && styles.disabledButton]}
                        onPress={() => onConfirm(reason)}
                        disabled={isSubmitting || (isRunningSignoff && !reason.trim())}
                    >
                        {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>{confirmButtonText}</Text>}
                    </TouchableOpacity>
                </View>
            </View>
        </Modal>
    );
};


const EventsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/events'), []);
	const { data: events, loading, error, reload } = useApi(apiCall, { subscribeTo: 'EVENT' });
	const { addToast } = useToast();
    const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const [modalState, setModalState] = useState({ isOpen: false, event: null, action: null });
	const [isSubmitting, setIsSubmitting] = useState(false);

	const openModal = (event, action) => setModalState({ isOpen: true, event, action });
	const closeModal = () => setModalState({ isOpen: false, event: null, action: null });

	const handleAction = async (reason = '') => {
        const { event, action } = modalState;
        if (!event || !action) return;

		setIsSubmitting(true);
		try {
			const result = await apiClient.post(`/public/events/${event.id}/${action}`, { reason });
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
        const isSignUpAction = event.userAttendanceStatus === 'OFFEN' || event.userAttendanceStatus === 'ABGEMELDET';
        const action = isSignUpAction ? 'signup' : 'signoff';
        
        // Users can sign up for planned or running events, but cannot sign off from completed/cancelled ones.
        const canPerformAction = event.status === 'GEPLANT' || event.status === 'LAUFEND';
        const isSignupDisabled = isSignUpAction && (!event.userQualified || !canPerformAction);

        return (
            <View style={styles.card}>
                <TouchableOpacity onPress={() => navigation.navigate('EventDetails', { eventId: event.id })}>
                    <Text style={styles.cardTitle}>{event.name}</Text>
                </TouchableOpacity>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Wann:</Text><Text style={styles.detailsListValue}>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text></View>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Status:</Text><StatusBadge status={event.status} /></View>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Dein Status:</Text><Text style={styles.detailsListValue}>{event.userAttendanceStatus}</Text></View>
                
                {canPerformAction && (
                    <View style={styles.cardActions}>
                        <TouchableOpacity 
                            style={[styles.button, isSignUpAction ? styles.successButton : styles.dangerButton, isSignupDisabled && styles.disabledButton]} 
                            onPress={() => openModal(event, action)}
                            disabled={isSignupDisabled}
                        >
                            <Text style={styles.buttonText}>{isSignUpAction ? 'Anmelden' : 'Abmelden'}</Text>
                        </TouchableOpacity>
                    </View>
                )}
            </View>
        );
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
        <>
            <FlatList
                data={events}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListHeaderComponent={<Text style={styles.title}>Anstehende Veranstaltungen</Text>}
                ListEmptyComponent={<View style={styles.card}><Text>Keine anstehenden Veranstaltungen geplant.</Text></View>}
            />
            <ActionConfirmationModal 
                isOpen={modalState.isOpen}
                onClose={closeModal}
                onConfirm={handleAction}
                event={modalState.event}
                action={modalState.action}
                isSubmitting={isSubmitting}
            />
        </>
	);
};

export default EventsPage;