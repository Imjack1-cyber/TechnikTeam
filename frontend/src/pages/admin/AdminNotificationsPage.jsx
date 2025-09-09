import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { Picker } from '@react-native-picker/picker';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { RadioButton } from 'react-native-paper'; // Example for radio buttons

const AdminNotificationsPage = () => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
	const { addToast } = useToast();
	const [formData, setFormData] = useState({ title: '', description: '', level: 'Informational', targetType: 'ALL', targetId: '', androidImportance: 'DEFAULT' });
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const fetchEvents = useCallback(() => apiClient.get('/events'), []);
	const fetchMeetings = useCallback(() => apiClient.get('/meetings?courseId=0'), []);

	const { data: events, loading: eventsLoading } = useApi(fetchEvents);
	const { data: meetings, loading: meetingsLoading } = useApi(fetchMeetings);

	const sendNotification = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const payload = { ...formData, targetId: formData.targetId ? parseInt(formData.targetId, 10) : null };
			const result = await apiClient.post('/admin/notifications', payload);
			if (result.success) {
				addToast(result.message, 'success');
				setFormData({ title: '', description: '', level: 'Informational', targetType: 'ALL', targetId: '', androidImportance: 'DEFAULT' });
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Senden fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

    const handleSendPress = () => {
        if (!formData.title || !formData.description) {
			setError('Titel und Beschreibung sind erforderlich.');
			return;
		}
        if (formData.level === 'Warning') {
            Alert.alert(
                "WARNUNG: Notfall-Benachrichtigung",
                "Diese Stufe sollte nur für echte Notfälle verwendet werden. Der Bildschirm des Empfängers wird blinken und ein Alarmton wird abgespielt. Sind Sie sicher?",
                [ { text: "Abbrechen", style: "cancel" }, { text: "Ja, Notfall senden", style: "destructive", onPress: sendNotification } ]
            );
        } else {
            sendNotification();
        }
    }

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.headerContainer}>
                <Icon name="bullhorn" size={24} style={styles.headerIcon}/>
			    <Text style={styles.title}>Benachrichtigungen senden</Text>
            </View>
            <Text style={styles.subtitle}>Erstellen und versenden Sie hier systemweite Benachrichtigungen an Benutzergruppen.</Text>
			
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Neue Benachrichtigung</Text>
				{error && <Text style={styles.errorText}>{error}</Text>}
				
                <View style={styles.formGroup}>
                    <Text style={styles.label}>Titel</Text>
                    <TextInput style={styles.input} value={formData.title} onChangeText={val => setFormData({...formData, title: val})} />
                </View>
                <View style={styles.formGroup}>
                    <Text style={styles.label}>Beschreibung</Text>
                    <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({...formData, description: val})} multiline />
                </View>

                <View style={styles.formGroup}>
                    <Text style={styles.label}>Stufe</Text>
                    <RadioButton.Group onValueChange={val => setFormData({...formData, level: val})} value={formData.level}>
                        <View style={styles.radioRow}><RadioButton value="Informational" /><Text>Info</Text></View>
                        <View style={styles.radioRow}><RadioButton value="Important" /><Text>Wichtig</Text></View>
                        <View style={styles.radioRow}><RadioButton value="Warning" /><Text style={{color: colors.danger}}>Warnung (Notfall)</Text></View>
                    </RadioButton.Group>
                </View>

                <View style={styles.formGroup}>
                    <Text style={styles.label}>Android Wichtigkeit</Text>
                    <Picker selectedValue={formData.androidImportance} onValueChange={val => setFormData({...formData, androidImportance: val})}>
                        <Picker.Item label="Standard" value="DEFAULT" />
                        <Picker.Item label="Hoch (Heads-up)" value="HIGH" />
                    </Picker>
                </View>

                <View style={styles.formGroup}>
                    <Text style={styles.label}>Zielgruppe</Text>
                    <Picker selectedValue={formData.targetType} onValueChange={val => setFormData({...formData, targetType: val, targetId: ''})}>
                        <Picker.Item label="Alle Benutzer" value="ALL" />
                        <Picker.Item label="Event-Teilnehmer" value="EVENT" />
                        <Picker.Item label="Meeting-Teilnehmer" value="MEETING" />
                    </Picker>
                </View>

                {formData.targetType === 'EVENT' && (
                    <View style={styles.formGroup}>
                        <Text style={styles.label}>Spezifisches Event</Text>
                        <Picker selectedValue={formData.targetId} onValueChange={val => setFormData({...formData, targetId: val})}>
                            <Picker.Item label="-- Bitte auswählen --" value="" />
                            {events?.map(e => <Picker.Item key={e.id} label={e.name} value={e.id} />)}
                        </Picker>
                    </View>
                )}
                {formData.targetType === 'MEETING' && (
                     <View style={styles.formGroup}>
                        <Text style={styles.label}>Spezifisches Meeting</Text>
                        <Picker selectedValue={formData.targetId} onValueChange={val => setFormData({...formData, targetId: val})}>
                            <Picker.Item label="-- Bitte auswählen --" value="" />
                            {meetings?.map(m => <Picker.Item key={m.id} label={`${m.parentCourseName}: ${m.name}`} value={m.id} />)}
                        </Picker>
                    </View>
                )}
				<TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSendPress} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Benachrichtigung senden</Text>}
				</TouchableOpacity>
			</View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1 },
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        radioRow: { flexDirection: 'row', alignItems: 'center' },
    });
};

export default AdminNotificationsPage;