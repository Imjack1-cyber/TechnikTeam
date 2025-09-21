import React, { useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, SafeAreaView, Platform, Image } from 'react-native';
import { useRoute } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { getThemeColors, spacing, typography, borders, shadows } from '../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useToast } from '../context/ToastContext';
import { useDownloadStore } from '../store/downloadStore';
import { v4 as uuidv4 } from 'uuid';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';

const FileSharePage = () => {
    const route = useRoute();
    const { token } = route.params;
    const { addToast } = useToast();
    const addDownload = useDownloadStore(state => state.addDownload);
    const markDownloadAsLocallyComplete = useDownloadStore(state => state.markDownloadAsLocallyComplete);

    // This is a public page, so we default to the light theme.
    const theme = useAuthStore(state => state.theme || 'light');
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

    const apiCall = useCallback(() => apiClient.get(`/public/files/share/${token}/meta`), [token]);
    const { data: file, loading, error } = useApi(apiCall);
    
    const handleDownload = async () => {
        if (!file) return;

        addToast(`Download für "${file.filename}" wird vorbereitet...`, 'info');
        const downloadId = uuidv4();
        addDownload(downloadId, file.filename);

        try {
            // The download endpoint itself is now the share link
            const downloadUrl = `${apiClient.getBaseUrl()}/public/files/share/${token}`;
            
            // Re-implementing download logic here since this is a public page
            if (Platform.OS === 'web') {
                 const response = await fetch(downloadUrl);
                 if (!response.ok) throw new Error('Download-Link ist ungültig oder abgelaufen.');
                 const blob = await response.blob();
                 const url = window.URL.createObjectURL(blob);
                 const a = document.createElement('a');
                 a.href = url;
                 a.download = file.filename;
                 document.body.appendChild(a);
                 a.click();
                 a.remove();
                 window.URL.revokeObjectURL(url);
            } else {
                // Native platform download logic
                const fileUri = FileSystem.documentDirectory + file.filename.replace(/[^a-zA-Z0-9.\-_]/g, '_');
                const { uri } = await FileSystem.downloadAsync(
                    downloadUrl,
                    fileUri,
                );
                addToast('Download abgeschlossen!', 'success');
                if (await Sharing.isAvailableAsync()) {
                    await Sharing.shareAsync(uri, { dialogTitle: file.filename });
                }
            }
            markDownloadAsLocallyComplete(downloadId);
        } catch (err) {
            addToast(`Download fehlgeschlagen: ${err.message}`, 'error');
            useDownloadStore.getState().updateDownload(downloadId, { status: 'error' });
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
                    <Text style={styles.title}>Ungültiger Link</Text>
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
                <TouchableOpacity style={[styles.button, styles.successButton, {width: '100%'}]} onPress={handleDownload}>
                    <Icon name="download" size={20} color={colors.white} />
                    <Text style={[styles.buttonText, {fontSize: 18}]}>Herunterladen</Text>
                </TouchableOpacity>
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
    });
};

export default FileSharePage;