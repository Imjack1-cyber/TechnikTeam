import React, { useCallback } from 'react';
import { View, Text, ActivityIndicator, ScrollView } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import DailyVoteSummary from '../../components/admin/availability/DailyVoteSummary';
import { getThemeColors, spacing } from '../../styles/theme';

const AdminAvailabilityPollDetailsPage = () => {
    const route = useRoute();
    const { pollId } = route.params;
    const apiCall = useCallback(() => apiClient.get(`/admin/availability/${pollId}`), [pollId]);
    const { data, loading, error } = useApi(apiCall);
    
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    if (loading) {
        return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
    }

    if (error) {
        return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
    }

    if (!data) {
        return <View style={styles.centered}><Text>Umfragedaten nicht gefunden.</Text></View>;
    }

    const { poll, responses, analysis, adminAvailableDays } = data;

    return (
        <ScrollView style={styles.container}>
            <View style={{padding: spacing.md}}>
                <Text style={styles.title}>{poll.title}</Text>
                <Text style={styles.subtitle}>{poll.description}</Text>
            </View>

            <DailyVoteSummary analysis={analysis} adminAvailableDays={adminAvailableDays} />
        </ScrollView>
    );
};

export default AdminAvailabilityPollDetailsPage;