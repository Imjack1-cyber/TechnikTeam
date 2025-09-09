import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, Platform } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { format } from 'date-fns';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getThemeColors } from '../../../styles/theme';

const RepeatMeetingModal = ({ isOpen, onClose, onSuccess, meeting }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [datetime, setDatetime] = useState('');
    const [endDateTime, setEndDateTime] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const [isPickerVisible, setPickerVisible] = useState(false);
    const [pickerTarget, setPickerTarget] = useState('start');

    const showPicker = (target) => {
        setPickerTarget(target);
        setPickerVisible(true);
    };

    const handleConfirmDate = (date) => {
        const formattedDate = format(date, "yyyy-MM-dd'T'HH:mm");
        if (pickerTarget === 'start') {
            setDatetime(formattedDate);
        } else {
            setEndDateTime(formattedDate);
        }
        setPickerVisible(false);
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/meetings/${meeting.id}/repeat`, {
				meetingDateTime: datetime,
                endDateTime: endDateTime || null,
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
				
                <Text style={styles.label}>Neuer Beginn</Text>
                <View style={{flexDirection: 'row', alignItems: 'center'}}>
                    <TextInput 
                        style={[styles.input, {flex: 1}]}
                        value={datetime}
                        onChangeText={setDatetime}
                        placeholder="JJJJ-MM-TTTHH:MM"
                        editable={Platform.OS === 'web'}
                    />
                    <TouchableOpacity onPress={() => showPicker('start')}>
                        <Icon name="calendar-alt" size={24} color={colors.primary} style={{marginLeft: 8}}/>
                    </TouchableOpacity>
                </View>

                <Text style={styles.label}>Neues Ende (optional)</Text>
                <View style={{flexDirection: 'row', alignItems: 'center'}}>
                    <TextInput 
                        style={[styles.input, {flex: 1}]}
                        value={endDateTime}
                        onChangeText={setEndDateTime}
                        placeholder="JJJJ-MM-TTTHH:MM"
                        editable={Platform.OS === 'web'}
                    />
                    <TouchableOpacity onPress={() => showPicker('end')}>
                        <Icon name="calendar-alt" size={24} color={colors.primary} style={{marginLeft: 8}}/>
                    </TouchableOpacity>
                </View>

				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting || !datetime}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Wiederholung erstellen</Text>}
				</TouchableOpacity>

                <DateTimePickerModal
                    isVisible={isPickerVisible}
                    mode="datetime"
                    onConfirm={handleConfirmDate}
                    onCancel={() => setPickerVisible(false)}
                />
			</View>
		</Modal>
	);
};

export default RepeatMeetingModal;