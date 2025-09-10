import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, Platform } from 'react-native';
import { useDownloadStore } from '../../store/downloadStore';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders, shadows } from '../../styles/theme';
import ProgressBar from './ProgressBar';
import Icon from '@expo/vector-icons/FontAwesome5';
import * as Sharing from 'expo-sharing';

const DownloadItem = ({ download, downloadId }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const removeDownload = useDownloadStore(state => state.removeDownload);

    const handleOpen = async () => {
        if (Platform.OS === 'web' || !download.fileUri) return;
        try {
            if (await Sharing.isAvailableAsync()) {
                await Sharing.shareAsync(download.fileUri, { dialogTitle: download.filename });
            }
        } catch (error) {
            console.error("Error sharing file:", error);
        }
    };

    return (
        <View style={styles.downloadItem}>
            <View style={{flex: 1}}>
                <Text style={styles.filename} numberOfLines={1}>{download.filename}</Text>
                <ProgressBar progress={download.progress / download.total} />
            </View>
            {download.status === 'completed' && Platform.OS !== 'web' ? (
                <TouchableOpacity onPress={handleOpen} style={styles.actionButton}>
                    <Icon name="folder-open" size={20} color={colors.success} />
                </TouchableOpacity>
            ) : null}
            <TouchableOpacity onPress={() => removeDownload(downloadId)} style={styles.actionButton}>
                <Icon name="times" size={20} color={colors.textMuted} />
            </TouchableOpacity>
        </View>
    );
};

const DownloadsIndicator = () => {
    const downloads = useDownloadStore(state => state.downloads);
    const theme = useAuthStore(state => state.theme);
    const styles = pageStyles(theme);
    const colors = getThemeColors(theme);

    const activeDownloads = Object.entries(downloads);

    if (activeDownloads.length === 0) {
        return null;
    }

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Aktive Downloads</Text>
            <FlatList
                data={activeDownloads}
                keyExtractor={([id, _]) => id}
                renderItem={({ item: [id, download] }) => <DownloadItem download={download} downloadId={id} />}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            position: 'absolute',
            bottom: spacing.lg,
            right: spacing.lg,
            width: 320,
            maxHeight: 400,
            backgroundColor: colors.surface,
            borderRadius: borders.radius,
            borderWidth: 1,
            borderColor: colors.border,
            ...shadows.lg,
            zIndex: 10000,
            padding: spacing.md,
        },
        title: {
            fontSize: typography.h4,
            fontWeight: 'bold',
            color: colors.heading,
            marginBottom: spacing.sm,
        },
        downloadItem: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            paddingVertical: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        filename: {
            color: colors.text,
            marginBottom: spacing.xs,
        },
        actionButton: {
            padding: spacing.sm,
        }
    });
};

export default DownloadsIndicator;