import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, StyleSheet } from 'react-native';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import { MultipleSelectList } from 'react-native-dropdown-select-list';
import AdminModal from '../ui/AdminModal';
import BouncyCheckbox from "react-native-bouncy-checkbox";

const TaskModal = ({ isOpen, onClose, onSuccess, event, task, allUsers, categories }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
	const isEditMode = !!task;
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const [formData, setFormData] = useState({
		name: '',
		description: '',
		status: 'LOCKED',
        requiredPersons: 1,
        isImportant: false,
		assignedUserIds: [],
        categoryId: null,
        displayOrder: 0,
	});
    const [selectedDependencies, setSelectedDependencies] = useState(new Set());
    const [dependsOnPrevious, setDependsOnPrevious] = useState(true);

	useEffect(() => {
		if (isOpen) {
			if (isEditMode && task) {
				setFormData({
					name: task.name || '',
					description: task.description || '',
					status: task.status || 'LOCKED',
                    requiredPersons: task.requiredPersons || 1,
                    isImportant: task.isImportant || false,
					assignedUserIds: task.assignedUsers?.map(u => u.id) || [],
                    categoryId: task.categoryId,
                    displayOrder: task.displayOrder || 0,
				});
                setSelectedDependencies(new Set(task.dependsOn?.map(t => t.id) || []));
			} else {
				setFormData({ name: '', description: '', status: 'LOCKED', requiredPersons: 1, isImportant: false, assignedUserIds: [], categoryId: categories?.[0]?.id || null, displayOrder: 0 });
                setSelectedDependencies(new Set());
			}
            setDependsOnPrevious(true);
		}
	}, [task, isEditMode, isOpen, categories]);
    
    const handleChange = (name, value) => {
        setFormData(prev => ({...prev, [name]: value}));
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

        let finalDependencies = Array.from(selectedDependencies);
        if (dependsOnPrevious) {
            const tasksInCategory = event.eventTasks
                .filter(t => t.categoryId === formData.categoryId)
                .sort((a, b) => a.displayOrder - b.displayOrder);
            const currentIndex = tasksInCategory.findIndex(t => t.displayOrder === formData.displayOrder);
            if (currentIndex > 0) {
                finalDependencies.push(tasksInCategory[currentIndex - 1].id);
            }
        }

		const payload = {
			id: task?.id || 0,
			name: formData.name,
			description: formData.description,
			status: formData.status,
            requiredPersons: parseInt(String(formData.requiredPersons), 10) || 1,
            isImportant: formData.isImportant,
			assignedUsers: formData.assignedUserIds.map(id => ({ id })),
			dependsOn: [...new Set(finalDependencies)].map(id => ({ id })),
            categoryId: formData.categoryId,
            displayOrder: parseInt(String(formData.displayOrder), 10) || 0,
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
    const allOtherTasks = event.eventTasks?.filter(t => t.id !== task?.id) || [];

	return (
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={isEditMode ? "Aufgabe bearbeiten" : "Neue Aufgabe erstellen"}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Aufgabe speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Name (Titel)</Text>
            <TextInput style={styles.input} value={formData.name} onChangeText={val => handleChange('name', val)} />

            <Text style={styles.label}>Beschreibung (Markdown unterstützt)</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => handleChange('description', val)} multiline />
            
            <Text style={styles.label}>Kategorie</Text>
            <Picker selectedValue={formData.categoryId} onValueChange={val => handleChange('categoryId', val)}>
                <Picker.Item label="Unkategorisiert" value={null} />
                {categories?.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
            </Picker>

            <Text style={styles.label}>Reihenfolgen-Nummer</Text>
            <TextInput style={styles.input} value={String(formData.displayOrder)} onChangeText={val => handleChange('displayOrder', val)} keyboardType="number-pad"/>

            <View style={{flexDirection: 'row', alignItems: 'center', marginVertical: 16}}>
                 <BouncyCheckbox isChecked={dependsOnPrevious} onPress={(isChecked) => setDependsOnPrevious(isChecked)} />
                <Text>Hängt von vorheriger Aufgabe ab</Text>
            </View>
            
            <Text style={styles.label}>Status</Text>
            <Picker selectedValue={formData.status} onValueChange={val => handleChange('status', val)}>
                <Picker.Item label="Gesperrt" value="LOCKED" />
                <Picker.Item label="Offen" value="OPEN" />
                <Picker.Item label="In Arbeit" value="IN_PROGRESS" />
                <Picker.Item label="Erledigt" value="DONE" />
            </Picker>
            
            <Text style={styles.label}>Benötigte Personen</Text>
            <TextInput style={styles.input} value={String(formData.requiredPersons)} onChangeText={val => handleChange('requiredPersons', val)} keyboardType="number-pad"/>

            <View style={{flexDirection: 'row', alignItems: 'center', marginVertical: 16}}>
                 <BouncyCheckbox isChecked={formData.isImportant} onPress={isChecked => handleChange('isImportant', isChecked)} />
                <Text>Als wichtig markieren</Text>
            </View>

            <Text style={styles.label}>Zugewiesen an</Text>
            <MultipleSelectList 
                setSelected={(val) => handleChange('assignedUserIds', val)} 
                data={userOptions} 
                save="key"
                label="Zugewiesene Mitglieder"
                placeholder="Mitglieder auswählen"
                searchPlaceholder="Suchen"
                boxStyles={styles.input}
                defaultOptions={userOptions.filter(opt => formData.assignedUserIds.includes(opt.key))}
            />
		</AdminModal>
	);
};

const pageStyles = (theme) => StyleSheet.create({});

export default TaskModal;