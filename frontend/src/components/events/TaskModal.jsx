import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, StyleSheet } from 'react-native';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import { MultipleSelectList } from 'react-native-dropdown-select-list';

const TaskModal = ({ isOpen, onClose, onSuccess, event, task, allUsers }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
	const isEditMode = !!task;
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [formData, setFormData] = useState({
		description: '',
		details: '',
		status: 'OFFEN',
		assignedUserIds: [],
	});

	useEffect(() => {
		if (isOpen) {
			if (isEditMode && task) {
				setFormData({
					description: task.description || '',
					details: task.details || '',
					status: task.status || 'OFFEN',
					assignedUserIds: task.assignedUsers?.map(u => u.id) || [],
				});
			} else {
				setFormData({ description: '', details: '', status: 'OFFEN', assignedUserIds: [] });
			}
		}
	}, [task, isEditMode, isOpen]);
    
    const handleChange = (name, value) => {
        setFormData(prev => ({...prev, [name]: value}));
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		const payload = {
			id: task?.id || 0,
			description: formData.description,
			details: formData.details,
			status: formData.status,
			assignedUsers: formData.assignedUserIds.map(id => ({ id })),
			dependsOn: task?.dependsOn || [],
			requiredItems: task?.requiredItems || [],
			requiredKits: task?.requiredKits || [],
			requiredPersons: task?.requiredPersons || 0,
			displayOrder: task?.displayOrder || 0,
		};

		try {
			const result = await apiClient.post(`/events/${event.id}/tasks`, payload);
			if (result.success) {
				addToast(`Aufgabe ${isEditMode ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Speichern fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    const userOptions = allUsers?.map(u => ({ key: u.id, value: u.username })) || [];

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={isEditMode ? "Aufgabe bearbeiten" : "Neue Aufgabe erstellen"}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<Text style={styles.label}>Beschreibung (Titel)</Text>
				<TextInput style={styles.input} value={formData.description} onChangeText={val => handleChange('description', val)} />

				<Text style={styles.label}>Details (Markdown unterstützt)</Text>
				<TextInput style={[styles.input, styles.textArea]} value={formData.details} onChangeText={val => handleChange('details', val)} multiline />
				
                <Text style={styles.label}>Status</Text>
				<Picker selectedValue={formData.status} onValueChange={val => handleChange('status', val)}>
                    <Picker.Item label="Offen" value="OFFEN" />
                    <Picker.Item label="In Arbeit" value="IN_ARBEIT" />
                    <Picker.Item label="Erledigt" value="ERLEDIGT" />
                </Picker>
                
                <Text style={styles.label}>Zugewiesen an</Text>
                <MultipleSelectList 
                    setSelected={(val) => handleChange('assignedUserIds', val)} 
                    data={userOptions} 
                    save="key"
                    label="Zugewiesene Mitglieder"
                    placeholder="Mitglieder auswählen"
                    searchPlaceholder="Suchen"
                    boxStyles={styles.input}
                />

				<TouchableOpacity style={[styles.button, styles.primaryButton, {marginTop: 16}]} onPress={handleSubmit} disabled={isSubmitting}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Aufgabe speichern</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const pageStyles = (theme) => StyleSheet.create({});

export default TaskModal;