import React, { useCallback, useState } from 'react';
import { View, Text, FlatList, TouchableOpacity, Alert, ActivityIndicator, Platform } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';
import { useToast } from '../../context/ToastContext';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import ConfirmationModal from '../ui/ConfirmationModal';

const ProfileActiveSessions = ({ onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const apiCall = useCallback(() => apiClient.get('/public/sessions'), []);
    const { data: sessions, loading, error } = useApi(apiCall);

    const [revokingSession, setRevokingSession] = useState(null);
    const [isRevokeAllModalOpen, setIsRevokeAllModalOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleRevoke = (session) => {
        setRevokingSession(session);
    };
    
    const handleRevokeAll = () => {
        setIsRevokeAllModalOpen(true);
    };

    const confirmRevoke = async () => {
        if (!revokingSession) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/public/sessions/${revokingSession.jti}/revoke`);
            if (result.success) {
                addToast('Sitzung erfolgreich widerrufen.', 'success');
                onUpdate();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setRevokingSession(null);
        }
    };

    const confirmRevokeAll = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/public/sessions/revoke-all');
            if (result.success) {
                addToast(result.message, 'success');
                onUpdate();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setIsRevokeAllModalOpen(false);
        }
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
        <>
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
            
            {revokingSession && (
                <ConfirmationModal
                    isOpen={!!revokingSession}
                    onClose={() => setRevokingSession(null)}
                    onConfirm={confirmRevoke}
                    isSubmitting={isSubmitting}
                    title="Sitzung widerrufen?"
                    message={`Sind Sie sicher, dass Sie die Sitzung von ${revokingSession.deviceType || 'Unbekannt'} (${revokingSession.ipAddress}) beenden möchten?`}
                    confirmText="Beenden"
                    confirmButtonVariant="danger"
                />
            )}

            <ConfirmationModal
                isOpen={isRevokeAllModalOpen}
                onClose={() => setIsRevokeAllModalOpen(false)}
                onConfirm={confirmRevokeAll}
                isSubmitting={isSubmitting}
                title="Alle anderen Sitzungen beenden?"
                message="Alle anderen angemeldeten Geräte werden abgemeldet."
                confirmText="Alle beenden"
                confirmButtonVariant="danger"
            />
        </>
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