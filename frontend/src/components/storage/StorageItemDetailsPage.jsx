import React, { useState, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, ActivityIndicator, Image, StyleSheet } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Lightbox from '../ui/Lightbox';
import DamageReportModal from '../storage/DamageReportModal';
import ReservationCalendar from '../storage/ReservationCalendar';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const StorageItemDetailsPage = () => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
	const route = useRoute();
	const { itemId } = route.params;
	const [activeTab, setActiveTab] = useState('history');
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [isReportModalOpen, setIsReportModalOpen] = useState(false);
	const { addToast } = useToast();

	const fetchItemCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const fetchHistoryCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/history`), [itemId]);
	const fetchReservationsCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/reservations`), [itemId]);
    const fetchRelationsCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/relations`), [itemId]);

	const { data: itemData, loading: itemLoading, error: itemError } = useApi(fetchItemCall);
	const { data: historyData, loading: historyLoading, error: historyError } = useApi(fetchHistoryCall);
	const { data: reservations, loading: reservationsLoading, error: reservationsError } = useApi(fetchReservationsCall);
    const { data: relations, loading: relationsLoading, error: relationsError } = useApi(fetchRelationsCall);


	const handleReportSuccess = () => {
		setIsReportModalOpen(false);
		addToast('Schadensmeldung erfolgreich übermittelt.', 'success');
	};

	const getImagePath = (path) => `${apiClient.getRootUrl()}/api/v1/public/files/images/${path.split('/').pop()}`;

	if (itemLoading || historyLoading || reservationsLoading || relationsLoading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (itemError) return <View style={styles.centered}><Text style={styles.errorText}>{itemError}</Text></View>;
	if (!itemData) return <View style={styles.centered}><Text>Artikel nicht gefunden.</Text></View>;

	const item = itemData;
	const { transactions, maintenance } = historyData || { transactions: [], maintenance: [] };

    const renderTabContent = () => {
        switch (activeTab) {
            case 'history':
                return <View style={styles.card}><Text style={styles.cardTitle}>Verlauf</Text>{transactions.map(entry => <Text key={entry.id}>{new Date(entry.transactionTimestamp).toLocaleDateString()} - {entry.quantityChange} von {entry.username}</Text>)}</View>;
            case 'maintenance':
                return <View style={styles.card}><Text style={styles.cardTitle}>Wartung</Text>{maintenance.map(entry => <Text key={entry.id}>{new Date(entry.logDate).toLocaleDateString()} - {entry.action} von {entry.username}</Text>)}</View>;
            case 'availability':
                return <View style={styles.card}><ReservationCalendar reservations={reservations} /></View>;
            case 'relations':
                return <View style={styles.card}><Text style={styles.cardTitle}>Zugehörige Artikel</Text>{relations.map(rel => <Text key={rel.id}>{rel.name}</Text>)}</View>;
            default:
                return null;
        }
    }

	return (
		<>
			<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
				<Text style={styles.title}>{item.name}</Text>
				{item.imagePath && (
					<TouchableOpacity onPress={() => setIsLightboxOpen(true)}>
						<Image source={{ uri: getImagePath(item.imagePath) }} style={{ width: '100%', height: 200, borderRadius: 8, marginBottom: 16 }} />
					</TouchableOpacity>
				)}
				<View style={styles.card}>
					<View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Verfügbar / Gesamt:</Text><Text>{item.availableQuantity} / {item.quantity}</Text></View>
					<View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Defekt:</Text><Text>{item.defectiveQuantity}</Text></View>
					<View style={styles.detailsListRow}><Text style={styles.detailsListLabel}>Ort:</Text><Text>{item.location}</Text></View>
				</View>
				<TouchableOpacity style={[styles.button, {backgroundColor: colors.warning, marginTop: 16}]} onPress={() => setIsReportModalOpen(true)}>
					<Text style={{color: '#000'}}>Schaden melden</Text>
				</TouchableOpacity>

                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.tabContainer}>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'history' && styles.activeTab]} onPress={() => setActiveTab('history')}><Text style={[styles.tabText, activeTab === 'history' && styles.activeTabText]}>Verlauf</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'maintenance' && styles.activeTab]} onPress={() => setActiveTab('maintenance')}><Text style={[styles.tabText, activeTab === 'maintenance' && styles.activeTabText]}>Wartung</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'availability' && styles.activeTab]} onPress={() => setActiveTab('availability')}><Text style={[styles.tabText, activeTab === 'availability' && styles.activeTabText]}>Verfügbarkeit</Text></TouchableOpacity>
                    <TouchableOpacity style={[styles.tabButton, activeTab === 'relations' && styles.activeTab]} onPress={() => setActiveTab('relations')}><Text style={[styles.tabText, activeTab === 'relations' && styles.activeTabText]}>Zubehör</Text></TouchableOpacity>
                </ScrollView>
                {renderTabContent()}
			</ScrollView>

			{isLightboxOpen && <Lightbox src={getImagePath(item.imagePath)} onClose={() => setIsLightboxOpen(false)} />}
			<DamageReportModal isOpen={isReportModalOpen} onClose={() => setIsReportModalOpen(false)} onSuccess={handleReportSuccess} item={item} />
		</>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        tabContainer: { flexDirection: 'row', paddingHorizontal: 16, paddingVertical: 8, borderBottomWidth: 1, borderColor: colors.border, marginTop: 16 },
        tabButton: { paddingVertical: 8, paddingHorizontal: 12, marginRight: 8, borderRadius: 8 },
        activeTab: { backgroundColor: colors.primary },
        tabText: { color: colors.text },
        activeTabText: { color: colors.white },
    });
};

export default StorageItemDetailsPage;