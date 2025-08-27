import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import MarkdownDisplay from 'react-native-markdown-display';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { MultipleSelectList } from 'react-native-dropdown-select-list';

const AdminEventDebriefingPage = () => {
	const route = useRoute();
    const navigation = useNavigation();
	const { eventId } = route.params;
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const eventApiCall = useCallback(() => apiClient.get(`/public/events/${eventId}`), [eventId]);
	const { data: event, loading: eventLoading, error: eventError } = useApi(eventApiCall);

	const debriefingApiCall = useCallback(() => apiClient.get(`/admin/events/${eventId}/debriefing`), [eventId]);
	const { data: debriefing, loading: debriefingLoading, error: debriefingError, reload: reloadDebriefing } = useApi(debriefingApiCall);

	const [formData, setFormData] = useState({ whatWentWell: '', whatToImprove: '', equipmentNotes: '', standoutCrewMemberIds: [] });
	const [isEditing, setIsEditing] = useState(false);
	const [isSubmitting, setIsSubmitting] = useState(false);

	useEffect(() => {
		if (debriefing) {
			setFormData({
				whatWentWell: debriefing.whatWentWell || '',
				whatToImprove: debriefing.whatToImprove || '',
				equipmentNotes: debriefing.equipmentNotes || '',
				standoutCrewMemberIds: debriefing.standoutCrewDetails?.map(u => u.id) || [],
			});
			setIsEditing(false);
		} else {
			setIsEditing(true);
		}
	}, [debriefing]);

	const handleSubmit = async () => {
		setIsSubmitting(true);
		try {
			const result = await apiClient.post(`/admin/events/${eventId}/debriefing`, formData);
			if (result.success) {
				addToast('Debriefing gespeichert!', 'success');
				reloadDebriefing();
			} else { throw new Error(result.message); }
		} catch (err) {
			addToast(err.message, 'error');
		} finally {
			setIsSubmitting(false);
		}
	};

	const loading = eventLoading || debriefingLoading;
	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (eventError) return <View style={styles.centered}><Text style={styles.errorText}>{eventError}</Text></View>;
	if (!event) return <View style={styles.centered}><Text>Event nicht gefunden.</Text></View>;

	const canManage = isAdmin || (user.permissions && user.permissions.includes('EVENT_DEBRIEFING_MANAGE')) || user.id === event.leaderUserId;

	if (!isEditing && debriefing) {
		return (
			<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
				<Text style={styles.title}>Debriefing: {event.name}</Text>
				<Text style={styles.subtitle}>Eingereicht von {debriefing.authorUsername} am {new Date(debriefing.submittedAt).toLocaleString('de-DE')}</Text>
				{canManage && <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={() => setIsEditing(true)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>}
				<View style={styles.card}><Text style={styles.cardTitle}>Was lief gut?</Text><MarkdownDisplay>{debriefing.whatWentWell}</MarkdownDisplay></View>
				<View style={styles.card}><Text style={styles.cardTitle}>Was kann verbessert werden?</Text><MarkdownDisplay>{debriefing.whatToImprove}</MarkdownDisplay></View>
				<View style={styles.card}><Text style={styles.cardTitle}>Anmerkungen zum Material</Text><MarkdownDisplay>{debriefing.equipmentNotes || 'Keine.'}</MarkdownDisplay></View>
				<View style={styles.card}><Text style={styles.cardTitle}>Besonders hervorgehobene Mitglieder</Text><Text>{debriefing.standoutCrewDetails?.length > 0 ? debriefing.standoutCrewDetails.map(u => u.username).join(', ') : 'Niemand wurde besonders hervorgehoben.'}</Text></View>
                <TouchableOpacity style={[styles.button, styles.secondaryButton, {marginTop: 16}]} onPress={() => navigation.goBack()}><Text style={styles.buttonText}>Zurück</Text></TouchableOpacity>
			</ScrollView>
		);
	}

	if (!canManage) {
		return <View style={styles.centered}><Text style={styles.errorText}>Sie haben keine Berechtigung, dieses Debriefing zu bearbeiten.</Text></View>;
	}
    
    const crewOptions = event.assignedAttendees?.map(m => ({ key: m.id, value: m.username })) || [];

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>Debriefing für: {event.name}</Text>
			<Text style={styles.subtitle}>Fassen Sie die wichtigsten Punkte der Veranstaltung zusammen.</Text>
			<View style={styles.card}>
                <Text style={styles.label}>Was lief gut?</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.whatWentWell} onChangeText={val => setFormData({...formData, whatWentWell: val})} multiline required />
				
                <Text style={styles.label}>Was kann verbessert werden?</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.whatToImprove} onChangeText={val => setFormData({...formData, whatToImprove: val})} multiline required />

                <Text style={styles.label}>Anmerkungen zum Material</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.equipmentNotes} onChangeText={val => setFormData({...formData, equipmentNotes: val})} multiline />

                <Text style={styles.label}>Besonders hervorgehobene Mitglieder</Text>
                <MultipleSelectList 
                    setSelected={(val) => setFormData({...formData, standoutCrewMemberIds: val})} 
                    data={crewOptions} 
                    save="key"
                    label="Mitglieder"
                    placeholder="Mitglieder auswählen"
                    searchPlaceholder="Suchen"
                    boxStyles={styles.input}
                    defaultOptions={crewOptions.filter(opt => formData.standoutCrewMemberIds.includes(opt.key))}
                />
				
                <View style={styles.actionButtons}>
					<TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSubmit} disabled={isSubmitting}>
						<Text style={styles.buttonText}>{isSubmitting ? 'Speichern...' : 'Debriefing speichern'}</Text>
					</TouchableOpacity>
					{debriefing && <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => setIsEditing(false)} disabled={isSubmitting}><Text style={styles.buttonText}>Abbrechen</Text></TouchableOpacity>}
				</View>
			</View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
       actionButtons: { flexDirection: 'row', gap: spacing.sm, marginTop: spacing.md },
    });
};

export default AdminEventDebriefingPage;