import React, { useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography } from '../../styles/theme';

const AdminLogPage = () => {
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/logs'), []);
	const { data: logs, loading, error, reload } = useApi(apiCall);
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };

	const canRevoke = isAdmin || user?.permissions.includes('LOG_REVOKE');

	const handleRevoke = (log) => {
        Alert.alert(
            `Aktion widerrufen?`,
            `Möchten Sie die Aktion "${log.actionType}" (ID: ${log.id}) wirklich widerrufen? Dies führt die entsprechende Gegenaktion aus.`,
            [
                { text: 'Abbrechen', style: 'cancel' },
                { text: 'Widerrufen', style: 'destructive', onPress: async () => {
                    try {
                        const result = await apiClient.post(`/logs/${log.id}/revoke`);
                        if (result.success) {
                            addToast('Aktion erfolgreich widerrufen.', 'success');
                            reload();
                        } else { throw new Error(result.message); }
                    } catch (err) { addToast(`Widerrufen fehlgeschlagen: ${err.message}`, 'error'); }
                }}
            ]
        );
	};

    const renderItem = ({ item: log }) => {
        let context = {};
        try {
            if (log.context) context = JSON.parse(log.context);
        } catch (e) { /* ignore */ }
        const isRevocable = canRevoke && log.status === 'ACTIVE' && context.revocable === true;

        return (
            <View style={styles.card}>
                <View style={styles.cardHeader}>
                    <Text style={styles.cardTitle}>{log.actionType}</Text>
                    {log.status === 'REVOKED'
                        ? <Text style={[styles.badge, styles.infoBadge]}>Widerrufen</Text>
                        : <Text style={[styles.badge, styles.okBadge]}>Aktiv</Text>
                    }
                </View>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Wer:</Text>
                    <Text style={styles.value} onPress={() => log.adminUserId && navigation.navigate('UserProfile', { userId: log.adminUserId })}>{log.adminUsername}</Text>
                </View>
                <View style={styles.detailRow}>
                    <Text style={styles.label}>Wann:</Text>
                    <Text style={styles.value}>{new Date(log.actionTimestamp).toLocaleString('de-DE')}</Text>
                </View>
                <Text style={styles.detailsText}>{log.details}</Text>
                {canRevoke && (
                    <View style={styles.cardActions}>
                        <TouchableOpacity style={[styles.button, {backgroundColor: getThemeColors(theme).warning}, isRevocable ? {} : styles.disabledButton]} onPress={() => handleRevoke(log)} disabled={!isRevocable}>
                            <Text style={{color: '#000'}}>Widerrufen</Text>
                        </TouchableOpacity>
                    </View>
                )}
            </View>
        );
    };

	return (
		<View style={styles.container}>
			<View style={styles.headerContainer}>
                <Icon name="clipboard-list" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Admin Aktions-Protokoll</Text>
			</View>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={logs}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Keine Log-Einträge gefunden.</Text></View>}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        headerContainer: { flexDirection: 'row', alignItems: 'center' },
        headerIcon: { color: colors.heading, marginRight: 12 },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold' },
        badge: { paddingVertical: 4, paddingHorizontal: 10, borderRadius: 20, fontSize: 12, fontWeight: '600', color: colors.white },
        infoBadge: { backgroundColor: colors.textMuted },
        okBadge: { backgroundColor: colors.success },
        detailRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 4 },
        label: { fontWeight: 'bold', color: colors.textMuted },
        value: { color: colors.primary },
        detailsText: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderColor: colors.border },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 16 },
    });
};

export default AdminLogPage;