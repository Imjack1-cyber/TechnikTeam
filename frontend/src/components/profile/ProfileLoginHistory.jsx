import React, { useState } from 'react';
import { View, Text, Alert, Platform, TouchableOpacity, StyleSheet } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import ConfirmationModal from '../ui/ConfirmationModal';

const ProfileLoginHistory = ({ loginHistory, onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const { addToast } = useToast();

    const [forgettingIp, setForgettingIp] = useState(null);
    const [isForgetAllModalOpen, setIsForgetAllModalOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const getDeviceIcon = (deviceType) => {
        switch (deviceType?.toLowerCase()) {
            case 'desktop': return 'desktop';
            case 'mobile': return 'mobile-alt';
            case 'tablet': return 'tablet-alt';
            default: return 'question-circle';
        }
    };
    
    const confirmForgetIp = async () => {
        if (!forgettingIp) return;
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/public/profile/known-ips/forget', { ipAddress: forgettingIp });
            if (result.success) {
                addToast('Standort erfolgreich vergessen.', 'success');
                onUpdate(); // Reload profile data
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setForgettingIp(null);
        }
    };

    const confirmForgetAll = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/public/profile/known-ips/forget-all');
            if (result.success) {
                addToast('Alle Standorte erfolgreich vergessen.', 'success');
                onUpdate();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setIsForgetAllModalOpen(false);
        }
    };


    const renderItem = (item, index) => (
        <View style={styles.detailsListRow} key={index}>
            <View style={{ flexDirection: 'row', alignItems: 'center', flex: 1, gap: 8 }}>
                <Icon name={getDeviceIcon(item.deviceType)} size={16} />
                <View>
                    <Text style={styles.detailsListLabel}>{item.ipAddress} ({item.countryCode || 'N/A'})</Text>
                    <Text style={styles.detailsListValue}>{item.deviceType}</Text>
                </View>
            </View>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 16 }}>
                <Text style={styles.detailsListValue}>{new Date(item.lastSeen).toLocaleDateString('de-DE')}</Text>
                <TouchableOpacity onPress={() => setForgettingIp(item.ipAddress)}>
                    <Icon name="times-circle" solid size={20} color={styles.detailsListValue.color} />
                </TouchableOpacity>
            </View>
        </View>
    );

    return (
        <>
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Bekannte Login-Standorte</Text>
                <Text style={styles.subtitle}>Eine Liste der IP-Adressen (Standorte), von denen Sie sich zuletzt angemeldet haben und für die kein 2FA erforderlich ist.</Text>
                
                {loginHistory && loginHistory.length > 0 ? (
                    loginHistory.map((item, index) => renderItem(item, index))
                ) : (
                    <Text>Keine bekannten Standorte verfügbar.</Text>
                )}

                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton, {marginTop: 16}]} onPress={() => setIsForgetAllModalOpen(true)}>
                    <Text style={styles.dangerOutlineButtonText}>Alle bekannten Standorte vergessen</Text>
                </TouchableOpacity>
            </View>

            {forgettingIp && (
                <ConfirmationModal
                    isOpen={!!forgettingIp}
                    onClose={() => setForgettingIp(null)}
                    onConfirm={confirmForgetIp}
                    isSubmitting={isSubmitting}
                    title={`IP ${forgettingIp} vergessen?`}
                    message="Wenn Sie diesen Standort vergessen, wird bei der nächsten Anmeldung von diesem Standort aus eine Zwei-Faktor-Authentifizierung (2FA) erforderlich, auch wenn sie kürzlich verwendet wurde."
                    confirmText="Vergessen"
                    confirmButtonVariant="danger"
                />
            )}
            <ConfirmationModal
                isOpen={isForgetAllModalOpen}
                onClose={() => setIsForgetAllModalOpen(false)}
                onConfirm={confirmForgetAll}
                isSubmitting={isSubmitting}
                title="Alle bekannten Standorte vergessen?"
                message="Bei Ihrer nächsten Anmeldung von einem beliebigen Standort aus wird eine Zwei-Faktor-Authentifizierung (2FA) erforderlich sein."
                confirmText="Alle vergessen"
                confirmButtonVariant="danger"
            />
        </>
    );
};

const pageStyles = (theme) => {
    return StyleSheet.create({});
};

export default ProfileLoginHistory;