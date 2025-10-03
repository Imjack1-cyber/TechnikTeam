import React, { useCallback } from 'react';
import { ScrollView, View, Text, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import Widget from '../components/admin/dashboard/Widget';
import { getCommonStyles } from '../styles/commonStyles';
import { navigateFromUrl } from '../router/navigationHelper';

const DashboardPage = () => {
	const { user, layout } = useAuthStore(state => ({
		user: state.user,
		layout: state.layout,
	}));
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/public/dashboard'), []);
	const { data: dashboardData, loading, error } = useApi(apiCall);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const renderItem = (item, type) => {
        const handlePress = () => {
            if (item.url) {
                navigateFromUrl(item.url);
            } else {
                switch(type) {
                    case 'event':
                        navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: item.id } });
                        break;
                    case 'task':
                        if (item.eventId) {
                            navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: item.eventId } });
                        }
                        break;
                    case 'meeting':
                        navigation.navigate('MeetingDetails', { meetingId: item.id });
                        break;
                    case 'conversation':
                        navigation.navigate('Chat', { screen: 'MessageView', params: { conversationId: item.id } });
                        break;
                    case 'item': // For low stock items
                        navigation.navigate('StorageItemDetails', { itemId: item.id });
                        break;
                    default:
                        console.warn(`Unhandled navigation type in Dashboard: ${type}`);
                }
            }
        };
        return (
            <TouchableOpacity style={styles.detailsListRow} key={item.id} onPress={handlePress}>
                <Text style={styles.detailsListLabel} numberOfLines={1}>{item.name || item.description || (item.groupChat ? item.name : item.otherParticipantUsername)}</Text>
                <Text style={styles.detailsListValue}>{item.eventDateTime ? new Date(item.eventDateTime).toLocaleDateString('de-DE') : ''}</Text>
            </TouchableOpacity>
        );
    };

	const renderWidgetContent = (data, type, emptyMessage) => {
		if (!data || data.length === 0) return <Text style={styles.bodyText}>{emptyMessage}</Text>;
		return <View>{data.map(item => renderItem(item, type))}</View>;
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>Willkommen, {user?.username}!</Text>
			
            {layout.dashboardWidgets.recommendedEvents && <Widget icon="fa-star" title="Für Dich empfohlen">{renderWidgetContent(dashboardData.recommendedEvents, 'event', 'Keine Empfehlungen.')}</Widget>}
            {layout.dashboardWidgets.assignedEvents && <Widget icon="fa-calendar-check" title="Meine nächsten Einsätze">{renderWidgetContent(dashboardData.assignedEvents, 'event', 'Keine Einsätze.')}</Widget>}
            {layout.dashboardWidgets.openTasks && <Widget icon="fa-tasks" title="Meine offenen Aufgaben">{renderWidgetContent(dashboardData.openTasks, 'task', 'Keine offenen Aufgaben.')}</Widget>}
            {layout.dashboardWidgets.upcomingMeetings && <Widget icon="fa-graduation-cap" title="Meine nächsten Lehrgänge">{renderWidgetContent(dashboardData.upcomingMeetings, 'meeting', 'Keine Lehrgänge.')}</Widget>}
            {layout.dashboardWidgets.recentConversations && <Widget icon="fa-comments" title="Letzte Gespräche">{renderWidgetContent(dashboardData.recentConversations, 'conversation', 'Keine Gespräche.')}</Widget>}
            {layout.dashboardWidgets.upcomingEvents && <Widget icon="fa-calendar-alt" title="Weitere anstehende Events">{renderWidgetContent(dashboardData.upcomingEvents, 'event', 'Keine weiteren Events.')}</Widget>}
            {layout.dashboardWidgets.lowStockItems && <Widget icon="fa-box-open" title="Niedriger Lagerbestand">{renderWidgetContent(dashboardData.lowStockItems, 'item', 'Alle Artikel ausreichend vorhanden.')}</Widget>}
		</ScrollView>
	);
};

export default DashboardPage;