import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import VenueModal from '../../components/admin/venues/VenueModal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const AdminVenuesPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/venues'), []);
	const { data: venues, loading, error, reload } = useApi(apiCall, { subscribeTo: 'VENUE' });
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingVenue, setEditingVenue] = useState(null);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (venue = null) => {
		setEditingVenue(venue);
		setIsModalOpen(true);
	};
    
    const handleSuccess = () => {
        setIsModalOpen(false);
        setEditingVenue(null);
        reload();
    };

	const handleDelete = (venue) => {
        Alert.alert(`Ort "${venue.name}" löschen?`, "", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/venues/${venue.id}`);
                    if (result.success) {
                        addToast('Ort erfolgreich gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }}
        ]);
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.name}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Adresse:</Text>
                <Text style={styles.value} numberOfLines={1}>{item.address || '-'}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Karte:</Text>
                <Text style={styles.value}>{item.mapImagePath ? 'Ja' : 'Nein'}</Text>
            </View>
             <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(item)}><Text style={styles.dangerOutlineButtonText}>Löschen</Text></TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={[styles.container, {overflow: 'auto'}]}>
            <View style={styles.headerContainer}>
                <Icon name="map-marked-alt" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Veranstaltungsorte</Text>
            </View>
			<Text style={styles.subtitle}>Verwalten Sie hier die Orte und die zugehörigen Raumpläne.</Text>
            <TouchableOpacity style={[styles.button, styles.successButton, { alignSelf: 'flex-start' }]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neuer Ort</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={venues}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
            />

			{isModalOpen && <VenueModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={handleSuccess} venue={editingVenue} />}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, padding: spacing.md },
        headerContainer: { flexDirection: 'row', alignItems: 'center', marginBottom: spacing.sm },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
    });
};

export default AdminVenuesPage;