import React, { useCallback } from 'react';
import { ScrollView, View, Text, ActivityIndicator, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import Widget from '../../components/admin/dashboard/Widget';
import EventTrendChart from '../../components/admin/dashboard/EventTrendChart';
import { getCommonStyles } from '../../styles/commonStyles';

const AdminDashboardPage = () => {
	const { user } = useAuthStore();
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/admin/dashboard'), []);
	const { data: dashboardData, loading, error } = useApi(apiCall);

    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const renderWidgetItem = (item, type) => {
		switch (type) {
			case 'event':
				return (
					<TouchableOpacity style={styles.detailsListRow} key={item.id} onPress={() => navigation.navigate('EventDetails', { eventId: item.id })}>
						<Text style={styles.detailsListLabel}>{item.name}</Text>
						<Text style={styles.detailsListValue}>{new Date(item.eventDateTime).toLocaleDateString('de-DE')}</Text>
					</TouchableOpacity>
				);
			case 'item':
				return (
					<TouchableOpacity style={styles.detailsListRow} key={item.id} onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.id })}>
						<Text style={styles.detailsListLabel}>{item.name}</Text>
						<Text style={styles.detailsListValue}>{item.availableQuantity} / {item.maxQuantity}</Text>
					</TouchableOpacity>
				);
			case 'log':
				return (
					<View style={styles.detailsListRow} key={item.id}>
						<Text style={[styles.detailsListLabel, {flex: 1}]} numberOfLines={1}>
                            <Text style={{fontWeight: 'bold'}}>{item.adminUsername}</Text>: {item.actionType}
                        </Text>
						<Text style={styles.detailsListValue}>{new Date(item.actionTimestamp).toLocaleDateString('de-DE')}</Text>
					</View>
				);
			default:
				return null;
		}
	};

	const renderWidgetContent = (widgetData, type, emptyMessage) => {
		if (!widgetData || widgetData.length === 0) {
			return <Text style={styles.bodyText}>{emptyMessage}</Text>;
		}
		return <View style={styles.detailsList}>{widgetData.map(item => renderWidgetItem(item, type))}</View>;
	};

	if (loading) {
		return <View style={styles.centered}><ActivityIndicator size="large" /></View>
	}

	if (error) {
		return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	}

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>Willkommen im Admin-Bereich, {user?.username}!</Text>
			<Text style={styles.subtitle}>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option aus der Navigation oder nutzen Sie den Schnellzugriff.</Text>

			<Widget icon="fa-calendar-alt" title="Anstehende Events" linkTo="AdminEvents" linkText="Alle Events anzeigen">
				{renderWidgetContent(dashboardData?.upcomingEvents, 'event', 'Keine anstehenden Events.')}
			</Widget>

			<Widget icon="fa-box-open" title="Niedriger Lagerbestand" linkTo="AdminStorage" linkText="Lager verwalten">
				{renderWidgetContent(dashboardData?.lowStockItems, 'item', 'Alle Artikel sind ausreichend vorhanden.')}
			</Widget>

			<Widget icon="fa-history" title="Letzte Aktionen" linkTo="AdminLog" linkText="Alle Logs anzeigen">
				{renderWidgetContent(dashboardData?.recentLogs, 'log', 'Keine Aktionen protokolliert.')}
			</Widget>
            
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Event-Trend (Letzte 12 Monate)</Text>
                <EventTrendChart trendData={dashboardData?.eventTrendData} />
            </View>
		</ScrollView>
	);
};

export default AdminDashboardPage;