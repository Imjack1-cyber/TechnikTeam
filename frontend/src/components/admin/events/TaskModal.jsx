import React, { useState, useEffect, useRef } from 'react';
import { View, Text, TextInput, TouchableOpacity, ScrollView, ActivityIndicator, StyleSheet } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';
import { MultipleSelectList } from 'react-native-dropdown-select-list';
import AdminModal from '../../ui/AdminModal';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import { getThemeColors } from '../../../styles/theme';
import ConfirmationModal from '../../ui/ConfirmationModal';

const TaskModal = ({ isOpen, onClose, onSuccess, event, task, allUsers, allTasks, categories }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
	const isEditMode = !!task;
	const { addToast } = useToast();
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    const isInitialLoad = useRef(true);

	const [formData, setFormData] = useState({
		name: '',
		description: '',
		status: 'LOCKED',
        requiredPersons: 1,
        isImportant: false,
        categoryId: null,
        displayOrder: 0,
	});
    const [assignedUserIds, setAssignedUserIds] = useState([]);
    const [dependsOnIds, setDependsOnIds] = useState([]);

	useEffect(() => {
		if (isOpen) {
            isInitialLoad.current = true;
			if (isEditMode && task) {
				setFormData({
					name: task.name || '',
					description: task.description || '',
					status: task.status || 'LOCKED',
                    requiredPersons: task.requiredPersons || 1,
                    isImportant: task.isImportant || false,
                    categoryId: task.categoryId,
                    displayOrder: task.displayOrder || 0,
				});
                setAssignedUserIds(task.assignedUsers?.map(u => u.id) || []);
                setDependsOnIds(task.dependsOn?.map(t => t.id) || []);
			} else {
				setFormData({ name: '', description: '', status: 'LOCKED', requiredPersons: 1, isImportant: false, categoryId: categories?.[0]?.id || null, displayOrder: 0 });
                setAssignedUserIds([]);
                setDependsOnIds([]);
			}
		}
	}, [task, isEditMode, isOpen, categories]);
    
    // Auto-update display order based on dependencies
    useEffect(() => {
        if (isInitialLoad.current) {
            isInitialLoad.current = false;
            return;
        }
        const currentDeps = Array.isArray(dependsOnIds) ? dependsOnIds : [];
        if (currentDeps.length > 0) {
            const maxOrder = Math.max(-1, ...currentDeps.map(depId => {
                const dependentTask = allTasks.find(t => t.id === depId);
                return dependentTask ? dependentTask.displayOrder : -1;
            }));
            if (maxOrder >= 0) {
                handleChange('displayOrder', maxOrder + 1);
            }
        }
    }, [dependsOnIds, allTasks]);
    
    const handleChange = (name, value) => {
        setFormData(prev => ({...prev, [name]: value}));
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

        const finalDependencies = Array.isArray(dependsOnIds) ? dependsOnIds : [];
        const finalAssignedUsers = Array.isArray(assignedUserIds) ? assignedUserIds : [];

		const payload = {
			id: task?.id || 0,
			name: formData.name,
			description: formData.description,
			status: formData.status,
            requiredPersons: parseInt(String(formData.requiredPersons), 10) || 1,
            isImportant: formData.isImportant,
			assignedUsers: finalAssignedUsers.map(id => ({ id })),
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

    const handleDelete = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.delete(`/events/${event.id}/tasks/${task.id}`);
            if (result.success) {
                addToast('Aufgabe gelöscht.', 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
            setIsDeleteConfirmOpen(false);
        }
    };
    
    const userOptions = allUsers?.map(u => ({ key: u.id, value: u.username })) || [];
    const dependencyOptions = (allTasks || []).filter(t => t.id !== task?.id).map(t => ({ key: t.id, value: t.name }));

	return (
        <>
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
                <TextInput style={styles.input} value={formData.name} onChangeText={val => handleChange('name', val)} placeholderTextColor={colors.textMuted} />

                <Text style={styles.label}>Beschreibung (Markdown unterstützt)</Text>
                <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => handleChange('description', val)} multiline placeholderTextColor={colors.textMuted} />
                
                <Text style={styles.label}>Kategorie</Text>
                <Picker selectedValue={formData.categoryId} onValueChange={val => handleChange('categoryId', val)} itemStyle={{color: colors.text}}>
                    <Picker.Item label="Unkategorisiert" value={null} />
                    {categories?.map(cat => <Picker.Item key={cat.id} label={cat.name} value={cat.id} />)}
                </Picker>

                <Text style={styles.label}>Reihenfolgen-Nummer</Text>
                <TextInput style={styles.input} value={String(formData.displayOrder)} onChangeText={val => handleChange('displayOrder', val)} keyboardType="number-pad" placeholderTextColor={colors.textMuted}/>

                <Text style={styles.label}>Abhängig von (Tasks, die vorher erledigt sein müssen):</Text>
                <MultipleSelectList 
                    setSelected={(val) => setDependsOnIds(val || [])} 
                    data={dependencyOptions} 
                    save="key"
                    label="Abhängigkeiten"
                    placeholder="Aufgaben auswählen..."
                    searchPlaceholder="Suchen"
                    boxStyles={styles.input}
                    inputStyles={{ color: colors.text }}
                    dropdownTextStyles={{ color: colors.text }}
                    badgeStyles={{backgroundColor: colors.primary}}
                    defaultOptions={dependencyOptions.filter(opt => Array.isArray(dependsOnIds) && dependsOnIds.includes(opt.key))}
                />
                
                <Text style={styles.label}>Status</Text>
                <Picker selectedValue={formData.status} onValueChange={val => handleChange('status', val)} itemStyle={{color: colors.text}}>
                    <Picker.Item label="Gesperrt" value="LOCKED" />
                    <Picker.Item label="Offen" value="OPEN" />
                    <Picker.Item label="In Arbeit" value="IN_PROGRESS" />
                    <Picker.Item label="Erledigt" value="DONE" />
                </Picker>
                
                <Text style={styles.label}>Benötigte Personen</Text>
                <TextInput style={styles.input} value={String(formData.requiredPersons)} onChangeText={val => handleChange('requiredPersons', val)} keyboardType="number-pad" placeholderTextColor={colors.textMuted}/>

                <View style={{flexDirection: 'row', alignItems: 'center', marginVertical: 16}}>
                     <BouncyCheckbox isChecked={formData.isImportant} onPress={isChecked => handleChange('isImportant', isChecked)} fillColor={colors.primary} />
                    <Text style={{color: colors.text}}>Als wichtig markieren</Text>
                </View>

                <Text style={styles.label}>Zugewiesen an</Text>
                <MultipleSelectList 
                    setSelected={(val) => setAssignedUserIds(val || [])} 
                    data={userOptions} 
                    save="key"
                    label="Zugewiesene Mitglieder"
                    placeholder="Mitglieder auswählen"
                    searchPlaceholder="Suchen"
                    boxStyles={styles.input}
                    inputStyles={{ color: colors.text }}
                    dropdownTextStyles={{ color: colors.text }}
                    badgeStyles={{backgroundColor: colors.primary}}
                    defaultOptions={userOptions.filter(opt => Array.isArray(assignedUserIds) && assignedUserIds.includes(opt.key))}
                />
                {isEditMode && (
                    <TouchableOpacity style={[styles.button, styles.dangerButton, {marginTop: 16, alignSelf: 'flex-start'}]} onPress={() => setIsDeleteConfirmOpen(true)}>
                        <Text style={styles.buttonText}>Löschen</Text>
                    </TouchableOpacity>
                )}
            </AdminModal>

            <ConfirmationModal 
                isOpen={isDeleteConfirmOpen}
                onClose={() => setIsDeleteConfirmOpen(false)}
                onConfirm={handleDelete}
                title="Aufgabe löschen?"
                message={`Möchten Sie die Aufgabe "${task?.name}" wirklich endgültig löschen?`}
                confirmText="Löschen"
                confirmButtonVariant="danger"
                isSubmitting={isSubmitting}
            />
        </>
	);
};

const pageStyles = (theme) => StyleSheet.create({});

export default TaskModal;