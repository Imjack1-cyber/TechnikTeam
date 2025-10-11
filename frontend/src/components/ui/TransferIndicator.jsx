import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, FlatList, Platform } from 'react-native';
import { useTransferStore } from '../../store/transferStore';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders, shadows } from '../../styles/theme';
import ProgressBar from './ProgressBar';
import Icon from 'react-native-vector-icons/FontAwesome5';
import * as Sharing from 'expo-sharing';

const formatBytes = (bytes, decimals = 2) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

const formatSpeed = (bytesPerSecond) => {
    if (bytesPerSecond < 1024) {
        return `${bytesPerSecond.toFixed(0)} B/s`;
    }
    return `${formatBytes(bytesPerSecond)}/s`;
};

const formatEta = (seconds) => {
    if (seconds === Infinity || seconds > 3600) return '> 1h';
    if (seconds > 60) return `${Math.round(seconds / 60)} min`;
    return `${Math.round(seconds)} s`;
};

const TransferItem = ({ transfer, transferId }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const { cancelTransfer } = useTransferStore();

    const handleOpen = async () => {
        if (Platform.OS === 'web' || !transfer.fileUri) return;
        try {
            if (await Sharing.isAvailableAsync()) {
                await Sharing.shareAsync(transfer.fileUri, { dialogTitle: transfer.filename });
            }
        } catch (error) {
            console.error("Error sharing file:", error);
        }
    };

    const progressPercent = (transfer.progress / transfer.total) * 100;
    const isFinished = transfer.status === 'completed' || transfer.status === 'error' || transfer.status === 'canceled';

    return (
        <View style={styles.downloadItem}>
            <Icon name={transfer.type === 'upload' ? 'upload' : 'download'} size={20} color={colors.primary} />
            <View style={{flex: 1}}>
                <Text style={styles.filename} numberOfLines={1}>{transfer.filename}</Text>
                <ProgressBar progress={progressPercent / 100} />
                <View style={styles.statusRow}>
                    <Text style={styles.statusText}>
                        {formatBytes(transfer.progress)} / {formatBytes(transfer.total)}
                    </Text>
                    {!isFinished && (
                        <>
                            <Text style={styles.statusText}>{formatSpeed(transfer.speed)}</Text>
                            <Text style={styles.statusText}>ETA: {formatEta(transfer.eta)}</Text>
                        </>
                    )}
                </View>
            </View>
            {transfer.status === 'completed' && Platform.OS !== 'web' ? (
                <TouchableOpacity onPress={handleOpen} style={styles.actionButton}>
                    <Icon name="folder-open" size={20} color={colors.success} />
                </TouchableOpacity>
            ) : null}
            {transfer.status === 'progressing' && (
                 <TouchableOpacity onPress={() => cancelTransfer(transferId)} style={styles.actionButton}>
                    <Icon name="times" size={20} color={colors.textMuted} />
                </TouchableOpacity>
            )}
             {transfer.status === 'error' && <Icon name="exclamation-circle" size={20} color={colors.danger} />}
             {transfer.status === 'canceled' && <Icon name="ban" size={20} color={colors.textMuted} />}
        </View>
    );
};

const TransferIndicator = () => {
    const transfers = useTransferStore(state => state.transfers);
    const theme = useAuthStore(state => state.theme);
    const styles = pageStyles(theme);

    const activeTransfers = Object.entries(transfers)
        .filter(([id, transfer]) => transfer.displayMode === 'indicator');

    if (activeTransfers.length === 0) {
        return null;
    }

    return (
        <View style={styles.container}>
            <Text style={styles.title}>Übertragungen</Text>
            <FlatList
                data={activeTransfers}
                keyExtractor={([id, _]) => id}
                renderItem={({ item: [id, transfer] }) => <TransferItem transfer={transfer} transferId={id} />}
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
            gap: spacing.md,
            paddingVertical: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        filename: {
            color: colors.text,
            marginBottom: spacing.xs,
            fontWeight: '500',
        },
        actionButton: {
            padding: spacing.sm,
        },
        statusRow: {
            flexDirection: 'row',
            justifyContent: 'space-between',
            marginTop: spacing.xs,
        },
        statusText: {
            fontSize: typography.caption,
            color: colors.textMuted,
        }
    });
};

export default TransferIndicator;