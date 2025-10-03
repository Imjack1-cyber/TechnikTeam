import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, Platform } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import AdminModal from '../../ui/AdminModal';
import DateTimePicker from '../../ui/DateTimePicker';
import { format } from 'date-fns';

const RepeatMeetingModal = ({ isOpen, onClose, onSuccess, meeting }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [datetime, setDatetime] = useState(null);
    const [endDateTime, setEndDateTime] = useState(null);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/meetings/${meeting.id}/repeat`, {
				meetingDateTime: datetime ? format(datetime, "yyyy-MM-dd'T'HH:mm") : null,
                endDateTime: endDateTime ? format(endDateTime, "yyyy-MM-dd'T'HH:mm") : null,
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
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={`"${meeting.name}" wiederholen`}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Wiederholung erstellen"
        >
			<Text style={styles.bodyText}>Geben Sie das neue Datum und die Uhrzeit für dieses wiederholte Meeting an.</Text>
            {error && <Text style={styles.errorText}>{error}</Text>}
            
            <DateTimePicker
                label="Neuer Beginn"
                value={datetime}
                onChange={(date) => setDatetime(date)}
                mode="datetime"
            />

            <DateTimePicker
                label="Neues Ende (optional)"
                value={endDateTime}
                onChange={(date) => setEndDateTime(date)}
                mode="datetime"
            />
		</AdminModal>
	);
};

export default RepeatMeetingModal;