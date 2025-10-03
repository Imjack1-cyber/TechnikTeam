import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import useAdminData from '../../../hooks/useAdminData';
import AdminModal from '../../ui/AdminModal';
import { parseISO } from 'date-fns';
import { getThemeColors } from '../../../styles/theme';
import DateTimePicker from '../../ui/DateTimePicker';

const MeetingModal = ({ isOpen, onClose, onSuccess, meeting, courseId }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const isEditMode = !!meeting;
    const { users } = useAdminData();
    const { addToast } = useToast();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    
    const [formData, setFormData] = useState({
        name: '',
        meetingDateTime: null,
        endDateTime: null,
        location: '',
        description: '',
        leaderUserId: '',
        maxParticipants: '',
        signupDeadline: null,
    });
    
    useEffect(() => {
        if (isOpen) {
            if (meeting) {
                setFormData({
                    name: meeting.name || '',
                    meetingDateTime: meeting.meetingDateTime ? parseISO(meeting.meetingDateTime) : null,
                    endDateTime: meeting.endDateTime ? parseISO(meeting.endDateTime) : null,
                    location: meeting.location || '',
                    description: meeting.description || '',
                    leaderUserId: meeting.leaderUserId || '',
                    maxParticipants: meeting.maxParticipants?.toString() || '',
                    signupDeadline: meeting.signupDeadline ? parseISO(meeting.signupDeadline) : null,
                });
            } else {
                setFormData({
                    name: '', meetingDateTime: null, endDateTime: null, location: '',
                    description: '', leaderUserId: '', maxParticipants: '', signupDeadline: null,
                });
            }
        }
    }, [meeting, isOpen]);

    const handleChange = (name, value) => setFormData(prev => ({ ...prev, [name]: value }));

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
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={isEditMode ? 'Meeting bearbeiten' : 'Neues Meeting erstellen'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Name</Text>
            <TextInput style={styles.input} value={formData.name} onChangeText={val => handleChange('name', val)} />
            
            <DateTimePicker
                label="Beginn"
                value={formData.meetingDateTime}
                onChange={(date) => handleChange('meetingDateTime', date)}
                mode="datetime"
            />
            
            <DateTimePicker
                label="Ende (optional)"
                value={formData.endDateTime}
                onChange={(date) => handleChange('endDateTime', date)}
                mode="datetime"
            />
            
            <DateTimePicker
                label="Anmeldefrist (optional)"
                value={formData.signupDeadline}
                onChange={(date) => handleChange('signupDeadline', date)}
                mode="datetime"
            />

            <Text style={styles.label}>Leitung</Text>
            <Picker selectedValue={formData.leaderUserId} onValueChange={val => setFormData({...formData, leaderUserId: val})}>
                <Picker.Item label="(Keine)" value="" />
                {users?.map(u => <Picker.Item key={u.id} label={u.username} value={u.id} />)}
            </Picker>
            
            <Text style={styles.label}>Maximale Teilnehmer (leer für unbegrenzt)</Text>
            <TextInput style={styles.input} value={formData.maxParticipants} onChangeText={val => handleChange('maxParticipants', val)} keyboardType="number-pad"/>
        </AdminModal>
    );
};

export default MeetingModal;