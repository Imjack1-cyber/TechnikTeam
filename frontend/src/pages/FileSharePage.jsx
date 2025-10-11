import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, SafeAreaView, Platform, Image } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders, shadows } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useToast } from '../context/ToastContext';
import { useTransferStore } from '../store/transferStore';
import { v4 as uuidv4 } from 'uuid';
import TransferButton from '../components/ui/TransferButton';

const FileSharePage = () => {
    const route = useRoute();
    const { token } = route.params;
    const { addToast } = useToast();
    const { addTransfer, updateTransfer } = useTransferStore();
    const [transferId, setTransferId] = useState(null);

    // This is a public page, so we default to the light theme.
    const theme = useAuthStore(state => state.theme || 'light');
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const apiCall = useCallback(() => apiClient.get(`/public/files/share/${token}/meta`), [token]);
    const { data: file, loading, error } = useApi(apiCall);
    
    const handleDownload = async () => {
        if (!file) return;

        const newTransferId = uuidv4();
        setTransferId(newTransferId);
        addTransfer(newTransferId, file.filename, 'download', file.size || null);

        try {
            const downloadUrl = `${apiClient.getBaseUrl()}/public/files/share/${token}`;
            await apiClient.downloadFile(downloadUrl, file.filename, newTransferId);
            // The apiClient now handles updating the store to 'completed'
        } catch (err) {
            if (err.name !== 'AbortError') {
                addToast(`Download fehlgeschlagen: ${err.message}`, 'error');
                updateTransfer(newTransferId, { status: 'error' });
            }
        }
    };


    const renderContent = () => {
        if (loading) {
            return <ActivityIndicator size="large" color={colors.primary} />;
        }
        if (error || !file) {
            return (
                <>
                    <Icon name="times-circle" solid size={80} color={colors.danger} style={styles.icon} />
                    <Text style={styles.statusTextError}>Ungültiger Link</Text>
                    <Text style={styles.message}>Dieser Freigabe-Link ist ungültig, abgelaufen oder Sie haben keine Berechtigung, auf diese Datei zuzugreifen.</Text>
                </>
            );
        }
        return (
            <>
                <Icon name="check-circle" solid size={80} color={colors.success} style={styles.icon} />
                <Text style={styles.title}>Download bereit</Text>
                <Text style={styles.message}>Klicken Sie unten, um die Datei herunterzuladen:</Text>
                <View style={styles.fileInfoBox}>
                    <Icon name="file-alt" solid size={24} color={colors.primary} />
                    <Text style={styles.filename} numberOfLines={2}>{file.filename}</Text>
                </View>
                <TransferButton
                    transferId={transferId}
                    onPress={handleDownload}
                    buttonStyle={[styles.button, styles.successButton, {width: '100%'}]}
                    textStyle={[styles.buttonText, {fontSize: 18}]}
                    defaultIcon="download"
                    defaultText="Herunterladen"
                />
            </>
        );
    };

    return (
        <SafeAreaView style={styles.container}>
             <View style={styles.card}>
                <View style={styles.header}>
                    <Image source={require('../../assets/icon.png')} style={styles.logo} />
                    <Text style={styles.headerText}>TechnikTeam Dateifreigabe</Text>
                </View>
                <View style={styles.content}>
                    {renderContent()}
                </View>
                 <View style={styles.footer}>
                    <Text style={styles.footerText}>TechnikTeam © {new Date().getFullYear()}</Text>
                </View>
            </View>
        </SafeAreaView>
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
            maxWidth: 450,
            backgroundColor: colors.surface,
            borderRadius: borders.radius * 2,
            ...shadows.lg,
            overflow: 'hidden',
        },
        header: {
            flexDirection: 'row',
            alignItems: 'center',
            padding: spacing.md,
            backgroundColor: colors.surface,
            borderBottomWidth: 1,
            borderColor: colors.border,
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
        },
        icon: {
            marginBottom: spacing.md,
        },
        title: {
            fontSize: typography.h1,
            fontWeight: 'bold',
            textAlign: 'center',
            marginBottom: spacing.sm,
            color: colors.heading,
        },
        message: {
            fontSize: typography.body,
            textAlign: 'center',
            color: colors.textMuted,
            marginBottom: spacing.lg,
            maxWidth: 400,
        },
        fileInfoBox: {
            flexDirection: 'row',
            alignItems: 'center',
            backgroundColor: colors.background,
            paddingHorizontal: spacing.md,
            paddingVertical: spacing.sm,
            borderRadius: borders.radius,
            borderWidth: 1,
            borderColor: colors.border,
            width: '100%',
            marginBottom: spacing.xl,
        },
        filename: {
            fontSize: typography.h4,
            fontWeight: '600',
            color: colors.text,
            marginLeft: spacing.md,
            flexShrink: 1,
        },
        footer: {
            paddingVertical: spacing.sm,
            backgroundColor: colors.background,
            borderTopWidth: 1,
            borderColor: colors.border,
        },
        footerText: {
            textAlign: 'center',
            fontSize: typography.caption,
            color: colors.textMuted,
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
    });
};

export default FileSharePage;