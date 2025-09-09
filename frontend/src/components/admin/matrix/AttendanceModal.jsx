import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, ActivityIndicator, StyleSheet } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import { Picker } from '@react-native-picker/picker';
import AdminModal from '../../ui/AdminModal';

const AttendanceModal = ({ isOpen, onClose, onSuccess, cellData }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
	const [attended, setAttended] = useState(cellData.attended);
	const [meetingRemarks, setMeetingRemarks] = useState(cellData.remarks || '');
	const [isSubmittingMeeting, setIsSubmittingMeeting] = useState(false);
	const [meetingError, setMeetingError] = useState('');

	const [qualStatus, setQualStatus] = useState(cellData.qualification?.status || 'BESUCHT');
	const [qualDate, setQualDate] = useState(cellData.qualification?.completionDate || new Date().toISOString().split('T')[0]);
	const [qualRemarks, setQualRemarks] = useState(cellData.qualification?.remarks || '');
	const [isSubmittingQual, setIsSubmittingQual] = useState(false);
	const [qualError, setQualError] = useState('');

	const { addToast } = useToast();

	useEffect(() => {
		if (isOpen) {
			setAttended(cellData.attended);
			setMeetingRemarks(cellData.remarks || '');
			setQualStatus(cellData.qualification?.status || 'BESUCHT');
			setQualDate(cellData.qualification?.completionDate || new Date().toISOString().split('T')[0]);
			setQualRemarks(cellData.qualification?.remarks || '');
			setMeetingError('');
			setQualError('');
		}
	}, [isOpen, cellData]);


	const handleMeetingSubmit = async () => {
		setIsSubmittingMeeting(true);
		setMeetingError('');

		const payload = {
			userId: cellData.userId,
			meetingId: cellData.meetingId,
			attended,
			remarks: meetingRemarks,
		};

		try {
			const result = await apiClient.put('/matrix/attendance', payload);
			if (result.success) {
				addToast('Meeting-Teilnahme erfolgreich gespeichert.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setMeetingError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmittingMeeting(false);
		}
	};

	const handleQualificationSubmit = async () => {
		setIsSubmittingQual(true);
		setQualError('');

		const payload = {
			userId: cellData.userId,
			courseId: cellData.courseId,
			status: qualStatus,
			completionDate: qualDate,
			remarks: qualRemarks,
		};

		try {
			const result = await apiClient.put('/matrix/qualification', payload);
			if (result.success) {
				addToast('Qualifikations-Status erfolgreich gespeichert.', 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setQualError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmittingQual(false);
		}
	};

	return (
		<AdminModal
			isOpen={isOpen}
			onClose={onClose}
			title={`Eintrag bearbeiten für: ${cellData.userName}`}
		>
            <View style={styles.card}>
                <Text style={styles.cardTitle}>Teilnahme am Meeting: "{cellData.meetingName}"</Text>
                {meetingError && <Text style={styles.errorText}>{meetingError}</Text>}
                <View style={{flexDirection: 'row', alignItems: 'center', marginVertical: 16}}>
                    <BouncyCheckbox isChecked={attended} onPress={(isChecked) => setAttended(isChecked)} />
                    <Text>Hat am Meeting teilgenommen</Text>
                </View>
                <Text style={styles.label}>Anmerkungen zum Meeting</Text>
                <TextInput style={[styles.input, styles.textArea]} value={meetingRemarks} onChangeText={setMeetingRemarks} multiline/>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleMeetingSubmit} disabled={isSubmittingMeeting}>
                    {isSubmittingMeeting ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Meeting-Teilnahme speichern</Text>}
                </TouchableOpacity>
            </View>

            <View style={[styles.card, {marginTop: 16}]}>
                <Text style={styles.cardTitle}>Gesamt-Qualifikation für Kurs: "{cellData.courseName}"</Text>
                {qualError && <Text style={styles.errorText}>{qualError}</Text>}
                <Text style={styles.label}>Status</Text>
                <Picker selectedValue={qualStatus} onValueChange={setQualStatus}>
                    <Picker.Item label="Besucht" value="BESUCHT" />
                    <Picker.Item label="Absolviert" value="ABSOLVIERT" />
                    <Picker.Item label="Bestanden (Qualifiziert)" value="BESTANDEN" />
                    <Picker.Item label="Nicht Besucht (Eintrag entfernen)" value="NICHT BESUCHT" />
                </Picker>
                <Text style={styles.label}>Anmerkungen zur Qualifikation</Text>
                <TextInput style={[styles.input, styles.textArea]} value={qualRemarks} onChangeText={setQualRemarks} multiline/>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleQualificationSubmit} disabled={isSubmittingQual}>
                    {isSubmittingQual ? <ActivityIndicator color="#fff"/> : <Text style={styles.buttonText}>Qualifikation speichern</Text>}
                </TouchableOpacity>
            </View>
		</AdminModal>
	);
};

export default AttendanceModal;