import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';

const RepeatMeetingModal = ({ isOpen, onClose, onSuccess, meeting }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [datetime, setDatetime] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/meetings/${meeting.id}/repeat`, {
				meetingDateTime: datetime,
			});
			if (result.success) {
				addToast('Meeting erfolgreich wiederholt.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Erstellen des Wiederholungs-Meetings fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`"${meeting.name}" wiederholen`}>
			<View>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.bodyText}>Geben Sie das neue Datum und die Uhrzeit für dieses wiederholte Meeting an.</Text>
				<View style={styles.formGroup}>
					<Text style={styles.label}>Neues Datum & Uhrzeit</Text>
					<TextInput
						style={styles.input}
						value={datetime}
                        placeholder="JJJJ-MM-TTTHH:MM"
						onChangeText={setDatetime}
					/>
				</View>
				<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Wiederholung erstellen</Text>}
				</TouchableOpacity>
			</View>
		</Modal>
	);
};

export default RepeatMeetingModal;