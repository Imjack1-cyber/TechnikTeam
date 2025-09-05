import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Platform } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { useToast } from '../context/ToastContext';
import { getToken } from '../lib/storage';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import { getThemeColors } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const MeetingDetailsPage = () => {
	const route = useRoute();
	const { meetingId } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/meetings/${meetingId}`), [meetingId]);
	const { data, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const { addToast } = useToast();

    const handleDownloadAttachment = async (attachment) => {
        addToast('Download wird gestartet...', 'info');
        const downloadUrl = `${apiClient.getBaseUrl()}/api/v1/public/files/download/${attachment.id}`;
        const token = await getToken();
    
        try {
            if (Platform.OS === 'web') {
                const response = await fetch(downloadUrl, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                if (!response.ok) throw new Error('Fehler beim Herunterladen der Datei.');
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = attachment.filename;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);
                addToast('Download abgeschlossen!', 'success');
            } else {
                // Native logic
                const fileUri = FileSystem.documentDirectory + attachment.filename.replace(/[^a-zA-Z0-9.\-_]/g, '_');
                const { uri } = await FileSystem.downloadAsync(
                    downloadUrl,
                    fileUri,
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                addToast('Download abgeschlossen!', 'success');
                if (await Sharing.isAvailableAsync()) {
                    await Sharing.shareAsync(uri, { dialogTitle: attachment.filename });
                } else {
                    addToast('Datei heruntergeladen. Siehe Download-Ordner.', 'info');
                }
            }
        } catch (error) {
            console.error('Download error:', error);
            addToast('Download fehlgeschlagen.', 'error');
        }
    };

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!data) return <View style={styles.centered}><Text>Meeting nicht gefunden.</Text></View>;

	const { meeting, attachments } = data;

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>{meeting.parentCourseName}</Text>
			<Text style={[styles.subtitle, {marginTop: -16}]}>{meeting.name}</Text>

			<View style={styles.card}>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Datum:</Text><Text>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</Text></View>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Ort:</Text><Text>{meeting.location || 'N/A'}</Text></View>
                <View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Leitung:</Text><Text>{meeting.leaderUsername || 'N/A'}</Text></View>
				<Text style={[styles.cardTitle, {marginTop: 16}]}>Beschreibung</Text>
				<MarkdownDisplay>
					{meeting.description || 'Keine Beschreibung vorhanden.'}
				</MarkdownDisplay>
			</View>

            <View style={styles.card}>
                <Text style={styles.cardTitle}>Anhänge</Text>
                {attachments?.length > 0 ? (
                    attachments.map(att => (
                        <TouchableOpacity key={att.id} style={[styles.detailsListRow, {justifyContent: 'flex-start', gap: 8}]} onPress={() => handleDownloadAttachment(att)}>
                            <Icon name="download" size={16} color={getThemeColors(theme).primary} />
                            <Text style={{color: getThemeColors(theme).primary}}>{att.filename}</Text>
                        </TouchableOpacity>
                    ))
                ) : <Text>Keine Anhänge verfügbar.</Text>}
            </View>
		</ScrollView>
	);
};

export default MeetingDetailsPage;