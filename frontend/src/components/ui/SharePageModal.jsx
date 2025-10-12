import React, { useState, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, Platform } from 'react-native';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from './AdminModal';
import QRCode from 'react-native-qrcode-svg';
import Clipboard from '@react-native-clipboard/clipboard';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';

const SharePageModal = ({
    isOpen,
    onClose,
    url,
}) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [downloadQr, setDownloadQr] = useState(false);

    const handleCopyToClipboard = () => {
        Clipboard.setString(url);
        addToast('Link in die Zwischenablage kopiert!', 'success');
    };

    const handleDownloadQrCode = () => {
        if (!url) return;
        // This triggers the re-render of the hidden, large QR code
        setDownloadQr(true);
    };

    const onQrDownloadRef = (ref) => {
        if (ref && downloadQr) {
            // Determine a safe filename based on the URL's path segment
            const urlPath = new URL(url).pathname.split('/').filter(p => p).join('-');
            const filename = `TechnikTeam-QRCode-${urlPath || 'home'}-${Date.now()}.png`;

            ref.toDataURL(async (data) => {
                if (Platform.OS === 'web') {
                    try {
                        const link = document.createElement('a');
                        link.href = `data:image/png;base64,${data}`;
                        link.download = filename;
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                        addToast('QR-Code wird heruntergeladen...', 'success');
                    } catch (error) {
                        console.error('Error downloading QR code on web', error);
                        addToast('Fehler beim Speichern des QR-Codes.', 'error');
                    }
                } else {
                    try {
                        const fileUri = FileSystem.cacheDirectory + filename;
                        await FileSystem.writeAsStringAsync(fileUri, data, { encoding: FileSystem.EncodingType.Base64 });
                        if (await Sharing.isAvailableAsync()) {
                            await Sharing.shareAsync(fileUri);
                        } else {
                            addToast('QR-Code konnte nicht geteilt werden.', 'info');
                        }
                    } catch (error) {
                        console.error('Error saving or sharing QR code on native', error);
                        addToast('Fehler beim Speichern des QR-Codes.', 'error');
                    }
                }
                // Reset state to unmount the hidden component
                setDownloadQr(false);
            });
        }
    };

    return (
        <>
            <AdminModal isOpen={isOpen} onClose={onClose} title="Seite teilen">
                <Text style={styles.bodyText}>Teilen Sie diesen Link, um andere direkt auf diese Seite zu führen.</Text>
                
                <View style={styles.linkContainer}>
                    <TextInput
                        style={[styles.input, styles.readOnlyInput]}
                        value={url}
                        editable={false}
                    />
                    <TouchableOpacity style={styles.copyButton} onPress={handleCopyToClipboard}>
                        <Icon name="copy" solid size={18} color={colors.white} />
                    </TouchableOpacity>
                </View>

                {url && (
                    <View style={styles.qrContainer}>
                        <QRCode 
                            value={url} 
                            size={150}
                            backgroundColor="transparent"
                            color={colors.text}
                        />
                    </View>
                )}
                
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleDownloadQrCode} disabled={!url}>
                    <Icon name="download" size={16} color={colors.text} />
                    <Text style={{color: colors.text}}> QR-Code herunterladen</Text>
                </TouchableOpacity>
            </AdminModal>

            {/* Hidden component for generating the larger download QR code */}
            {downloadQr && (
                <View style={{ position: 'absolute', left: -10000, top: 0 }}>
                    <QRCode
                        value={url}
                        size={900}
                        backgroundColor="transparent"
                        color={colors.text}
                        getRef={onQrDownloadRef}
                    />
                </View>
            )}
        </>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        linkContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            marginVertical: spacing.md,
        },
        copyButton: {
            backgroundColor: colors.primary,
            padding: 14,
            borderTopRightRadius: borders.radius,
            borderBottomRightRadius: borders.radius,
        },
        qrContainer: {
            alignItems: 'center',
            padding: spacing.md,
            backgroundColor: colors.white,
            borderRadius: borders.radius,
            borderWidth: 1,
            borderColor: colors.border,
            alignSelf: 'center',
            marginBottom: spacing.md,
        },
    });
};

export default SharePageModal;