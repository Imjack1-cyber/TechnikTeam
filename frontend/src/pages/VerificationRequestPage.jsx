import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, SafeAreaView } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const VerificationRequestPage = () => {
    const route = useRoute();
    const navigation = useNavigation();
    const { token } = route.params;
    const { addToast } = useToast();
    const { user, theme } = useAuthStore();
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [finalState, setFinalState] = useState(null); // 'approved' or 'denied'

    const apiCall = useCallback(() => apiClient.get(`/identity-verification/details/${token}`), [token]);
    const { data: request, loading, error } = useApi(apiCall);

    const handleAction = async (decision) => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post('/public/profile/verify-identity', { challengeToken: token, decision });
            if (result.success) {
                addToast(`Anfrage erfolgreich ${decision === 'approve' ? 'bestätigt' : 'abgelehnt'}.`, 'success');
                setFinalState(decision);
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
            navigation.goBack();
        } finally {
            setIsSubmitting(false);
        }
    };

    const getRequestTitle = () => {
        if (!request) return 'Anfrage laden...';
        switch (request.requestType) {
            case 'PASSWORD_RESET': return 'Passwort zurücksetzen';
            case 'MFA_LOGIN': return 'Anmeldung bestätigen';
            default: return 'Anfrage bestätigen';
        }
    };

    const getRequestDescription = () => {
        if (!request) return '';
        const context = request.contextAsMap || {};
        switch (request.requestType) {
            case 'PASSWORD_RESET':
                return `Eine Anfrage zum Zurücksetzen des Passworts für den Benutzer "${request.username}" wurde gestellt.`;
            case 'MFA_LOGIN':
                let desc = `Eine Anmeldung für den Benutzer "${request.username}" wurde angefordert`;
                if (context.ipAddress) {
                    desc += ` von der IP-Adresse ${context.ipAddress}`;
                    if (context.countryCode) desc += ` (${context.countryCode})`;
                }
                desc += '.';
                return desc;
            default:
                return `Eine unbekannte Aktion wurde angefordert.`;
        }
    };

    if (loading) {
        return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
    }

    if (error || !request) {
        return (
            <SafeAreaView style={styles.container}>
                <View style={styles.centered}>
                    <Icon name="times-circle" size={48} color={colors.danger} />
                    <Text style={[styles.title, {marginTop: spacing.md}]}>Ungültige Anfrage</Text>
                    <Text style={styles.subtitle}>{error || 'Dieser Link ist ungültig, abgelaufen oder wurde bereits verwendet.'}</Text>
                </View>
            </SafeAreaView>
        );
    }
    
    if (finalState) {
         return (
            <SafeAreaView style={styles.container}>
                <View style={styles.centered}>
                    <Icon name="check-circle" size={48} color={colors.success} />
                    <Text style={[styles.title, {marginTop: spacing.md}]}>Aktion abgeschlossen</Text>
                    <Text style={styles.subtitle}>Die Anfrage wurde erfolgreich {finalState === 'approve' ? 'bestätigt' : 'abgelehnt'}.</Text>
                </View>
            </SafeAreaView>
        );
    }

    return (
        <SafeAreaView style={styles.container}>
            <View style={styles.centered}>
                <Icon name="shield-alt" size={48} color={colors.primary} style={{marginBottom: spacing.md}} />
                <Text style={styles.title}>{getRequestTitle()}</Text>
                <Text style={styles.subtitle}>{getRequestDescription()}</Text>

                <View style={styles.buttonContainer}>
                    <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => handleAction('deny')} disabled={isSubmitting}>
                        <Icon name="times" size={20} color={colors.white} />
                        <Text style={styles.buttonText}>Ablehnen</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => handleAction('approve')} disabled={isSubmitting}>
                        <Icon name="check" size={20} color={colors.white} />
                        <Text style={styles.buttonText}>Bestätigen</Text>
                    </TouchableOpacity>
                </View>

                {isSubmitting && <ActivityIndicator size="large" style={{marginTop: spacing.lg}} />}
            </View>
        </SafeAreaView>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        buttonContainer: {
            flexDirection: 'row',
            gap: spacing.lg,
            marginTop: spacing.lg,
        }
    });
};

export default VerificationRequestPage;