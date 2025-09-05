import React, { useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, Platform } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';

const AdminRequestsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/requests/pending'), []);
	const { data: requests, loading, error, reload } = useApi(apiCall);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

    const performAction = async (requestId, action) => {
        try {
            const result = await apiClient.post(`/requests/${requestId}/${action}`);
            if (result.success) {
                addToast(`Antrag erfolgreich ${action === 'approve' ? 'genehmigt' : 'abgelehnt'}.`, 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
    };

	const handleAction = (requestId, action) => {
		const confirmationText = action === 'approve' ? 'Änderungen wirklich übernehmen?' : 'Antrag wirklich ablehnen?';
        const title = 'Aktion bestätigen';

        if (Platform.OS === 'web') {
            if (window.confirm(`${title}\n\n${confirmationText}`)) {
                performAction(requestId, action);
            }
        } else {
            Alert.alert(title, confirmationText, [
                { text: 'Abbrechen', style: 'cancel' },
                { text: action === 'approve' ? 'Genehmigen' : 'Ablehnen', style: 'default', onPress: () => performAction(requestId, action) }
            ]);
        }
	};

	const renderChanges = (changesJson) => {
		try {
			const changes = JSON.parse(changesJson);
			return (
				<View>
					{Object.entries(changes).map(([key, value]) => (
						<Text key={key} style={styles.changeText}><Text style={{fontWeight: 'bold'}}>{key}:</Text> {value}</Text>
					))}
				</View>
			);
		} catch (e) {
			return <Text style={styles.errorText}>Fehler beim Parsen der Änderungen.</Text>;
		}
	};
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>Antrag von {item.username}</Text>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Beantragt am:</Text>
                <Text style={styles.value}>{new Date(item.requestedAt).toLocaleString('de-DE')}</Text>
            </View>
            <View style={{marginTop: 8}}>
                <Text style={styles.label}>Änderungen:</Text>
                {renderChanges(item.requestedChanges)}
            </View>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleAction(item.id, 'approve')}>
                    <Text style={styles.buttonText}>Genehmigen</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => handleAction(item.id, 'deny')}>
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
			<Text style={styles.subtitle}>Hier sehen Sie alle von Benutzern beantragten Änderungen an Stammdaten.</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}

			<FlatList
                data={requests}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Keine offenen Anträge vorhanden.</Text></View>}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
        changeText: { fontSize: typography.body, color: colors.text, marginLeft: 8 }
    });
};

export default AdminRequestsPage;