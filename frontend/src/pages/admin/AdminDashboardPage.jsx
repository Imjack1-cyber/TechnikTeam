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
		const handlePress = () => {
			if (type === 'event') navigation.navigate('Event Management', { screen: 'AdminEvents' });
			if (type === 'item') navigation.navigate('Lager & Material', { screen: 'AdminStorage' });
            if (type === 'log') navigation.navigate('Berichte', { screen: 'AdminLog' });
		};

		return (
			<TouchableOpacity style={styles.detailsListRow} key={item.id} onPress={handlePress}>
				<Text style={styles.detailsListLabel} numberOfLines={1}>{item.name || `${item.adminUsername}: ${item.actionType}`}</Text>
				<Text style={styles.detailsListValue}>{type === 'item' ? `${item.availableQuantity} / ${item.maxQuantity}` : new Date(item.eventDateTime || item.actionTimestamp).toLocaleDateString('de-DE')}</Text>
			</TouchableOpacity>
		);
	};

	const renderWidgetContent = (widgetData, type, emptyMessage) => {
		if (!widgetData || widgetData.length === 0) {
			return <Text style={styles.bodyText}>{emptyMessage}</Text>;
		}
		return <View style={styles.detailsList}>{widgetData.map(item => renderWidgetItem(item, type))}</View>;
	};

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<Text style={styles.title}>Willkommen, {user?.username}!</Text>
			<Text style={styles.subtitle}>Wählen Sie eine Option aus der Navigation oder nutzen Sie den Schnellzugriff.</Text>

			<Widget icon="fa-calendar-alt" title="Anstehende Events" linkTo="Event Management" linkText="Alle Events anzeigen">
				{renderWidgetContent(dashboardData?.upcomingEvents, 'event', 'Keine anstehenden Events.')}
			</Widget>
			<Widget icon="fa-box-open" title="Niedriger Lagerbestand" linkTo="Lager & Material" linkText="Lager verwalten">
				{renderWidgetContent(dashboardData?.lowStockItems, 'item', 'Alle Artikel sind ausreichend vorhanden.')}
			</Widget>
			<Widget icon="fa-history" title="Letzte Aktionen" linkTo="Berichte" linkText="Alle Logs anzeigen">
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