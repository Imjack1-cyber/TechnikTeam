import React, { useCallback } from 'react';
import { View, Text, ActivityIndicator, ScrollView } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import DailyVoteSummary from '../../components/admin/availability/DailyVoteSummary';
import { getThemeColors, spacing } from '../../styles/theme';
import InternalPoll from '../../components/polls/InternalPoll'; // Reusing for results display

const AdminPollResultsPage = () => {
    const route = useRoute();
    const { pollId } = route.params;
    
    // Admin results might need different data, but for now, we can reuse the public endpoints for simplicity
    const apiCall = useCallback(() => apiClient.get(`/admin/polls/${pollId}/results`), [pollId]);
    const { data, loading, error, reload } = useApi(apiCall, { subscribeTo: 'POLL' });
    
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    if (loading) {
        return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
    }

    if (error) {
        return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
    }

    if (!data || !data.poll) {
        return <View style={styles.centered}><Text>Umfragedaten nicht gefunden.</Text></View>;
    }

    const { poll, analysis, adminAvailableDays } = data;

    // Conditionally render the correct results component based on poll type
    switch (poll.type) {
        case 'SCHEDULING':
        case 'AVAILABILITY':
            return (
                <ScrollView style={styles.container}>
                    <View style={{padding: spacing.md}}>
                        <Text style={styles.title}>{poll.question}</Text>
                        <Text style={styles.subtitle}>{poll.description}</Text>
                    </View>
                    <DailyVoteSummary analysis={analysis} adminAvailableDays={adminAvailableDays} />
                </ScrollView>
            );
        case 'MULTIPLE_CHOICE':
        case 'WORD_CLOUD':
            // The InternalPoll component already has logic to show results if hasVoted is true.
            // We can reuse it by passing a modified poll object.
            const pollDataForDisplay = {
                poll: { ...poll, hasVoted: true }, // Force results view
            };
            return <InternalPoll pollData={pollDataForDisplay} reload={reload} />;
        default:
            return <View style={styles.centered}><Text>Unbekannter Umfragetyp</Text></View>;
    }
};

export default AdminPollResultsPage;