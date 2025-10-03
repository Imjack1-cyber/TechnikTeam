import React, { useCallback, useState, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, Switch } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import { Picker } from '@react-native-picker/picker';
import ConfirmationModal from '../../components/ui/ConfirmationModal';

const AdminLogPage = () => {
	const navigation = useNavigation();
	const apiCall = useCallback(() => apiClient.get('/logs'), []);
	const { data: logs, loading, error, reload } = useApi(apiCall, { subscribeTo: 'ADMIN_LOG' });
	const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
	const { addToast } = useToast();

    const theme = useAuthStore(state => state.theme);
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [searchTerm, setSearchTerm] = useState('');
    const [selectedUser, setSelectedUser] = useState('');
    const [selectedAction, setSelectedAction] = useState('');
    const [showOnlyRevocable, setShowOnlyRevocable] = useState(false);
    const [revokingLog, setRevokingLog] = useState(null);
    const [isSubmittingRevoke, setIsSubmittingRevoke] = useState(false);

	const canRevoke = isAdmin || user?.permissions.includes('LOG_REVOKE');

    const uniqueUsers = useMemo(() => {
        if (!logs) return [];
        return [...new Set(logs.map(log => log.adminUsername))].sort();
    }, [logs]);

    const uniqueActions = useMemo(() => {
        if (!logs) return [];
        return [...new Set(logs.map(log => log.actionType))].sort();
    }, [logs]);

    const filteredLogs = useMemo(() => {
        if (!logs) return [];
        return logs.filter(log => {
            const matchesSearch = searchTerm ? log.details.toLowerCase().includes(searchTerm.toLowerCase()) : true;
            const matchesUser = selectedUser ? log.adminUsername === selectedUser : true;
            const matchesAction = selectedAction ? log.actionType === selectedAction : true;
            const matchesRevocable = showOnlyRevocable ? (log.status === 'ACTIVE' && log.context && JSON.parse(log.context).revocable === true) : true;
            return matchesSearch && matchesUser && matchesAction && matchesRevocable;
        });
    }, [logs, searchTerm, selectedUser, selectedAction, showOnlyRevocable]);


	const confirmRevoke = async () => {
        if (!revokingLog) return;
        setIsSubmittingRevoke(true);
        try {
            const result = await apiClient.post(`/logs/${revokingLog.id}/revoke`);
            if (result.success) {
                addToast('Aktion erfolgreich widerrufen.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Widerrufen fehlgeschlagen: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingRevoke(false);
            setRevokingLog(null);
        }
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
                        <TouchableOpacity style={[styles.button, {backgroundColor: getThemeColors(theme).warning}, isRevocable ? {} : styles.disabledButton]} onPress={() => setRevokingLog(log)} disabled={!isRevocable}>
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

            <View style={styles.filterContainer}>
                <TextInput style={styles.input} placeholder="In Details suchen..." value={searchTerm} onChangeText={setSearchTerm} />
                <View style={{flexDirection: 'row', gap: spacing.md}}>
                    <View style={{flex: 1}}>
                        <Text style={styles.label}>Admin</Text>
                        <Picker selectedValue={selectedUser} onValueChange={setSelectedUser}>
                            <Picker.Item label="Alle" value="" />
                            {uniqueUsers.map(u => <Picker.Item key={u} label={u} value={u} />)}
                        </Picker>
                    </View>
                    <View style={{flex: 1}}>
                         <Text style={styles.label}>Aktionstyp</Text>
                        <Picker selectedValue={selectedAction} onValueChange={setSelectedAction}>
                            <Picker.Item label="Alle" value="" />
                            {uniqueActions.map(a => <Picker.Item key={a} label={a} value={a} />)}
                        </Picker>
                    </View>
                </View>
                {canRevoke && (
                    <View style={styles.switchRow}>
                        <Text style={styles.label}>Nur widerrufbare Aktionen anzeigen</Text>
                        <Switch value={showOnlyRevocable} onValueChange={setShowOnlyRevocable} />
                    </View>
                )}
            </View>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={filteredLogs}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Keine Log-Einträge gefunden, die den Filtern entsprechen.</Text></View>}
            />
            {revokingLog && (
                <ConfirmationModal
                    isOpen={!!revokingLog}
                    onClose={() => setRevokingLog(null)}
                    onConfirm={confirmRevoke}
                    title="Aktion widerrufen?"
                    message={`Möchten Sie die Aktion "${revokingLog.actionType}" (ID: ${revokingLog.id}) wirklich widerrufen? Dies führt die entsprechende Gegenaktion aus und kann nicht rückgängig gemacht werden.`}
                    confirmText="Widerrufen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingRevoke}
                />
            )}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1 },
        contentContainer: { paddingHorizontal: spacing.md, paddingBottom: spacing.md },
        headerContainer: { flexDirection: 'row', alignItems: 'center', padding: spacing.md },
        headerIcon: { color: colors.heading, marginRight: 12 },
        filterContainer: {
            paddingHorizontal: spacing.md,
            marginBottom: spacing.md,
            backgroundColor: colors.surface,
            padding: spacing.md,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        switchRow: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginTop: spacing.md,
        },
        cardHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
        cardTitle: { fontSize: typography.h4, fontWeight: 'bold' },
        badge: { paddingVertical: 4, paddingHorizontal: 10, borderRadius: 20, fontSize: 12, fontWeight: '600', color: colors.white, overflow: 'hidden' },
        infoBadge: { backgroundColor: colors.textMuted },
        okBadge: { backgroundColor: colors.success },
        detailRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 4 },
        value: { color: colors.primary },
        detailsText: { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderColor: colors.border },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 16 },
    });
};

export default AdminLogPage;