import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Image } from 'react-native';
// NOTE: For file picking, a library like react-native-document-picker is required.
// import DocumentPicker from 'react-native-document-picker';
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
        // Placeholder for native file picker logic
        // try {
        //   const res = await DocumentPicker.pickSingle({
        //     type: [DocumentPicker.types.images],
        //   });
        //   setMapImage({ uri: res.uri, name: res.name, type: res.type });
        // } catch (err) {
        //   if (DocumentPicker.isCancel(err)) {
        //     // User cancelled the picker
        //   } else {
        //     throw err;
        //   }
        // }
        addToast("File picker not implemented in this demo.", "info");
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		const data = new FormData();
        const venueData = {
			...formData,
			mapImagePath: venue?.mapImagePath // Preserve existing image path
		};
        
		data.append('venue', JSON.stringify(venueData));

		if (mapImage) {
			// FormData in React Native requires the file object to have uri, name, and type
			data.append('mapImage', {
                uri: mapImage.uri,
                name: mapImage.name,
                type: mapImage.type,
            });
		}

		try {
			const endpoint = venue ? `/admin/venues/${venue.id}` : '/admin/venues';
            // Use PUT for updates. A POST with FormData might not work as expected for updates in some frameworks.
			const method = venue ? 'PUT' : 'POST';
			const result = await apiClient.request(endpoint, { method, body: data, headers: { 'Content-Type': 'multipart/form-data' } });

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
				<Text style={styles.label}>Name des Ortes</Text>
				<TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({...formData, name: val})} />
				<Text style={styles.label}>Adresse (optional)</Text>
				<TextInput style={styles.input} value={formData.address} onChangeText={val => setFormData({...formData, address: val})} />
				<Text style={styles.label}>Notizen</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.notes} onChangeText={val => setFormData({...formData, notes: val})} multiline />
				
                <Text style={styles.label}>Raumplan / Kartenbild (optional)</Text>
                <TouchableOpacity style={[styles.button, styles.secondaryButton, {alignSelf: 'flex-start'}]} onPress={handlePickImage}>
                    <Icon name="image" size={16} color="#fff" />
                    <Text style={styles.buttonText}>Bild auswählen</Text>
                </TouchableOpacity>
                {mapImage && <Text style={{marginTop: 8}}>Ausgewählt: {mapImage.name}</Text>}
                {venue?.mapImagePath && !mapImage && <Text style={{marginTop: 8}}>Aktuelles Bild bleibt erhalten.</Text>}
                
				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 24}]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

export default VenueModal;