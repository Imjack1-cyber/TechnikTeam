import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, Alert, ActivityIndicator, ScrollView } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import Modal from '../../components/ui/Modal';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import RNPickerSelect from 'react-native-picker-select';

const AchievementKeyDisplay = ({ achievementKey }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = pageStyles(theme);
	const parts = achievementKey.split('_');
	const [trigger, action, condition] = parts;
	return (
		<View style={styles.keyContainer}>
			<Text style={[styles.keyPart, styles.keyTrigger]}>{trigger}</Text>
			<Text style={[styles.keyPart, styles.keyAction]}>{action}</Text>
			{condition && <Text style={[styles.keyPart, styles.keyCondition]}>{condition}</Text>}
		</View>
	);
};

const AchievementModal = ({ isOpen, onClose, onSuccess, achievement }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
	const [achievementKey, setAchievementKey] = useState('');
	const formDataApiCall = useCallback(() => apiClient.get('/form-data/achievements'), []);
	const { data: formData } = useApi(formDataApiCall);
	const [keyParts, setKeyParts] = useState({ trigger: 'EVENT', action: '', condition: '' });
    const [formState, setFormState] = useState({ name: '', description: '', iconClass: '' });

	useEffect(() => {
		if (isOpen) {
			if (achievement) {
                setFormState({ name: achievement.name, description: achievement.description, iconClass: achievement.iconClass });
				setAchievementKey(achievement.achievementKey);
				const parts = achievement.achievementKey.split('_');
				setKeyParts({ trigger: parts[0] || 'EVENT', action: parts[1] || '', condition: parts[2] || '' });
			} else {
                setFormState({ name: '', description: '', iconClass: '' });
				setAchievementKey('EVENT__');
				setKeyParts({ trigger: 'EVENT', action: '', condition: '' });
			}
		}
	}, [achievement, isOpen]);

	const handleKeyPartChange = (part, value) => {
		const newKeyParts = { ...keyParts, [part]: value };
		if (part === 'trigger') newKeyParts.condition = '';
		setKeyParts(newKeyParts);
		setAchievementKey(`${newKeyParts.trigger}_${newKeyParts.action}_${newKeyParts.condition}`.toUpperCase());
	};

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const data = { ...formState, achievementKey };
		try {
			const result = achievement ? await apiClient.put(`/achievements/${achievement.id}`, data) : await apiClient.post('/achievements', data);
			if (result.success) {
				addToast(`Abzeichen ${achievement ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};
    
    const courseOptions = formData?.courses?.map(c => ({ label: c.name, value: c.abbreviation })) || [];

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={achievement ? 'Abzeichen bearbeiten' : 'Neues Abzeichen'}>
			<ScrollView>
				{error && <Text style={styles.errorText}>{error}</Text>}
                <Text style={styles.label}>Name</Text><TextInput style={styles.input} value={formState.name} onChangeText={val => setFormState({...formState, name: val})} />
                <Text style={styles.label}>Schlüssel</Text>
                <View style={styles.keyBuilder}>
                    <RNPickerSelect onValueChange={(val) => handleKeyPartChange('trigger', val)} items={[{ label: 'Event', value: 'EVENT' }, { label: 'Qualifikation', value: 'QUALIFICATION' }]} value={keyParts.trigger} style={pickerSelectStyles} />
                    <RNPickerSelect onValueChange={(val) => handleKeyPartChange('action', val)} items={keyParts.trigger === 'EVENT' ? [{ label: 'Teilnahme', value: 'PARTICIPANT' }, { label: 'Leitung', value: 'LEADER' }] : [{ label: 'Erhalten', value: 'GAINED' }]} value={keyParts.action} style={pickerSelectStyles} placeholder={{ label: 'Aktion...', value: null }} />
                    {keyParts.trigger === 'EVENT' ? <TextInput style={[styles.input, {flex: 1}]} value={keyParts.condition} onChangeText={val => handleKeyPartChange('condition', val)} keyboardType="number-pad" placeholder="Anzahl" /> : <RNPickerSelect onValueChange={(val) => handleKeyPartChange('condition', val)} items={courseOptions} value={keyParts.condition} style={pickerSelectStyles} placeholder={{ label: 'Kurs...', value: null }} /> }
                </View>
                <TextInput style={[styles.input, styles.disabledInput]} value={achievementKey} editable={false} />
                <Text style={styles.label}>Beschreibung</Text><TextInput style={styles.textArea} value={formState.description} onChangeText={val => setFormState({...formState, description: val})} multiline />
                <Text style={styles.label}>Font Awesome Icon-Klasse</Text><TextInput style={styles.input} value={formState.iconClass} onChangeText={val => setFormState({...formState, iconClass: val})} placeholder="z.B. fa-star" />
				<TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isSubmitting}><Text style={styles.buttonText}>{isSubmitting ? 'Speichern...' : 'Speichern'}</Text></TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        keyContainer: { flexDirection: 'row', flexWrap: 'wrap', gap: 4 },
        keyPart: { paddingVertical: 2, paddingHorizontal: 6, borderRadius: 4, borderWidth: 1, overflow: 'hidden'},
        keyTrigger: { backgroundColor: colors.primaryLight, borderColor: colors.primary, color: colors.primary },
        keyAction: { backgroundColor: colors.background, borderColor: colors.border },
        keyCondition: { backgroundColor: colors.background, borderColor: colors.border },
        keyBuilder: { flexDirection: 'row', gap: 8, marginBottom: 8 },
    });
};

const pickerSelectStyles = StyleSheet.create({
  inputIOS: { height: 48, borderWidth: 1, borderColor: '#dee2e6', borderRadius: 6, paddingHorizontal: 12, justifyContent: 'center', flex: 1 },
  inputAndroid: { height: 48, borderWidth: 1, borderColor: '#dee2e6', borderRadius: 6, paddingHorizontal: 12, justifyContent: 'center', flex: 1 },
});