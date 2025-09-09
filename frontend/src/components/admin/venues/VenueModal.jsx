import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Image } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../../ui/AdminModal';

const VenueModal = ({ isOpen, onClose, onSuccess, venue }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({ name: '', address: '', notes: '' });
    const [mapImage, setMapImage] = useState(null); // To hold file picker result
    const { addToast } = useToast();

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
            if (!res.canceled) {
                setMapImage(res.assets[0]);
            }
        } catch (err) {
            addToast("Fehler beim Auswählen der Datei.", "error");
        }
    };

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');

        const data = new FormData();

        data.append('venue', JSON.stringify(formData));

        if (mapImage) {
            // FormData in React Native requires the file object to have uri, name, and type
            data.append('mapImage', {
                uri: mapImage.uri,
                name: mapImage.name,
                type: mapImage.mimeType,
            });
        }

        try {
            const endpoint = venue ? `/admin/venues/${venue.id}` : '/admin/venues';
            const method = 'POST'; // Use POST for both create and update with multipart for reliability
            const result = await apiClient.request(endpoint, {
                method: method,
                body: data,
                // The Content-Type header is intentionally NOT set here.
                // The fetch API will automatically set it to 'multipart/form-data'
                // with the correct boundary when the body is a FormData object.
            });

            if (result.success) {
                addToast(`Ort erfolgreich ${venue ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Fehler beim Speichern');
        } finally {
            setIsSubmitting(false);
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
                <Text style={styles.label}>Raumplan / Kartenbild (optional)</Text>
                <TouchableOpacity style={[styles.button, styles.secondaryButton, { alignSelf: 'flex-start' }]} onPress={handlePickImage}>
                    <Icon name="image" size={16} />
                    <Text>Bild auswählen</Text>
                </TouchableOpacity>
                {mapImage && <Text style={{ marginTop: 8 }}>Ausgewählt: {mapImage.name}</Text>}
                {venue?.mapImagePath && !mapImage && <Text style={{ marginTop: 8 }}>Aktuelles Bild bleibt erhalten.</Text>}
            </View>
        </AdminModal>
    );
};

export default VenueModal;