import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';

const KitModal = ({ isOpen, onClose, onSuccess, kit }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const isEditMode = !!kit;
	const [formData, setFormData] = useState({
		name: kit?.name || '',
		description: kit?.description || '',
		location: kit?.location || ''
	});
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleChange = (name, value) => {
		setFormData({ ...formData, [name]: value });
	};

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		try {
			const result = isEditMode
				? await apiClient.put(`/kits/${kit.id}`, formData)
				: await apiClient.post('/kits', formData);

			if (result.success) {
				addToast(`Kit erfolgreich ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Kit bearbeiten" : "Neues Kit anlegen"}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<View style={styles.formGroup}>
					<Text style={styles.label}>Name des Kits</Text>
					<TextInput style={styles.input} value={formData.name} onChangeText={(val) => handleChange('name', val)} />
				</View>
				<View style={styles.formGroup}>
					<Text style={styles.label}>Beschreibung</Text>
					<TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={(val) => handleChange('description', val)} multiline />
				</View>
				<View style={styles.formGroup}>
					<Text style={styles.label}>Physischer Standort des Kits</Text>
					<TextInput style={styles.input} value={formData.location} onChangeText={(val) => handleChange('location', val)} placeholder="z.B. Lager, Schrank 3, Fach A" />
				</View>
				<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

export default KitModal;