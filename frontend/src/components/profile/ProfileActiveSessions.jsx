import React, { useCallback, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, Alert, ActivityIndicator } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const ProfileActiveSessions = ({ onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const apiCall = useCallback(() => apiClient.get('/public/sessions'), []);
    const { data: sessions, loading, error, reload } = useApi(apiCall);

    const handleRevoke = (session) => {
        Alert.alert('Sitzung widerrufen?', `Sind Sie sicher, dass Sie die Sitzung von ${session.deviceType || 'Unbekannt'} (${session.ipAddress}) beenden möchten?`, [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Beenden', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.post(`/public/sessions/${session.jti}/revoke`);
                    if (result.success) {
                        addToast('Sitzung erfolgreich widerrufen.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) {
                    addToast(`Fehler: ${err.message}`, 'error');
                }
            }}
        ]);
    };
    
    const handleRevokeAll = () => {
        Alert.alert('Alle anderen Sitzungen beenden?', 'Alle anderen angemeldeten Geräte werden abgemeldet.', [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Alle beenden', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.post('/public/sessions/revoke-all');
                    if (result.success) {
                        addToast(result.message, 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) {
                    addToast(`Fehler: ${err.message}`, 'error');
                }
            }}
        ]);
    };

    const getDeviceIcon = (deviceType) => {
        switch (deviceType?.toLowerCase()) {
            case 'desktop': return 'desktop';
            case 'mobile': return 'mobile-alt';
            case 'tablet': return 'tablet-alt';
            default: return 'question-circle';
        }
    };

    const renderItem = ({ item }) => (
        <View style={styles.sessionRow}>
            <Icon name={getDeviceIcon(item.deviceType)} size={24} style={styles.deviceIcon} />
            <View style={{flex: 1}}>
                <Text style={styles.deviceInfo}>{item.deviceType || 'Unbekannt'}</Text>
                <Text style={styles.ipInfo}>{item.ipAddress} ({item.countryCode || 'N/A'})</Text>
            </View>
            {item.isCurrentSession ? (
                <Text style={styles.currentTag}>Aktuell</Text>
            ) : (
                <TouchableOpacity onPress={() => handleRevoke(item)}>
                    <Icon name="times-circle" size={20} color={colors.danger} />
                </TouchableOpacity>
            )}
        </View>
    );

    return (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>Aktive Sitzungen</Text>
            <Text style={styles.subtitle}>Hier sehen Sie alle Geräte, auf denen Sie aktuell angemeldet sind.</Text>
            {loading && <ActivityIndicator />}
            {error && <Text style={styles.errorText}>{error}</Text>}

            <FlatList
                data={sessions}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
            />
             <TouchableOpacity style={[styles.button, styles.dangerButton, {marginTop: spacing.md}]} onPress={handleRevokeAll}>
                <Text style={styles.buttonText}>Alle anderen Sitzungen beenden</Text>
            </TouchableOpacity>
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return {
        sessionRow: {
            flexDirection: 'row',
            alignItems: 'center',
            paddingVertical: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
            gap: spacing.md,
        },
        deviceIcon: {
            color: colors.textMuted,
        },
        deviceInfo: {
            fontWeight: 'bold',
            color: colors.text,
        },
        ipInfo: {
            fontSize: typography.small,
            color: colors.textMuted,
        },
        currentTag: {
            fontWeight: 'bold',
            color: colors.success,
        }
    };
};

export default ProfileActiveSessions;