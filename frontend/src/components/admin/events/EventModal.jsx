import React, { useState, useEffect } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import DynamicSkillRows from './DynamicSkillRows';
import DynamicItemRows from './DynamicItemRows';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';

const EventModal = ({ isOpen, onClose, onSuccess, event, adminFormData, checklistTemplates }) => {
	const isEditMode = !!event;
	const { users, courses, storageItems, venues } = adminFormData;
	const [activeTab, setActiveTab] = useState('general');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const [formData, setFormData] = useState({});
	const [skillRows, setSkillRows] = useState([]);
	const [itemRows, setItemRows] = useState([]);
	
	useEffect(() => {
		if (isOpen) {
			if (isEditMode && event) {
				setFormData({
					name: event.name || '',
					eventDateTime: event.eventDateTime ? event.eventDateTime.substring(0, 16) : '',
					endDateTime: event.endDateTime ? event.endDateTime.substring(0, 16) : '',
					location: event.location || '',
					description: event.description || '',
					status: event.status || 'GEPLANT',
					leaderUserId: event.leaderUserId || '',
					reminderMinutes: event.reminderMinutes?.toString() || '0',
				});
				setSkillRows(event.skillRequirements?.length > 0 ? event.skillRequirements : [{ requiredCourseId: '', requiredPersons: 1 }]);
				setItemRows(event.reservedItems?.length > 0 ? event.reservedItems.map(i => ({ itemId: i.id, quantity: i.quantity })) : [{ itemId: '', quantity: 1 }]);
			} else {
				setFormData({ name: '', eventDateTime: '', endDateTime: '', location: '', description: '', status: 'GEPLANT', leaderUserId: '', reminderMinutes: '0'});
				setSkillRows([{ requiredCourseId: '', requiredPersons: 1 }]);
				setItemRows([{ itemId: '', quantity: 1 }]);
			}
		}
	}, [event, isEditMode, isOpen]);

	const handleChange = (name, value) => setFormData(prev => ({...prev, [name]: value}));

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const data = new FormData(); // FormData for potential file upload
		const eventData = {
			...formData,
			leaderUserId: formData.leaderUserId ? parseInt(formData.leaderUserId, 10) : 0,
			requiredCourseIds: skillRows.map(r => r.requiredCourseId).filter(Boolean),
			requiredPersons: skillRows.map(r => r.requiredPersons).filter(Boolean),
			itemIds: itemRows.map(r => r.itemId).filter(Boolean),
			quantities: itemRows.map(r => r.quantity).filter(Boolean),
		};
		data.append('eventData', new Blob([JSON.stringify(eventData)], { type: 'application/json' }));
		try {
			const result = isEditMode ? await apiClient.post(`/events/${event.id}`, data) : await apiClient.post('/events', data);
			if (result.success) {
				addToast(`Event ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    const renderGeneral = () => (
        <View>
            <Text style={styles.label}>Name</Text><TextInput style={styles.input} value={formData.name} onChangeText={val => handleChange('name', val)} />
            <Text style={styles.label}>Beginn</Text><TextInput style={styles.input} value={formData.eventDateTime} onChangeText={val => handleChange('eventDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM"/>
            <Text style={styles.label}>Ende</Text><TextInput style={styles.input} value={formData.endDateTime} onChangeText={val => handleChange('endDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM"/>
            <Text style={styles.label}>Ort</Text><TextInput style={styles.input} value={formData.location} onChangeText={val => handleChange('location', val)} />
            <Text style={styles.label}>Beschreibung</Text><TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => handleChange('description', val)} multiline />
        </View>
    );

    const renderDetails = () => (
        <View>
            <Text style={styles.label}>Status</Text>
            <Picker selectedValue={formData.status} onValueChange={val => handleChange('status', val)}><Picker.Item label="Geplant" value="GEPLANT" /><Picker.Item label="Laufend" value="LAUFEND" /><Picker.Item label="Abgeschlossen" value="ABGESCHLOSSEN" /><Picker.Item label="Abgesagt" value="ABGESAGT" /></Picker>
            <Text style={styles.label}>Leitung</Text>
            <Picker selectedValue={formData.leaderUserId} onValueChange={val => handleChange('leaderUserId', val)}><Picker.Item label="(Keine)" value="" />{users?.map(u => <Picker.Item key={u.id} label={u.username} value={u.id} />)}</Picker>
            <Text style={styles.label}>Personalbedarf</Text>
            <DynamicSkillRows rows={skillRows} setRows={setSkillRows} courses={courses} />
            <Text style={[styles.label, {marginTop: 16}]}>Materialbedarf</Text>
            <DynamicItemRows rows={itemRows} setRows={setItemRows} storageItems={storageItems} />
        </View>
    );

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Event bearbeiten" : "Neues Event erstellen"}>
			<ScrollView>
				<View style={{flexDirection: 'row', borderBottomWidth: 1, borderColor: getThemeColors(theme).border, marginBottom: 16}}>
                    <TouchableOpacity onPress={() => setActiveTab('general')} style={{paddingBottom: 8, marginRight: 16, borderBottomWidth: activeTab === 'general' ? 3 : 0, borderBottomColor: getThemeColors(theme).primary}}><Text>Allgemein</Text></TouchableOpacity>
                    <TouchableOpacity onPress={() => setActiveTab('details')} style={{paddingBottom: 8, borderBottomWidth: activeTab === 'details' ? 3 : 0, borderBottomColor: getThemeColors(theme).primary}}><Text>Details & Bedarf</Text></TouchableOpacity>
                </View>
				{error && <Text style={styles.errorText}>{error}</Text>}
				
                {activeTab === 'general' && renderGeneral()}
                {activeTab === 'details' && renderDetails()}

				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Event speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

export default EventModal;