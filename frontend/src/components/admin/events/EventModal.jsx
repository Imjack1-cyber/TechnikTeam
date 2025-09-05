import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Platform } from 'react-native';
import Modal from '../../ui/Modal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import DynamicSkillRows from './DynamicSkillRows';
import DynamicItemRows from './DynamicItemRows';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';
import DateTimePickerModal from "react-native-modal-datetime-picker";
import { format } from 'date-fns';
import Icon from '@expo/vector-icons/FontAwesome5';

const EventModal = ({ isOpen, onClose, onSuccess, event, adminFormData, checklistTemplates }) => {
	const isEditMode = !!event;
	const { users, courses, storageItems, venues } = adminFormData;
	const [activeTab, setActiveTab] = useState('general');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const [formData, setFormData] = useState({});
	const [skillRows, setSkillRows] = useState([]);
	const [itemRows, setItemRows] = useState([]);
    const [isPickerVisible, setPickerVisible] = useState(false);
    const [pickerTargetField, setPickerTargetField] = useState(null);
    const [templateId, setTemplateId] = useState('');
    const [availabilityPreview, setAvailabilityPreview] = useState({});
	
	useEffect(() => {
		if (isOpen) {
			if (isEditMode && event) {
				setFormData({
					name: event.name || '',
					eventDateTime: event.eventDateTime ? event.eventDateTime.substring(0, 16) : '',
					endDateTime: event.endDateTime ? event.endDateTime.substring(0, 16) : '',
					venueId: event.venueId || '',
					description: event.description || '',
					status: event.status || 'GEPLANT',
					leaderUserId: event.leaderUserId || '',
					reminderMinutes: event.reminderMinutes?.toString() || '0',
				});
				setSkillRows(event.skillRequirements?.length > 0 ? event.skillRequirements : [{ requiredCourseId: '', requiredPersons: 1 }]);
				setItemRows(event.reservedItems?.length > 0 ? event.reservedItems.map(i => ({ itemId: i.id, quantity: i.quantity })) : [{ itemId: '', quantity: 1 }]);
                setTemplateId(event.preflightTemplateId || '');
			} else {
				setFormData({ name: '', eventDateTime: '', endDateTime: '', venueId: '', description: '', status: 'GEPLANT', leaderUserId: '', reminderMinutes: '0'});
				setSkillRows([{ requiredCourseId: '', requiredPersons: 1 }]);
				setItemRows([{ itemId: '', quantity: 1 }]);
                setTemplateId('');
			}
            setAvailabilityPreview({});
		}
	}, [event, isEditMode, isOpen]);
    
    useEffect(() => {
        const checkAvailability = async () => {
            if (templateId && formData.eventDateTime) {
                try {
                    const result = await apiClient.get(`/admin/checklist-templates/${templateId}/apply-preview`);
                    if (result.success) {
                        const previewMap = result.data.reduce((acc, item) => {
                            acc[item.itemId] = { availableQuantity: item.availableQuantity };
                            return acc;
                        }, {});
                        setAvailabilityPreview(previewMap);
                        const newItems = result.data.map(item => ({
                            itemId: item.itemId,
                            quantity: item.requestedQuantity
                        }));
                        setItemRows(newItems);
                        addToast('Materialverfügbarkeit für das Datum geprüft.', 'info');
                    }
                } catch (err) {
                    addToast('Fehler bei der Verfügbarkeitsprüfung.', 'error');
                }
            }
        };
        checkAvailability();
    }, [templateId, formData.eventDateTime, addToast]);


	const handleChange = (name, value) => setFormData(prev => ({...prev, [name]: value}));
    
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

        if (!formData.name.trim() || !formData.eventDateTime.trim()) {
            setError('Name und Beginn dürfen nicht leer sein.');
            setIsSubmitting(false);
            return;
        }

		const data = new FormData();
		const eventData = {
			...formData,
			leaderUserId: formData.leaderUserId ? parseInt(formData.leaderUserId, 10) : 0,
            preflightTemplateId: templateId ? parseInt(templateId, 10) : null,
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
            
            <Text style={styles.label}>Beginn</Text>
            <View style={styles.dateInputContainer}>
                <TextInput style={styles.input} value={formData.eventDateTime} onChangeText={val => handleChange('eventDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM" editable={Platform.OS === 'web'}/>
                <TouchableOpacity onPress={() => Platform.OS !== 'web' && showPicker('eventDateTime')}><Icon name="calendar-alt" size={24} color={colors.primary} /></TouchableOpacity>
            </View>
            
            <Text style={styles.label}>Ende</Text>
            <View style={styles.dateInputContainer}>
                <TextInput style={styles.input} value={formData.endDateTime} onChangeText={val => handleChange('endDateTime', val)} placeholder="JJJJ-MM-TTTHH:MM" editable={Platform.OS === 'web'}/>
                <TouchableOpacity onPress={() => Platform.OS !== 'web' && showPicker('endDateTime')}><Icon name="calendar-alt" size={24} color={colors.primary} /></TouchableOpacity>
            </View>

            <Text style={styles.label}>Ort</Text>
            <Picker selectedValue={formData.venueId} onValueChange={val => handleChange('venueId', val)}>
                <Picker.Item label="-- Veranstaltungsort auswählen --" value="" />
                {venues?.map(v => <Picker.Item key={v.id} label={v.name} value={v.id} />)}
            </Picker>

            <Text style={styles.label}>Beschreibung</Text><TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => handleChange('description', val)} multiline />
        </View>
    );

    const renderDetails = () => (
        <View>
            <Text style={styles.label}>Status</Text>
            <Picker selectedValue={formData.status} onValueChange={val => handleChange('status', val)}><Picker.Item label="Geplant" value="GEPLANT" /><Picker.Item label="Laufend" value="LAUFEND" /><Picker.Item label="Abgeschlossen" value="ABGESCHLOSSEN" /><Picker.Item label="Abgesagt" value="ABGESAGT" /></Picker>
            <Text style={styles.label}>Leitung</Text>
            <Picker selectedValue={formData.leaderUserId} onValueChange={val => handleChange('leaderUserId', val)}><Picker.Item label="(Keine)" value="" />{users?.map(u => <Picker.Item key={u.id} label={u.username} value={u.id} />)}</Picker>
            
            <Text style={[styles.label, {marginTop: 16}]}>Checklisten-Vorlage (Optional)</Text>
            <Picker selectedValue={templateId} onValueChange={setTemplateId}>
                <Picker.Item label="-- Keine Vorlage --" value="" />
                {checklistTemplates.map(t => <Picker.Item key={t.id} label={t.name} value={t.id} />)}
            </Picker>

            <Text style={styles.label}>Personalbedarf</Text>
            <DynamicSkillRows rows={skillRows} setRows={setSkillRows} courses={courses} />
            <Text style={[styles.label, {marginTop: 16}]}>Materialbedarf</Text>
            <DynamicItemRows rows={itemRows} setRows={setItemRows} storageItems={storageItems} availabilityPreview={availabilityPreview} />
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
            <DateTimePickerModal
                isVisible={isPickerVisible}
                mode="datetime"
                onConfirm={handleConfirmDate}
                onCancel={hidePicker}
            />
		</Modal>
	);
};

const pageStyles = (theme) => {
    return StyleSheet.create({
        dateInputContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
        },
    });
};

export default EventModal;