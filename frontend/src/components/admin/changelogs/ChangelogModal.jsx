import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, Platform } from 'react-native';
import { useToast } from '../../../context/ToastContext';
import apiClient from '../../../services/apiClient';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import DateTimePicker from '../../ui/DateTimePicker';
import { format, parseISO } from 'date-fns';
import { getThemeColors } from '../../../styles/theme';

const ChangelogModal = ({ isOpen, onClose, onSuccess, changelog }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
	const [formData, setFormData] = useState({
		version: changelog?.version || '',
		releaseDate: changelog?.releaseDate || new Date().toISOString().split('T')[0],
		title: changelog?.title || '',
		notes: changelog?.notes || '',
	});
    const [isDatePickerVisible, setDatePickerVisible] = useState(false);

    useEffect(() => {
        if (changelog) {
            setFormData({ version: changelog.version, releaseDate: changelog.releaseDate, title: changelog.title, notes: changelog.notes });
        } else {
            setFormData({ version: '', releaseDate: new Date().toISOString().split('T')[0], title: '', notes: '' });
        }
    }, [changelog, isOpen]);

    const handleConfirmDate = (date) => {
        setFormData({...formData, releaseDate: format(date, 'yyyy-MM-dd')});
        setDatePickerVisible(false);
    };

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = changelog
				? await apiClient.put(`/admin/changelogs/${changelog.id}`, formData)
				: await apiClient.post('/admin/changelogs', formData);
			if (result.success) {
				addToast(`Changelog ${changelog ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Fehler beim Speichern');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={changelog ? 'Changelog bearbeiten' : 'Neuen Changelog erstellen'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Version (z.B. 2.1.0)</Text>
            <TextInput style={styles.input} value={formData.version} onChangeText={val => setFormData({...formData, version: val})} placeholderTextColor={colors.textMuted}/>
            <Text style={styles.label}>Titel</Text>
            <TextInput style={styles.input} value={formData.title} onChangeText={val => setFormData({...formData, title: val})} placeholderTextColor={colors.textMuted}/>
            
            <Text style={styles.label}>Veröffentlichungsdatum</Text>
            <TouchableOpacity onPress={() => setDatePickerVisible(true)} style={[styles.input, { justifyContent: 'center' }]}>
                <Text style={{color: colors.text}}>{format(parseISO(formData.releaseDate), 'dd.MM.yyyy')}</Text>
            </TouchableOpacity>
            <DateTimePicker
                isVisible={isDatePickerVisible}
                mode="date"
                onConfirm={handleConfirmDate}
                onCancel={() => setDatePickerVisible(false)}
                date={parseISO(formData.releaseDate)}
            />

            <Text style={styles.label}>Anmerkungen (Markdown)</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.notes} onChangeText={val => setFormData({...formData, notes: val})} multiline placeholderTextColor={colors.textMuted}/>
        </AdminModal>
	);
};

export default ChangelogModal;