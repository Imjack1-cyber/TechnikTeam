import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import { getThemeColors } from '../../../styles/theme';

const CourseModal = ({ isOpen, onClose, onSuccess, course }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
	const [formData, setFormData] = useState({
		name: course?.name || '',
		abbreviation: course?.abbreviation || '',
		description: course?.description || '',
	});

	useEffect(() => {
		if (course) {
			setFormData({
				name: course.name,
				abbreviation: course.abbreviation,
				description: course.description,
			});
		} else {
			setFormData({ name: '', abbreviation: '', description: '' });
		}
	}, [course]);

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = course
				? await apiClient.put(`/courses/${course.id}`, formData)
				: await apiClient.post('/courses', formData);

			if (result.success) {
				addToast(`Vorlage erfolgreich ${course ? 'aktualisiert' : 'erstellt'}.`, 'success');
				onSuccess();
			} else {
				throw new Error(result.message);
			}
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
            title={course ? "Vorlage bearbeiten" : "Neue Vorlage"}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
        >
			{error && <Text style={styles.errorText}>{error}</Text>}
			<Text style={styles.label}>Name der Vorlage</Text>
			<TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({ ...formData, name: val })} placeholderTextColor={colors.textMuted} />

			<Text style={styles.label}>Abkürzung</Text>
			<TextInput style={styles.input} value={formData.abbreviation} onChangeText={val => setFormData({ ...formData, abbreviation: val })} placeholderTextColor={colors.textMuted} />

			<Text style={styles.label}>Beschreibung</Text>
			<TextInput
				style={[styles.input, styles.textArea]}
				value={formData.description}
				onChangeText={val => setFormData({ ...formData, description: val })}
				multiline
                placeholderTextColor={colors.textMuted}
			/>
        </AdminModal>
	);
};

export default CourseModal;