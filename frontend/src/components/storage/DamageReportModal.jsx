import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';

const DamageReportModal = ({ isOpen, onClose, onSuccess, item }) => {
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
				<Text style={styles.description}>Bitte beschreiben Sie den Defekt so genau wie möglich. Ein Administrator wird die Meldung prüfen.</Text>
				<Text style={styles.label}>Beschreibung des Schadens</Text>
				<TextInput
					style={styles.textArea}
					value={description}
					onChangeText={setDescription}
					multiline
					numberOfLines={5}
					placeholder="z.B. Kabel hat einen Wackelkontakt..."
				/>
				<View style={styles.buttonContainer}>
					<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleClose} disabled={isSubmitting}>
						<Text style={styles.secondaryButtonText}>Abbrechen</Text>
					</TouchableOpacity>
					<TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={handleSubmit} disabled={isSubmitting}>
						{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Schaden melden</Text>}
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const styles = StyleSheet.create({
	errorText: {
		color: '#dc3545',
		marginBottom: 12,
		textAlign: 'center',
	},
	description: {
		marginBottom: 16,
		fontSize: 16,
		color: '#212529',
	},
	label: {
		fontWeight: '500',
		color: '#6c757d',
		marginBottom: 8,
	},
	textArea: {
		borderWidth: 1,
		borderColor: '#dee2e6',
		borderRadius: 6,
		padding: 12,
		textAlignVertical: 'top',
		minHeight: 120,
		fontSize: 16,
	},
	buttonContainer: {
		flexDirection: 'row',
		justifyContent: 'flex-end',
		marginTop: 24,
		gap: 8,
	},
	button: {
		paddingVertical: 10,
		paddingHorizontal: 20,
		borderRadius: 6,
		justifyContent: 'center',
		alignItems: 'center',
	},
	secondaryButton: {
		backgroundColor: '#6c757d',
	},
	dangerButton: {
		backgroundColor: '#dc3545',
	},
	buttonText: {
		color: '#fff',
		fontWeight: '500',
	},
	secondaryButtonText: {
		color: '#fff',
		fontWeight: '500',
	}
});

export default DamageReportModal;