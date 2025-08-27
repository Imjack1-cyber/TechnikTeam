import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';

const WikiPageModal = ({ isOpen, onClose, onSuccess, parentPath }) => {
	const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [fileName, setFileName] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		const fullPath = parentPath ? `${parentPath}/${fileName}` : fileName;

		try {
			const result = await apiClient.post('/wiki', { filePath: fullPath, content: `# ${fileName}\n\nNeue Seite.` });
			if (result.success) {
				addToast('Seite erfolgreich erstellt', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Erstellen der Seite fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Neue Wiki-Seite erstellen">
			<View>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Übergeordneter Pfad</Text>
				<TextInput style={[styles.input, { backgroundColor: '#e9ecef' }]} value={parentPath || '/'} editable={false} />
				<Text style={styles.label}>Dateiname (z.B. `neue-seite.md`)</Text>
				<TextInput style={styles.input} value={fileName} onChangeText={setFileName} autoCapitalize="none" autoCorrect={false} />
				
                <TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Seite erstellen</Text>}
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

export default WikiPageModal;