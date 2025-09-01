import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import useAdminData from '../../../hooks/useAdminData';

const MeetingModal = ({ isOpen, onClose, onSuccess, meeting, courseId }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const isEditMode = !!meeting;
    const { users } = useAdminData();
    const { addToast } = useToast();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        name: '',
        meetingDateTime: '',
        endDateTime: '',
        location: '',
        description: '',
        leaderUserId: '',
        maxParticipants: '',
        signupDeadline: '',
    });
    
    useEffect(() => {
        if (meeting) {
            setFormData({
                name: meeting.name || '',
                meetingDateTime: meeting.meetingDateTime?.substring(0, 16) || '',
                endDateTime: meeting.endDateTime?.substring(0, 16) || '',
                location: meeting.location || '',
                description: meeting.description || '',
                leaderUserId: meeting.leaderUserId || '',
                maxParticipants: meeting.maxParticipants?.toString() || '',
                signupDeadline: meeting.signupDeadline?.substring(0, 16) || '',
            });
        }
    }, [meeting, isOpen]);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        const payload = { ...formData, courseId };
        try {
            const result = isEditMode
                ? await apiClient.put(`/meetings/${meeting.id}`, payload)
                : await apiClient.post('/meetings', payload);
            if (result.success) {
                addToast(`Meeting ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Speichern fehlgeschlagen.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? 'Meeting bearbeiten' : 'Neues Meeting erstellen'}>
            <ScrollView>
                {error && <Text style={styles.errorText}>{error}</Text>}
                <Text style={styles.label}>Name</Text>
                <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({...formData, name: val})} />
                <Text style={styles.label}>Beginn</Text>
                <TextInput style={styles.input} value={formData.meetingDateTime} onChangeText={val => setFormData({...formData, meetingDateTime: val})} placeholder="JJJJ-MM-TTTHH:MM" />
                <Text style={styles.label}>Ende</Text>
                <TextInput style={styles.input} value={formData.endDateTime} onChangeText={val => setFormData({...formData, endDateTime: val})} placeholder="JJJJ-MM-TTTHH:MM" />
                <Text style={styles.label}>Leitung</Text>
                <Picker selectedValue={formData.leaderUserId} onValueChange={val => setFormData({...formData, leaderUserId: val})}>
                    <Picker.Item label="(Keine)" value="" />
                    {users?.map(u => <Picker.Item key={u.id} label={u.username} value={u.id} />)}
                </Picker>
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
                </TouchableOpacity>
            </ScrollView>
        </Modal>
    );
};

export default MeetingModal;