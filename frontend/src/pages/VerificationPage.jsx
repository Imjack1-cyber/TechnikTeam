import React, { useCallback } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, TouchableOpacity, Linking, Image } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const VerificationPage = () => {
    const route = useRoute();
    const { token } = route.params;
    const apiCall = useCallback(() => apiClient.get(`/public/verify/${token}`), [token]);
    const { data: userData, loading, error } = useApi(apiCall);

    // Hardcoding theme as this is a public page
    const theme = 'light';
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const renderContent = () => {
        if (loading) {
            return <ActivityIndicator size="large" color={colors.primary} />;
        }

        if (error || !userData || userData.status !== 'ACTIVE') {
            return (
                <>
                    <Icon name="times-circle" solid size={80} color={colors.danger} style={styles.icon} />
                    <Text style={styles.statusTextError}>Verifizierung fehlgeschlagen</Text>
                    <Text style={styles.message}>Dieser Ausweis ist ungültig oder gehört zu einem inaktiven Mitglied.</Text>
                </>
            );
        }

        return (
            <>
                <Icon name="check-circle" solid size={80} color={colors.success} style={styles.icon} />
                <Text style={styles.statusTextSuccess}>Mitglied verifiziert</Text>
                <Text style={styles.nameText}>{userData.username}</Text>
                <Text style={styles.roleText}>{userData.roleName}</Text>
            </>
        );
    };

    return (
        <View style={styles.container}>
            <View style={styles.card}>
                <View style={styles.header}>
                    <Image source={require('../../assets/icon.png')} style={styles.logo} />
                    <Text style={styles.headerText}>TechnikTeam Verifizierung</Text>
                </View>
                <View style={styles.content}>
                    {renderContent()}
                </View>
                <View style={styles.footer}>
                    <Text style={styles.footerText}>Bei Fragen oder Problemen kontaktieren Sie die Leitung:</Text>
                    <TouchableOpacity onPress={() => Linking.openURL('mailto:leitung.technik.team@no-bs.de')}>
                        <Text style={styles.emailLink}>leitung.technik.team@no-bs.de</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            flex: 1,
            backgroundColor: colors.background,
            justifyContent: 'center',
            alignItems: 'center',
            padding: spacing.md,
        },
        card: {
            width: '100%',
            maxWidth: 400,
            borderRadius: borders.radius,
            borderWidth: 1,
            borderColor: colors.border,
            overflow: 'hidden',
        },
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            padding: spacing.md,
            backgroundColor: colors.surface,
        },
        logo: {
            width: 40,
            height: 40,
            marginRight: spacing.sm,
        },
        headerText: {
            fontSize: typography.h4,
            fontWeight: 'bold',
            color: colors.heading,
        },
        content: {
            padding: spacing.xl,
            alignItems: 'center',
            backgroundColor: colors.surface,
        },
        icon: {
            marginBottom: spacing.md,
        },
        statusTextSuccess: {
            fontSize: typography.h2,
            fontWeight: 'bold',
            color: colors.success,
            marginBottom: spacing.lg,
        },
        statusTextError: {
            fontSize: typography.h2,
            fontWeight: 'bold',
            color: colors.danger,
            marginBottom: spacing.lg,
        },
        message: {
            fontSize: typography.body,
            color: colors.textMuted,
            textAlign: 'center',
        },
        nameText: {
            fontSize: typography.h1,
            fontWeight: '600',
            color: colors.heading,
        },
        roleText: {
            fontSize: typography.h4,
            color: colors.textMuted,
        },
        footer: {
            padding: spacing.md,
            backgroundColor: colors.background,
            borderTopWidth: 1,
            borderColor: colors.border,
            alignItems: 'center',
        },
        footerText: {
            fontSize: typography.small,
            color: colors.textMuted,
        },
        emailLink: {
            fontSize: typography.small,
            color: colors.primary,
            fontWeight: '500',
        }
    });
};

export default VerificationPage;