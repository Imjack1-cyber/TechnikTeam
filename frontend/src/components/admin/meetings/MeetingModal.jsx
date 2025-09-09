import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Platform } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import useAdminData from '../../../hooks/useAdminData';
import AdminModal from '../ui/AdminModal';
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { format } from 'date-fns';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getThemeColors } from '../../../styles/theme';

const MeetingModal = ({ isOpen, onClose, onSuccess, meeting, courseId }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const isEditMode = !!meeting;
    const { users } = useAdminData();
    const { addToast } = useToast();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [isPickerVisible, setPickerVisible] = useState(false);
    const [pickerTargetField, setPickerTargetField] = useState(null);
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
        if (isOpen) {
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
            } else {
                setFormData({
                    name: '', meetingDateTime: '', endDateTime: '', location: '',
                    description: '', leaderUserId: '', maxParticipants: '', signupDeadline: '',
                });
            }
        }
    }, [meeting, isOpen]);

    const handleChange = (name, value) => setFormData(prev => ({ ...prev, [name]: value }));

    const showPicker = (field) => {
        setPickerTargetField(field);
        setPickerVisible(true);
    };

    const hidePicker = () => setPickerVisible(false);

    const handleConfirmDate = (date) => {
        const formattedDate = format(date, "yyyy-MM-dd'T'HH:mm");
        handleChange(pickerTargetField, formattedDate);
        hidePicker();
    };

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
            
            <Text style={styles.label}>Beginn</Text>
            <View style={{flexDirection: 'row', alignItems: 'center'}}>
                <TextInput style={[styles.input, {flex: 1}]} value={formData.meetingDateTime} onChangeText={val => handleChange('meetingDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM" editable={Platform.OS === 'web'}/>
                <TouchableOpacity onPress={() => showPicker('meetingDateTime')}><Icon name="calendar-alt" size={24} color={colors.primary} style={{marginLeft: 8}}/></TouchableOpacity>
            </View>

            <Text style={styles.label}>Ende</Text>
             <View style={{flexDirection: 'row', alignItems: 'center'}}>
                <TextInput style={[styles.input, {flex: 1}]} value={formData.endDateTime} onChangeText={val => handleChange('endDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM" editable={Platform.OS === 'web'}/>
                <TouchableOpacity onPress={() => showPicker('endDateTime')}><Icon name="calendar-alt" size={24} color={colors.primary} style={{marginLeft: 8}}/></TouchableOpacity>
            </View>
            
            <Text style={styles.label}>Anmeldefrist</Text>
             <View style={{flexDirection: 'row', alignItems: 'center'}}>
                <TextInput style={[styles.input, {flex: 1}]} value={formData.signupDeadline} onChangeText={val => handleChange('signupDeadline', val)} placeholder="JJJJ-MM-TTTHH:MM" editable={Platform.OS === 'web'}/>
                <TouchableOpacity onPress={() => showPicker('signupDeadline')}><Icon name="calendar-alt" size={24} color={colors.primary} style={{marginLeft: 8}}/></TouchableOpacity>
            </View>

            <Text style={styles.label}>Leitung</Text>
            <Picker selectedValue={formData.leaderUserId} onValueChange={val => setFormData({...formData, leaderUserId: val})}>
                <Picker.Item label="(Keine)" value="" />
                {users?.map(u => <Picker.Item key={u.id} label={u.username} value={u.id} />)}
            </Picker>
            
            <Text style={styles.label}>Maximale Teilnehmer (leer für unbegrenzt)</Text>
            <TextInput style={styles.input} value={formData.maxParticipants} onChangeText={val => handleChange('maxParticipants', val)} keyboardType="number-pad"/>

             <DateTimePickerModal
                isVisible={isPickerVisible}
                mode="datetime"
                onConfirm={handleConfirmDate}
                onCancel={hidePicker}
            />
        </AdminModal>
    );
};

export default MeetingModal;