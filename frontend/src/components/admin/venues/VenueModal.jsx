import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Image, Platform } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import apiClient, { MAX_FILE_SIZE_BYTES } from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../../../components/ui/AdminModal';
import { useTransferStore } from '../../../store/transferStore';
import { v4 as uuidv4 } from 'uuid';

const VenueModal = ({ isOpen, onClose, onSuccess, venue }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({ name: '', address: '', notes: '' });
    const [mapImage, setMapImage] = useState(null); // To hold asset
    const { addToast } = useToast();
    const { addTransfer, updateTransfer } = useTransferStore();

    useEffect(() => {
        if (venue) {
            setFormData({ name: venue.name, address: venue.address || '', notes: venue.notes || '' });
        } else {
            setFormData({ name: '', address: '', notes: '' });
        }
        setMapImage(null); // Reset file on modal open
    }, [venue, isOpen]);

    const handlePickImage = async () => {
        try {
            const res = await DocumentPicker.getDocumentAsync({
                type: 'image/*',
            });
            if (res.canceled) {
                return;
            }
            if (res.assets && res.assets[0]) {
                const asset = res.assets[0];
                setMapImage(asset);
            } else {
                throw new Error("Document picker returned an unexpected response.");
            }
        } catch (err) {
            console.error("DocumentPicker Error:", err);
            let errorMessage = "Fehler beim Auswählen der Datei.";
            if (Platform.OS !== 'web' && err.message.includes('permission')) {
                errorMessage = "Fehler beim Auswählen der Datei. Bitte stellen Sie sicher, dass die App die Berechtigung hat, auf Ihre Dateien zuzugreifen.";
            }
            addToast(errorMessage, "error");
        }
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');

        const transferId = mapImage ? uuidv4() : null;
        if (transferId) {
            addTransfer(transferId, mapImage.name, 'upload', mapImage.size);
            onClose();
        }

        const data = new FormData();
        data.append('venue', JSON.stringify(formData));

        if (mapImage) {
            if (Platform.OS === 'web') {
                const response = await fetch(mapImage.uri);
                const blob = await response.blob();
                data.append('mapImage', new File([blob], mapImage.name, { type: mapImage.mimeType }));
            } else {
                data.append('mapImage', {
                    uri: mapImage.uri,
                    name: mapImage.name,
                    type: mapImage.mimeType,
                });
            }
        }

        try {
            const endpoint = venue ? `/admin/venues/${venue.id}` : '/admin/venues';
            
            let result;
            if (transferId) {
                result = await apiClient.uploadWithProgress(endpoint, data, transferId);
            } else {
                result = await apiClient.post(endpoint, data);
            }

            if (result.success) {
                addToast(`Ort erfolgreich ${venue ? 'aktualisiert' : 'erstellt'}.`, 'success');
                if (transferId) updateTransfer(transferId, { status: 'completed' });
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Fehler beim Speichern');
            if (transferId) {
                addToast(`Upload fehlgeschlagen: ${err.message}`, 'error');
                updateTransfer(transferId, { status: 'error' });
            }
        } finally {
            setIsSubmitting(false);
            if (!transferId) onClose();
        }
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={venue ? 'Ort bearbeiten' : 'Neuen Ort erstellen'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <View style={styles.formGroup}>
                <Text style={styles.label}>Name des Ortes</Text>
                <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({ ...formData, name: val })} />
            </View>
            <View style={styles.formGroup}>
                <Text style={styles.label}>Adresse (optional)</Text>
                <TextInput style={styles.input} value={formData.address} onChangeText={val => setFormData({ ...formData, address: val })} />
            </View>
            <View style={styles.formGroup}>
                <Text style={styles.label}>Notizen</Text>
                <TextInput style={[styles.input, styles.textArea]} value={formData.notes} onChangeText={val => setFormData({ ...formData, notes: val })} multiline />
            </View>

            <View style={styles.formGroup}>
                <Text style={styles.label}>Raumplan / Kartenbild (max. {MAX_FILE_SIZE_BYTES / 1024 / 1024} MB)</Text>
                <TouchableOpacity style={[styles.button, styles.secondaryButton, { alignSelf: 'flex-start' }]} onPress={handlePickImage}>
                    <Icon name="image" size={16} />
                    <Text>Bild auswählen</Text>
                </TouchableOpacity>
                {mapImage && (
                    <Text style={[{marginTop: 8}]}>
                        Ausgewählt: {mapImage.name} ({ (mapImage.size / 1024 / 1024).toFixed(2)} MB)
                    </Text>
                )}
                {venue?.mapImagePath && !mapImage && <Text style={{ marginTop: 8 }}>Aktuelles Bild bleibt erhalten.</Text>}
            </View>
        </AdminModal>
    );
};

export default VenueModal;