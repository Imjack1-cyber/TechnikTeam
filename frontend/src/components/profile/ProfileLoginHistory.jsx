import React, { useState } from 'react';
import { View, Text, Alert, Platform, TouchableOpacity, StyleSheet } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';

const ProfileLoginHistory = ({ loginHistory, onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const { addToast } = useToast();

    const getDeviceIcon = (deviceType) => {
        switch (deviceType?.toLowerCase()) {
            case 'desktop': return 'desktop';
            case 'mobile': return 'mobile-alt';
            case 'tablet': return 'tablet-alt';
            default: return 'question-circle';
        }
    };
    
    const handleForgetIp = (ipAddress) => {
        const title = `IP ${ipAddress} vergessen?`;
        const message = 'Wenn Sie diesen Standort vergessen, wird bei der nächsten Anmeldung von diesem Standort aus eine Zwei-Faktor-Authentifizierung (2FA) erforderlich, auch wenn sie kürzlich verwendet wurde.';
        const action = async () => {
            try {
                const result = await apiClient.post('/public/profile/known-ips/forget', { ipAddress });
                if (result.success) {
                    addToast('Standort erfolgreich vergessen.', 'success');
                    onUpdate(); // Reload profile data
                } else {
                    throw new Error(result.message);
                }
            } catch (err) {
                addToast(`Fehler: ${err.message}`, 'error');
            }
        };

        if (Platform.OS === 'web') {
            if (window.confirm(`${title}\n\n${message}`)) {
                action();
            }
        } else {
            Alert.alert(title, message, [
                { text: 'Abbrechen', style: 'cancel' },
                { text: 'Vergessen', style: 'destructive', onPress: action }
            ]);
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
                <TouchableOpacity onPress={() => handleForgetIp(item.ipAddress)}>
                    <Icon name="times-circle" solid size={20} color={styles.detailsListValue.color} />
                </TouchableOpacity>
            </View>
        </View>
    );

    return (
        <View style={styles.card}>
            <Text style={styles.cardTitle}>Login-Standorte</Text>
            <Text style={styles.subtitle}>Eine Liste der IP-Adressen, von denen Sie sich zuletzt angemeldet haben.</Text>
            
            {loginHistory && loginHistory.length > 0 ? (
                loginHistory.map((item, index) => renderItem(item, index))
            ) : (
                <Text>Keine Login-Historie verfügbar.</Text>
            )}
        </View>
    );
};

const pageStyles = (theme) => {
    return StyleSheet.create({});
};

export default ProfileLoginHistory;