import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import MarkdownDisplay from 'react-native-markdown-display';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';

const MeetingDetailsPage = () => {
	const route = useRoute();
	const { meetingId } = route.params;
	const apiCall = useCallback(() => apiClient.get(`/public/meetings/${meetingId}`), [meetingId]);
	const { data, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

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
                        <TouchableOpacity key={att.id} style={styles.detailsListRow}>
                            <Text style={{color: getThemeColors(theme).primary}}>{att.filename}</Text>
                        </TouchableOpacity>
                    ))
                ) : <Text>Keine Anhänge verfügbar.</Text>}
            </View>
		</ScrollView>
	);
};

export default MeetingDetailsPage;
