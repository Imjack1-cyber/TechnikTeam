import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, ActivityIndicator, Linking } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import EventTrendChart from '../../components/admin/dashboard/EventTrendChart';
import UserActivityChart from '../../components/admin/reports/UserActivityChart';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const AdminReportsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/reports/dashboard'), []);
	const { data: reportData, loading, error } = useApi(apiCall, { subscribeTo: 'EVENT' });
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const getCsvLink = (reportType) => `${apiClient.getRootUrl()}/api/v1/reports/${reportType}?export=csv`;

	if (loading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (error) return <View style={styles.centered}><Text style={styles.errorText}>{error}</Text></View>;
	if (!reportData) return <View style={styles.centered}><Text>Keine Berichtsdaten verfügbar.</Text></View>;

	const { eventTrend, userActivity, totalInventoryValue } = reportData;

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
			<View style={styles.headerContainer}>
                <Icon name="chart-pie" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Berichte & Analysen</Text>
            </View>
			<Text style={styles.subtitle}>Hier finden Sie zusammengefasste Daten und Analysen über die Anwendungsnutzung.</Text>

			<View style={styles.card}>
				<Text style={styles.cardTitle}>Event-Trend (Letzte 12 Monate)</Text>
				<EventTrendChart trendData={eventTrend} />
			</View>

			<View style={styles.card}>
				<Text style={styles.cardTitle}>Top 10 Aktivste Benutzer</Text>
				<UserActivityChart activityData={userActivity} />
			</View>

			<View style={styles.card}>
				<Text style={styles.cardTitle}>Sonstige Berichte & Exporte</Text>
                <View style={styles.exportRow}>
                    <Text style={styles.exportLabel}>Teilnahme-Zusammenfassung</Text>
                    <TouchableOpacity style={styles.exportButton} onPress={() => Linking.openURL(getCsvLink('event-participation'))}>
                        <Icon name="file-csv" size={16} color={colors.white} />
                        <Text style={styles.buttonText}>CSV</Text>
                    </TouchableOpacity>
                </View>
                <View style={styles.exportRow}>
                    <Text style={styles.exportLabel}>Nutzungsfrequenz (Material)</Text>
                    <TouchableOpacity style={styles.exportButton} onPress={() => Linking.openURL(getCsvLink('inventory-usage'))}>
                         <Icon name="file-csv" size={16} color={colors.white} />
                        <Text style={styles.buttonText}>CSV</Text>
                    </TouchableOpacity>
                </View>
                <View style={styles.exportRow}>
                    <Text style={styles.exportLabel}>Vollständige Benutzeraktivität</Text>
                    <TouchableOpacity style={styles.exportButton} onPress={() => Linking.openURL(getCsvLink('user-activity'))}>
                         <Icon name="file-csv" size={16} color={colors.white} />
                        <Text style={styles.buttonText}>CSV</Text>
                    </TouchableOpacity>
                </View>
                <View style={styles.exportRow}>
                    <Text style={styles.exportLabel}>Gesamtwert des Lagers</Text>
                    <Text style={styles.inventoryValue}>
                        {new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' }).format(totalInventoryValue)}
                    </Text>
                </View>
			</View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        exportRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        exportLabel: { fontSize: typography.body, color: colors.text },
        exportButton: { flexDirection: 'row', gap: 8, backgroundColor: colors.success, paddingVertical: 8, paddingHorizontal: 12, borderRadius: 6 },
        inventoryValue: { fontWeight: 'bold', fontSize: typography.body, color: colors.text },
    });
};

export default AdminReportsPage;