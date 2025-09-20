import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput } from 'react-native';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, typography, spacing, borders } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../../ui/AdminModal';
import { Picker } from '@react-native-picker/picker';
import QRCode from 'react-native-qrcode-svg';
import * as Clipboard from 'expo-clipboard';

const FileShareModal = ({ isOpen, onClose, file }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const linksApiCall = useCallback(() => apiClient.get(`/admin/files/${file.id}/share`), [file.id]);
    const { data: links, loading, error, reload } = useApi(linksApiCall);

    const [accessLevel, setAccessLevel] = useState('PUBLIC');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleCreateLink = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.post(`/admin/files/${file.id}/share`, { accessLevel });
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
    
    const handleDeleteLink = (linkId) => {
        Alert.alert('Link löschen?', 'Dieser Freigabe-Link wird dauerhaft ungültig.', [
            { text: 'Abbrechen', style: 'cancel'},
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/admin/files/share/${linkId}`);
                    if (result.success) {
                        addToast('Link gelöscht.', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }}
        ]);
    };
    
    const getShareUrl = (token) => `${apiClient.getRootUrl()}/api/v1/public/files/share/${token}`;

    const handleCopyToClipboard = async (url) => {
        await Clipboard.setStringAsync(url);
        addToast('Link in die Zwischenablage kopiert!', 'success');
    };

    const renderLinkItem = ({ item }) => {
        const url = getShareUrl(item.token);
        return (
            <View style={styles.linkItem}>
                <View style={styles.qrCodeContainer}>
                    <QRCode value={url} size={80} />
                </View>
                <View style={{flex: 1}}>
                    <Text style={styles.linkLabel}>Zugriff: <Text style={{fontWeight: 'bold'}}>{item.accessLevel}</Text></Text>
                    <Text style={styles.linkLabel}>Erstellt: {new Date(item.createdAt).toLocaleDateString()}</Text>
                    <TouchableOpacity onPress={() => handleCopyToClipboard(url)}>
                        <Text style={styles.urlText} numberOfLines={1}>{url}</Text>
                    </TouchableOpacity>
                </View>
                <TouchableOpacity onPress={() => handleDeleteLink(item.id)}>
                    <Icon name="trash" size={20} color={colors.danger} />
                </TouchableOpacity>
            </View>
        );
    };

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title={`"${file.filename}" freigeben`}>
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
                        ListEmptyComponent={<Text style={styles.emptyText}>Für diese Datei gibt es keine Freigabe-Links.</Text>}
                    />
                )}
            </View>
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
            backgroundColor: 'white',
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

export default FileShareModal;