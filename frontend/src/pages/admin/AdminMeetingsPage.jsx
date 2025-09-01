import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Alert, ActivityIndicator, TextInput, ScrollView } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import RNPickerSelect from 'react-native-picker-select';
import RepeatMeetingModal from '../../components/admin/meetings/RepeatMeetingModal';
import MeetingModal from '../../components/admin/meetings/MeetingModal';

const AdminMeetingsPage = () => {
    const route = useRoute();
    const navigation = useNavigation();
    const { courseId } = route.params;
	const meetingsApiCall = useCallback(() => apiClient.get(`/meetings?courseId=${courseId}`), [courseId]);
	const { data: meetingsData, loading, error, reload } = useApi(meetingsApiCall);
    const { addToast } = useToast();
    const [repeatingMeeting, setRepeatingMeeting] = useState(null);
    const [editingMeeting, setEditingMeeting] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const courseName = meetingsData?.[0]?.parentCourseName || 'Lehrgang';
    
    const openModal = (meeting = null) => {
        setEditingMeeting(meeting);
        setIsModalOpen(true);
    };

	const handleDelete = (meeting) => {
        Alert.alert(`Meeting '${meeting.name}' löschen?`, "Diese Aktion kann nicht rückgängig gemacht werden.", [
			{ text: 'Abbrechen', style: 'cancel' },
			{ text: 'Löschen', style: 'destructive', onPress: async () => {
				try {
					const result = await apiClient.delete(`/meetings/${meeting.id}`);
					if (result.success) {
						addToast('Meeting gelöscht.', 'success');
						reload();
					} else { throw new Error(result.message); }
				} catch (err) { addToast(`Löschen fehlgeschlagen: ${err.message}`, 'error'); }
			}},
		]);
	};
    
    const handleSuccess = () => {
        setRepeatingMeeting(null);
        setIsModalOpen(false);
        setEditingMeeting(null);
        reload();
    };

    const renderItem = ({ item: meeting }) => (
        <View style={styles.card}>
            <TouchableOpacity onPress={() => navigation.navigate('MeetingDetails', { meetingId: meeting.id })}>
                <Text style={styles.cardTitle}>{meeting.name}</Text>
            </TouchableOpacity>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Datum:</Text>
                <Text style={styles.value}>{new Date(meeting.meetingDateTime).toLocaleString('de-DE')}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Teilnehmer:</Text>
                <Text style={styles.value}>{meeting.participantCount || 0} / {meeting.maxParticipants || '∞'}</Text>
            </View>
            <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16}}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(meeting)}><Text style={styles.buttonText}>Bearbeiten</Text></TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => setRepeatingMeeting(meeting)}>
                    <Text style={styles.buttonText}>Wiederholen</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(meeting)}>
                    <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<Text style={styles.title}>Meetings für "{courseName}"</Text>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={meetingsData}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={styles.contentContainer}
                ListEmptyComponent={<View style={styles.card}><Text>Keine Meetings für diesen Lehrgang geplant.</Text></View>}
            />
            {repeatingMeeting && (
                <RepeatMeetingModal
                    isOpen={!!repeatingMeeting}
                    onClose={() => setRepeatingMeeting(null)}
                    onSuccess={handleSuccess}
                    meeting={repeatingMeeting}
                />
            )}
            {isModalOpen && (
                <MeetingModal
                    isOpen={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    onSuccess={handleSuccess}
                    meeting={editingMeeting}
                    courseId={courseId}
                />
            )}
		</View>
	);
};

export default AdminMeetingsPage;