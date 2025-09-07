import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Image } from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';

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
        } else {
            // If no new image is selected but an old one exists, we don't want to lose it.
            // The backend must handle the case where `mapImage` part is absent.
        }

        try {
            const endpoint = venue ? `/admin/venues/${venue.id}` : '/admin/venues';
            // PUT doesn't typically support multipart, Spring Boot handles it with some configuration.
            // Using POST for both create and update with multipart is more reliable.
            const result = await apiClient.request(endpoint, {
                method: venue ? 'PUT' : 'POST',
                body: data,
                headers: { 'Content-Type': 'multipart/form-data' }
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
        <Modal isOpen={isOpen} onClose={onClose} title={venue ? 'Ort bearbeiten' : 'Neuen Ort erstellen'}>
            <ScrollView>
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

                <TouchableOpacity style={[styles.button, styles.primaryButton, { marginTop: 24 }]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
                </TouchableOpacity>
            </ScrollView>
        </Modal>
    );
};

export default VenueModal;