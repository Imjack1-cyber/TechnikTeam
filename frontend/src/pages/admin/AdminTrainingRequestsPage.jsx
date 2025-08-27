import React, { useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';

const AdminTrainingRequestsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/admin/training-requests'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

	const handleDelete = (request) => {
        Alert.alert(`Anfrage löschen?`, `Anfrage für "${request.topic}" wirklich löschen?`, [
			{ text: 'Abbrechen', style: 'cancel' },
			{ text: 'Löschen', style: 'destructive', onPress: async () => {
				try {
					const result = await apiClient.delete(`/admin/training-requests/${request.id}`);
					if (result.success) {
						addToast('Anfrage gelöscht', 'success');
						reload();
					} else { throw new Error(result.message); }
				} catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
			}},
		]);
	};

    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>{item.topic}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Angefragt von:</Text>
                <Text style={styles.value}>{item.requesterUsername}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Datum:</Text>
                <Text style={styles.value}>{new Date(item.createdAt).toLocaleDateString('de-DE')}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Interessenten:</Text>
                <Text style={styles.value}>{item.interestCount}</Text>
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(item)}>
                    <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="question-circle" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Lehrgangsanfragen</Text>
            </View>
			<Text style={styles.subtitle}>Hier sehen Sie alle von Benutzern eingereichten Wünsche für neue Lehrgänge.</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={requests}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 16 }
    });
};

export default AdminTrainingRequestsPage;