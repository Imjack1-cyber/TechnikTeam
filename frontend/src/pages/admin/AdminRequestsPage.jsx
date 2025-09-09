import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Modal from '../../components/ui/Modal';

const RequestDetailsModal = ({ isOpen, onClose, request }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    if (!request) return null;
    
    let changes = {};
    try {
        changes = JSON.parse(request.requestedChanges);
    } catch (e) {}

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={`Antrag #${request.id} von ${request.username}`}>
             <View>
                {Object.entries(changes).map(([key, value]) => (
                    <View key={key} style={styles.detailRow}>
                        <Text style={styles.label}>{key}:</Text>
                        <Text style={styles.value}>{String(value)}</Text>
                    </View>
                ))}
            </View>
        </Modal>
    );
};

const AdminRequestsPage = () => {
    const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/requests/pending'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const [selectedRequest, setSelectedRequest] = useState(null);
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const handleAction = async (action, request) => {
		try {
			const result = await apiClient.post(`/requests/${request.id}/${action}`);
			if (result.success) {
				addToast(`Antrag #${request.id} erfolgreich ${action === 'approve' ? 'genehmigt' : 'abgelehnt'}.`, 'success');
				reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <TouchableOpacity onPress={() => setSelectedRequest(item)}>
                <Text style={styles.cardTitle}>Antrag von {item.username}</Text>
            </TouchableOpacity>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Datum:</Text>
                <Text style={styles.value}>{new Date(item.requestedAt).toLocaleString('de-DE')}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleAction('approve', item)}>
                    <Text style={styles.buttonText}>Genehmigen</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => handleAction('deny', item)}>
                    <Text style={styles.buttonText}>Ablehnen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="inbox" size={24} style={styles.headerIcon} />
                <Text style={styles.title}>Offene Anträge</Text>
            </View>
            <Text style={styles.subtitle}>Hier sehen Sie alle offenen Anträge auf Profiländerungen von Benutzern.</Text>

            {loading && <ActivityIndicator size="large" />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={requests}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={
                    <View style={styles.card}>
                        <Text>Keine offenen Anträge vorhanden.</Text>
                    </View>
                }
            />

            {selectedRequest && <RequestDetailsModal isOpen={!!selectedRequest} onClose={() => setSelectedRequest(null)} request={selectedRequest} />}
        </View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
    });
};

export default AdminRequestsPage;