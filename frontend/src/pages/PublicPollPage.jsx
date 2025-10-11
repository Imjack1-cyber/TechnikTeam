import React, { useCallback } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { getCommonStyles } from '../styles/commonStyles';
import SchedulingPoll from '../components/polls/SchedulingPoll';
import InternalPoll from '../components/polls/InternalPoll';
import { useAuthStore } from '../store/authStore';

const PublicPollPage = () => {
    const route = useRoute();
    const { uuid } = route.params;
    const { user, isAuthenticated } = useAuthStore();
    
    // Determine the guest name if the user is not logged in.
    const guestName = !isAuthenticated ? (route.params?.guestName || null) : null;
    
    const apiCall = useCallback(() => apiClient.get(`/public/polls/by-uuid/${uuid}`), [uuid]);
    const { data: pollData, loading, error, reload } = useApi(apiCall);


    const theme = 'light';
    const styles = getCommonStyles(theme);

    if (loading) {
        return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
    }

    if (error || !pollData) {
        return (
            <View style={styles.centered}>
                <Text style={styles.title}>Umfrage nicht gefunden</Text>
                <Text style={styles.subtitle}>{error || 'Dieser Link ist ungültig oder die Umfrage wurde gelöscht.'}</Text>
            </View>
        );
    }
    
    // FIX: The poll type is on the root poll object, not nested in options.
    const pollType = pollData.type;
    
    switch (pollType) {
        case 'AVAILABILITY':
        case 'SCHEDULING':
            // Pass the entire pollData object which now contains 'poll', 'options', etc.
            return <SchedulingPoll pollData={{ poll: pollData, options: pollData.optionsMap, responders: [] }} reload={reload} />;
        case 'MULTIPLE_CHOICE':
        case 'WORD_CLOUD':
            return <InternalPoll pollData={{ poll: pollData }} reload={reload} />;
        default:
            return (
                <View style={styles.centered}>
                    <Text style={styles.title}>Unbekannter Umfragetyp</Text>
                    <Text style={styles.subtitle}>Diese Umfrage kann nicht angezeigt werden.</Text>
                </View>
            );
    }
};

export default PublicPollPage;