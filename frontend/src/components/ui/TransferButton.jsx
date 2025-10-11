import React, { useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useTransferStore } from '../../store/transferStore';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';

const formatBytes = (bytes, decimals = 1) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
};

const formatSpeed = (bytesPerSecond) => {
    if (!bytesPerSecond || bytesPerSecond < 1) return `0 B/s`;
    return `${formatBytes(bytesPerSecond)}/s`;
};

const formatEta = (seconds) => {
    if (seconds === Infinity || seconds > 3600) return '> 1h';
    if (seconds > 60) return `${Math.round(seconds / 60)} min`;
    return `${Math.round(seconds)} s`;
};

const TransferButton = ({ transferId, onPress, buttonStyle, textStyle, defaultText, defaultIcon, defaultIconColor }) => {
    const { transfer, setTransferDisplayMode, cancelTransfer } = useTransferStore(state => ({
        transfer: state.transfers[transferId],
        setTransferDisplayMode: state.setTransferDisplayMode,
        cancelTransfer: state.cancelTransfer
    }));
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

    useEffect(() => {
        if (transferId) {
            setTransferDisplayMode(transferId, 'button');
        }
        return () => {
            if (transferId) {
                const currentTransfer = useTransferStore.getState().transfers[transferId];
                if(currentTransfer && currentTransfer.status === 'progressing') {
                    setTransferDisplayMode(transferId, 'indicator');
                }
            }
        };
    }, [transferId, setTransferDisplayMode]);


    if (!transfer) {
        return (
            <TouchableOpacity style={buttonStyle} onPress={onPress}>
                {defaultIcon && <Icon name={defaultIcon} size={16} color={defaultIconColor || colors.white} />}
                <Text style={textStyle}>{defaultText}</Text>
            </TouchableOpacity>
        );
    }

    const { status, progress, total, speed, eta } = transfer;
    const progressPercent = total > 0 ? (progress / total) * 100 : 0;

    let content;
    switch (status) {
        case 'starting':
            content = <Text style={textStyle}>Starte...</Text>;
            break;
        case 'progressing':
            const progressText = `${formatBytes(progress)} / ${formatBytes(total)}`;
            const speedText = formatSpeed(speed);
            const etaText = `ETA: ${formatEta(eta)}`;
            const progressSubTextColor = textStyle?.color || colors.white;

            content = (
                <View style={styles.progressContainer}>
                    <View style={[styles.progressBar, { width: `${progressPercent}%` }]} />
                    <View style={styles.progressContent}>
                        <View style={styles.progressTextContainer}>
                            <Text style={[textStyle, styles.progressTextMain]}>{transfer.type === 'upload' ? 'Lade hoch...' : 'Lade herunter...'}</Text>
                            <Text style={[styles.progressSubText, { color: progressSubTextColor }]}>{`${progressText} | ${speedText} | ${etaText}`}</Text>
                        </View>
                        <TouchableOpacity onPress={() => cancelTransfer(transferId)} style={styles.cancelButton}>
                            <Icon name="times" size={16} color={progressSubTextColor} />
                        </TouchableOpacity>
                    </View>
                </View>
            );
            break;
        case 'completed':
            content = <><Icon name="check" size={16} color={textStyle?.color || colors.white} /><Text style={textStyle}>Fertig</Text></>;
            break;
        case 'error':
            content = <><Icon name="times" size={16} color={textStyle?.color || colors.white} /><Text style={textStyle}>Fehler</Text></>;
            break;
        case 'canceled':
            content = <><Icon name="ban" size={16} color={textStyle?.color || colors.white} /><Text style={textStyle}>Abgebrochen</Text></>;
            break;
        default:
            content = <Text style={textStyle}>{defaultText}</Text>;
    }

    return (
        <TouchableOpacity style={buttonStyle} onPress={onPress} disabled={status === 'progressing' || status === 'completed'}>
            {content}
        </TouchableOpacity>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        progressContainer: {
            width: '100%',
            height: '100%',
            justifyContent: 'center',
            alignItems: 'center',
            borderRadius: 6,
            overflow: 'hidden',
        },
        progressBar: {
            position: 'absolute',
            left: 0,
            top: 0,
            bottom: 0,
            backgroundColor: colors.primary,
            opacity: 0.3,
        },
        progressContent: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'space-between',
            width: '100%',
            paddingHorizontal: spacing.md,
        },
        progressTextContainer: {
            flex: 1,
        },
        progressTextMain: {
            fontWeight: 'bold',
        },
        progressSubText: {
            fontSize: typography.caption,
        },
        cancelButton: {
            paddingLeft: spacing.md,
        }
    });
};

export default TransferButton;