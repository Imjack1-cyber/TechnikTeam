import React, { useCallback, useState, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TextInput, TouchableOpacity, ActivityIndicator, Alert } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import { RadioButton } from 'react-native-paper';

const AdminAuthLogPage = () => {
    const apiCall = useCallback(() => apiClient.get('/admin/auth-log'), []);
    const { data: logs, loading, error, reload } = useApi(apiCall);
    const { user, isAdmin } = useAuthStore(state => ({ user: state.user, isAdmin: state.isAdmin }));
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [showFilter, setShowFilter] = useState('ACTIVE');
    const [userFilter, setUserFilter] = useState('');
    const [ipFilter, setIpFilter] = useState('');

    const isSessionActive = useCallback((log) => log.eventType === 'LOGIN_SUCCESS' && !log.revoked && log.tokenExpiry && new Date(log.tokenExpiry) > new Date(), []);

    const filteredLogs = useMemo(() => {
        if (!logs) return [];
        return logs.filter(log => {
            const matchesUser = userFilter ? log.username.toLowerCase().includes(userFilter.toLowerCase()) : true;
            const matchesIp = ipFilter ? log.ipAddress.toLowerCase().includes(ipFilter.toLowerCase()) : true;
            const matchesStatus = showFilter === 'ALL' || (showFilter === 'ACTIVE' ? isSessionActive(log) : !isSessionActive(log));
            return matchesUser && matchesIp && matchesStatus;
        });
    }, [logs, showFilter, userFilter, ipFilter, isSessionActive]);

    const handleForceLogout = (jti) => {
        Alert.alert('Sitzung beenden?', 'Der Benutzer wird bei der nächsten Aktion ausgeloggt.', [
            { text: 'Abbrechen', style: 'cancel'},
            { text: 'Beenden', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.post('/admin/auth-log/revoke-session', { jti });
                    if (result.success) {
                        addToast('Sitzung erfolgreich widerrufen.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }}
        ]);
    };

    const renderItem = ({ item: log }) => {
        const isActive = isSessionActive(log);
        return (
            <View style={styles.card}>
                <Text style={styles.cardTitle}>{log.username}</Text>
                <Text>{new Date(log.timestamp).toLocaleString('de-DE')}</Text>
                <Text>{log.ipAddress} ({log.countryCode || 'N/A'})</Text>
                <Text><Icon name={log.deviceType === 'Desktop' ? 'desktop' : 'mobile-alt'} /> {log.deviceType}</Text>
                {isActive && (
                    <TouchableOpacity style={[styles.button, styles.dangerButton, {marginTop: 8}]} onPress={() => handleForceLogout(log.jti)}>
                        <Text style={styles.buttonText}>Logout erzwingen</Text>
                    </TouchableOpacity>
                )}
            </View>
        );
    };

    return (
        <View style={styles.container}>
            <View style={styles.headerContainer}>
                <Icon name="history" size={24} style={styles.headerIcon} />
			    <Text style={styles.title}>Login-Verlauf</Text>
            </View>
			<View style={styles.filterContainer}>
                <TextInput style={styles.input} placeholder="Benutzer filtern..." value={userFilter} onChangeText={setUserFilter} />
                <TextInput style={styles.input} placeholder="IP filtern..." value={ipFilter} onChangeText={setIpFilter} />
                <RadioButton.Group onValueChange={newValue => setShowFilter(newValue)} value={showFilter}>
                    <View style={{flexDirection: 'row', justifyContent: 'space-around'}}>
                        <View style={styles.radioRow}><RadioButton value="ALL" /><Text>Alle</Text></View>
                        <View style={styles.radioRow}><RadioButton value="ACTIVE" /><Text>Nur Aktive</Text></View>
                        <View style={styles.radioRow}><RadioButton value="INACTIVE" /><Text>Nur Inaktive</Text></View>
                    </View>
                </RadioButton.Group>
            </View>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={filteredLogs}
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
        headerContainer: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        filterContainer: { paddingHorizontal: 16, marginBottom: 16, gap: 8 },
        radioRow: { flexDirection: 'row', alignItems: 'center' }
    });
};

export default AdminAuthLogPage;