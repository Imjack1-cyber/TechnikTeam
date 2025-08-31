import React, { useState, useCallback } from 'react';
import { View, Text, ScrollView, TouchableOpacity, ActivityIndicator, Image, StyleSheet } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import Lightbox from '../components/ui/Lightbox';
import DamageReportModal from '../components/storage/DamageReportModal';
import ReservationCalendar from '../components/storage/ReservationCalendar';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';

const StorageItemDetailsPage = () => {
	const route = useRoute();
	const { itemId } = route.params;
	const [activeTab, setActiveTab] = useState('history');
	const [isLightboxOpen, setIsLightboxOpen] = useState(false);
	const [isReportModalOpen, setIsReportModalOpen] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const fetchItemCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const fetchHistoryCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/history`), [itemId]);
	const fetchReservationsCall = useCallback(() => apiClient.get(`/public/storage/${itemId}/reservations`), [itemId]);

	const { data: itemData, loading: itemLoading, error: itemError } = useApi(fetchItemCall);
	const { data: historyData, loading: historyLoading, error: historyError } = useApi(fetchHistoryCall);
	const { data: reservations, loading: reservationsLoading, error: reservationsError } = useApi(fetchReservationsCall);

	const handleReportSuccess = () => {
		setIsReportModalOpen(false);
		addToast('Schadensmeldung erfolgreich übermittelt.', 'success');
	};

	const getImagePath = (path) => `http://10.0.2.2:8081/TechnikTeam/api/v1/public/files/images/${path.split('/').pop()}`;

	if (itemLoading || historyLoading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (itemError) return <View style={styles.centered}><Text style={styles.errorText}>{itemError}</Text></View>;
	if (!itemData) return <View style={styles.centered}><Text>Artikel nicht gefunden.</Text></View>;

	const item = itemData;
	const { transactions, maintenance } = historyData || { transactions: [], maintenance: [] };

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
				<TouchableOpacity style={[styles.button, {backgroundColor: getThemeColors(theme).warning, marginTop: 16}]} onPress={() => setIsReportModalOpen(true)}>
					<Text style={{color: '#000'}}>Schaden melden</Text>
				</TouchableOpacity>

                {/* Simplified Tabs for Native */}
                <View style={styles.card}>
                    <Text style={styles.cardTitle}>Verlauf</Text>
                    {transactions.map(entry => <Text key={entry.id}>{new Date(entry.transactionTimestamp).toLocaleDateString()} - {entry.quantityChange} von {entry.username}</Text>)}
                </View>
                 <View style={styles.card}>
                    <Text style={styles.cardTitle}>Wartung</Text>
                    {maintenance.map(entry => <Text key={entry.id}>{new Date(entry.logDate).toLocaleDateString()} - {entry.action} von {entry.username}</Text>)}
                </View>
                 <View style={styles.card}>
                    <ReservationCalendar reservations={reservations} />
                </View>
			</ScrollView>

			{isLightboxOpen && <Lightbox src={getImagePath(item.imagePath)} onClose={() => setIsLightboxOpen(false)} />}
			<DamageReportModal isOpen={isReportModalOpen} onClose={() => setIsReportModalOpen(false)} onSuccess={handleReportSuccess} item={item} />
		</>
	);
};

export default StorageItemDetailsPage;