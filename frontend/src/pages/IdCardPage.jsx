import React from 'react';
import { View, Text, StyleSheet, Image, Platform } from 'react-native';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, typography, spacing, borders, shadows } from '../styles/theme';
import QRCode from 'react-native-qrcode-svg';
import Icon from 'react-native-vector-icons/FontAwesome5';
import apiClient from '../services/apiClient';

const IdCardPage = () => {
    const { user, theme } = useAuthStore(state => ({ 
        user: state.user, 
        theme: state.theme,
    }));
    const colors = getThemeColors(theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };

    const getBaseUrlForQrCode = () => {
        if (Platform.OS === 'web') {
            // On web, construct the URL from the current window location.
            return window.location.origin;
        }
        // For native, use the apiClient which constructs the full domain.
        return apiClient.getRootUrl();
    };

    const baseUrl = getBaseUrlForQrCode();
    const verificationUrl = `${baseUrl}/verify/${user.verificationToken}`;

    return (
        <View style={styles.container}>
            <View style={styles.card}>
                <View style={styles.header}>
                    <Image source={require('../../assets/icon.png')} style={styles.logo} />
                    <Text style={styles.headerText}>TechnikTeam</Text>
                </View>
                <View style={styles.content}>
                    <Icon name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={100} color={colors.primary} />
                    <Text style={styles.nameText}>{user.username}</Text>
                    <Text style={styles.roleText}>{user.roleName}</Text>
                </View>
                <View style={styles.qrContainer}>
                    <QRCode
                        value={verificationUrl}
                        size={120}
                        backgroundColor={colors.surface}
                        color={colors.text}
                    />
                    <Text style={styles.qrHelpText}>Scan to verify</Text>
                </View>
                <View style={styles.footer}>
                    <Text style={styles.footerText}>Offizieller Mitgliedsausweis</Text>
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
            padding: spacing.lg,
        },
        card: {
            backgroundColor: colors.surface,
            borderRadius: borders.radius * 2,
            width: '100%',
            maxWidth: 350,
            ...shadows.lg,
            overflow: 'hidden',
        },
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            backgroundColor: colors.primary,
            padding: spacing.md,
        },
        logo: {
            width: 40,
            height: 40,
            marginRight: spacing.sm,
        },
        headerText: {
            fontSize: typography.h3,
            fontWeight: 'bold',
            color: colors.white,
        },
        content: {
            alignItems: 'center',
            paddingVertical: spacing.xl,
        },
        nameText: {
            fontSize: typography.h1,
            fontWeight: '600',
            color: colors.heading,
            marginTop: spacing.md,
        },
        roleText: {
            fontSize: typography.h4,
            color: colors.textMuted,
            textTransform: 'uppercase',
            letterSpacing: 1,
        },
        qrContainer: {
            alignItems: 'center',
            paddingBottom: spacing.lg,
        },
        qrHelpText: {
            fontSize: typography.caption,
            color: colors.textMuted,
            marginTop: spacing.xs,
        },
        footer: {
            backgroundColor: colors.background,
            padding: spacing.sm,
            borderTopWidth: 1,
            borderColor: colors.border,
        },
        footerText: {
            textAlign: 'center',
            fontSize: typography.caption,
            color: colors.textMuted,
        },
    });
};

export default IdCardPage;