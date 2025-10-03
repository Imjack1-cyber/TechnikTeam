import React, { useState, useCallback, useRef } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, Platform } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from './AdminModal';
import { Picker } from '@react-native-picker/picker';
import QRCode from 'react-native-qrcode-svg';
import Clipboard from '@react-native-clipboard/clipboard';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';
import ConfirmationModal from './ConfirmationModal';


const ShareModal = ({
    isOpen,
    onClose,
    itemType,
    itemId,
    itemName,
    getLinksUrl,
    createLinkUrl,
    deleteLinkUrlPrefix,
    publicUrlPrefix,
}) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const qrCodeRefs = useRef({});
    const [downloadQrConfig, setDownloadQrConfig] = useState(null);

    const linksApiCall = useCallback(() => apiClient.get(getLinksUrl), [getLinksUrl]);
    const { data: links, loading, error, reload } = useApi(linksApiCall);

    const [accessLevel, setAccessLevel] = useState('PUBLIC');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [confirmDeleteId, setConfirmDeleteId] = useState(null);

    const handleCreateLink = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(createLinkUrl, { accessLevel });
            if (result.success) {
                addToast('Neuer Freigabe-Link erstellt.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const handleConfirmDelete = async () => {
        if (!confirmDeleteId) return;
        try {
            const result = await apiClient.delete(`${deleteLinkUrlPrefix}/${confirmDeleteId}`);
            if (result.success) {
                addToast('Link gelöscht.', 'success');
                reload();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setConfirmDeleteId(null);
        }
    };
    
    const getShareUrl = (token) => {
        const baseUrl = apiClient.getRootUrl() || (typeof window !== 'undefined' ? window.location.origin : '');
        return `${baseUrl}${publicUrlPrefix}/${token}`;
    };

    const handleCopyToClipboard = (url) => {
        Clipboard.setString(url);
        addToast('Link in die Zwischenablage kopiert!', 'success');
    };

    const startQrDownload = (linkId) => {
        const link = links.find(l => l.id === linkId);
        if(link) {
            setDownloadQrConfig({
                url: getShareUrl(link.token),
                filename: `${itemType}-${itemId}-share-${linkId}-qrcode.png`
            });
        }
    };

    const onQrDownloadRef = (ref) => {
        if (ref && downloadQrConfig) {
            const { filename } = downloadQrConfig;
            // The toDataURL callback takes the base64 data
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
                        console.error(`Error downloading QR code for ${itemType} on web`, error);
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
                        console.error(`Error saving or sharing QR code for ${itemType} on native`, error);
                        addToast('Fehler beim Speichern des QR-Codes.', 'error');
                    }
                }
                // Reset state to unmount the hidden component
                setDownloadQrConfig(null);
            });
        }
    };

    const renderLinkItem = ({ item }) => {
        const url = getShareUrl(item.token);
        return (
            <View style={styles.linkItem}>
                <View style={styles.qrCodeContainer}>
                    <QRCode 
                        value={url} 
                        size={80} 
                        getRef={c => (qrCodeRefs.current[item.id] = c)}
                        backgroundColor="transparent"
                    />
                </View>
                <View style={{flex: 1}}>
                    <Text style={styles.linkLabel}>Zugriff: <Text style={{fontWeight: 'bold'}}>{item.accessLevel}</Text></Text>
                    <Text style={styles.linkLabel}>Erstellt: {new Date(item.createdAt).toLocaleDateString()}</Text>
                    <TouchableOpacity onPress={() => handleCopyToClipboard(url)}>
                        <Text style={styles.urlText} numberOfLines={1}>{url}</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.secondaryButton, {alignSelf: 'flex-start', marginTop: spacing.sm}]} onPress={() => startQrDownload(item.id)}>
                        <Icon name="download" size={12} color={colors.text} />
                        <Text style={{color: colors.text, fontSize: 12}}> QR-Code</Text>
                    </TouchableOpacity>
                </View>
                <TouchableOpacity onPress={() => setConfirmDeleteId(item.id)}>
                    <Icon name="trash" size={20} color={colors.danger} />
                </TouchableOpacity>
            </View>
        );
    };

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title={`"${itemName}" freigeben`}>
            <View style={styles.newLinkContainer}>
                <Text style={styles.cardTitle}>Neuen Link erstellen</Text>
                <Picker selectedValue={accessLevel} onValueChange={setAccessLevel}>
                    <Picker.Item label="Öffentlich (jeder mit Link)" value="PUBLIC" />
                    <Picker.Item label="Angemeldete Benutzer" value="LOGGED_IN" />
                    <Picker.Item label="Nur Administratoren" value="ADMIN" />
                </Picker>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleCreateLink} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Link generieren</Text>}
                </TouchableOpacity>
            </View>

            <View style={styles.existingLinksContainer}>
                <Text style={styles.cardTitle}>Bestehende Freigabe-Links</Text>
                {loading ? <ActivityIndicator /> : (
                    <FlatList
                        data={links}
                        renderItem={renderLinkItem}
                        keyExtractor={item => item.id.toString()}
                        ListEmptyComponent={<Text style={styles.emptyText}>Für dieses Element gibt es keine Freigabe-Links.</Text>}
                    />
                )}
            </View>

            {confirmDeleteId && (
                <ConfirmationModal
                    isOpen={!!confirmDeleteId}
                    onClose={() => setConfirmDeleteId(null)}
                    onConfirm={handleConfirmDelete}
                    title="Link löschen?"
                    message="Dieser Freigabe-Link wird dauerhaft ungültig. Möchten Sie wirklich fortfahren?"
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                />
            )}

            {/* Hidden component for generating the larger download QR code */}
            {downloadQrConfig && (
                <View style={{ position: 'absolute', left: -10000, top: 0 }}>
                    <QRCode
                        value={downloadQrConfig.url}
                        size={300}
                        backgroundColor="transparent"
                        color={colors.text}
                        getRef={onQrDownloadRef}
                    />
                </View>
            )}
        </AdminModal>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        newLinkContainer: {
            padding: spacing.md,
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: borders.radius,
            marginBottom: spacing.lg,
        },
        existingLinksContainer: {
            flex: 1,
        },
        linkItem: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.md,
            paddingVertical: spacing.md,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        qrCodeContainer: {
            padding: 4,
            backgroundColor: 'white', // A white background for display, but transparent for download
            borderRadius: 4,
        },
        linkLabel: {
            fontSize: typography.small,
            color: colors.textMuted,
        },
        urlText: {
            color: colors.primary,
            textDecorationLine: 'underline',
            marginTop: spacing.xs,
        },
        emptyText: {
            textAlign: 'center',
            color: colors.textMuted,
            marginTop: spacing.md,
        },
    });
};

export default ShareModal;