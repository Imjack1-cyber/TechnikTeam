import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import Icon from 'react-native-vector-icons/FontAwesome5';

const DashboardPage = () => {
	const { user, layout } = useAuthStore(state => ({
		user: state.user,
		layout: state.layout,
	}));
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/public/dashboard'), []);
	const { data: dashboardData, loading, error } = useApi(apiCall);

	const widgets = layout.dashboardWidgets || {
		recommendedEvents: true,
		assignedEvents: true,
		openTasks: true,
		upcomingEvents: true,
		recentConversations: true,
		upcomingMeetings: true,
		lowStockItems: false,
	};
    
    if (loading) {
		return (
			<View style={styles.container}>
				<Text style={styles.title}>Willkommen zurück, {user?.username}!</Text>
                <ActivityIndicator size="large" color="#007bff" />
			</View>
		);
	}

	if (error) {
		return <View style={styles.container}><Text style={styles.errorText}>{error}</Text></View>;
	}

	return (
		<ScrollView style={styles.container}>
			<Text style={styles.title}>Willkommen zurück, {user?.username}!</Text>
			
            {widgets.recommendedEvents && dashboardData?.recommendedEvents?.length > 0 && (
                <View style={[styles.card, styles.recommendedCard]}>
                    <Text style={styles.cardTitle}><Icon name="star" color="#ffc107" /> Für Dich empfohlen</Text>
                    <Text style={styles.cardDescription}>Hier sind einige Events, für die du qualifiziert bist:</Text>
                    {dashboardData.recommendedEvents.map(event => (
                        <TouchableOpacity key={event.id} style={styles.listItem} onPress={() => navigation.navigate('EventDetails', { eventId: event.id })}>
                            <Text style={styles.listItemText}>{event.name}</Text>
                            <Text style={styles.listItemSubText}>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text>
                        </TouchableOpacity>
                    ))}
                </View>
            )}

            {widgets.assignedEvents && (
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Meine nächsten Einsätze</Text>
                    {dashboardData?.assignedEvents?.length > 0 ? (
                         dashboardData.assignedEvents.map(event => (
                            <TouchableOpacity key={event.id} style={styles.listItem} onPress={() => navigation.navigate('EventDetails', { eventId: event.id })}>
                                <Text style={styles.listItemText}>{event.name}</Text>
                                <Text style={styles.listItemSubText}>{new Date(event.eventDateTime).toLocaleString('de-DE')}</Text>
                            </TouchableOpacity>
                        ))
                    ) : (
                        <Text>Du bist derzeit für keine kommenden Events fest eingeteilt.</Text>
                    )}
                </View>
            )}

             {/* Other widgets would follow a similar pattern */}

		</ScrollView>
	);
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f8f9fa', // bg-color
        padding: 16,
    },
    title: {
        fontSize: 24,
        fontWeight: '700',
        color: '#002B5B', // heading-color
        marginBottom: 16,
    },
    card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginBottom: 16,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
    recommendedCard: {
        backgroundColor: 'rgba(0, 123, 255, 0.1)', // primary-color-light
    },
    cardTitle: {
		fontSize: 18,
		fontWeight: '600',
		color: '#002B5B',
        marginBottom: 8,
    },
    cardDescription: {
        marginBottom: 8,
        color: '#212529'
    },
    listItem: {
        paddingVertical: 12,
        borderBottomWidth: 1,
        borderBottomColor: '#dee2e6',
    },
    listItemText: {
        fontSize: 16,
        color: '#007bff' // primary-color
    },
    listItemSubText: {
        fontSize: 12,
        color: '#6c757d'
    },
    errorText: {
		color: '#dc3545',
		textAlign: 'center',
	}
});


export default DashboardPage;