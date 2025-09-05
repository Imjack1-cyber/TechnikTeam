import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity, Linking, Platform } from 'react-native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import CalendarView from '../components/calendar/CalendarView'; // Now using the universal calendar view
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors } from '../styles/theme';

const CalendarPage = () => {
	const apiCall = useCallback(() => apiClient.get('/public/calendar/entries'), []);
	const { data: calendarEntries, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleSubscribe = () => {
		const icsUrl = `${apiClient.getRootUrl()}/api/v1/public/calendar.ics`;
		Linking.openURL(icsUrl).catch(err => console.error("Couldn't load page", err));
	};

	if (loading) {
		return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	}

	if (error) {
		return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	}

	return (
		<ScrollView style={styles.container}>
			<View style={styles.header}>
				<Icon name="calendar-alt" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Terminübersicht</Text>
			</View>
			<View style={styles.descriptionContainer}>
				<Text style={styles.description}>
					Übersicht aller anstehenden Veranstaltungen und Lehrgänge.
				</Text>
				<TouchableOpacity style={styles.subscribeButton} onPress={handleSubscribe}>
					<Icon name="rss" size={14} color={colors.white} />
					<Text style={styles.subscribeButtonText}>Kalender abonnieren</Text>
				</TouchableOpacity>
			</View>

			<CalendarView entries={calendarEntries || []} />
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        header: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        descriptionContainer: {
            paddingHorizontal: 16,
            marginBottom: 16,
        },
        description: {
            fontSize: 16,
            color: colors.textMuted,
            marginBottom: 12,
        },
        subscribeButton: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            backgroundColor: colors.success,
            paddingVertical: 8,
            paddingHorizontal: 12,
            borderRadius: 6,
            alignSelf: 'flex-start',
        },
        subscribeButtonText: {
            color: colors.white,
            fontWeight: '500',
        },
    });
};

export default CalendarPage;