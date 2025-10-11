import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const DamageReportModal = ({ isOpen, onClose, onSuccess, item }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [description, setDescription] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		try {
			const result = await apiClient.post(`/public/storage/${item.id}/report-damage`, { description });
			if (result.success) {
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Meldung konnte nicht gesendet werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	const handleClose = () => {
		setDescription('');
		setError('');
		setIsSubmitting(false);
		onClose();
	};

	return (
		<Modal isOpen={isOpen} onClose={handleClose} title={`Schaden für "${item?.name}" melden`}>
			<View>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.bodyText}>Bitte beschreiben Sie den Defekt so genau wie möglich. Ein Administrator wird die Meldung prüfen.</Text>
				<Text style={styles.label}>Beschreibung des Schadens</Text>
				<TextInput
					style={[styles.input, styles.textArea]}
					value={description}
					onChangeText={setDescription}
					multiline
					numberOfLines={5}
					placeholder="z.B. Kabel hat einen Wackelkontakt..."
                    placeholderTextColor={colors.textMuted}
				/>
				<View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 24}}>
					<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleClose} disabled={isSubmitting}>
						<Text style={styles.buttonText}>Abbrechen</Text>
					</TouchableOpacity>
					<TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={handleSubmit} disabled={isSubmitting}>
						{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Schaden melden</Text>}
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

export default DamageReportModal;