import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useToast } from '../../../context/ToastContext';
import apiClient from '../../../services/apiClient';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import { getThemeColors } from '../../../styles/theme';

const AnnouncementModal = ({ isOpen, onClose, onSuccess, announcement }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');

    useEffect(() => {
        if(announcement) {
            setTitle(announcement.title);
            setContent(announcement.content);
        } else {
            setTitle('');
            setContent('');
        }
    }, [announcement]);

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		const data = { title, content };

		try {
			const result = announcement
				? await apiClient.put(`/admin/announcements/${announcement.id}`, data)
				: await apiClient.post('/admin/announcements', data);

			if (result.success) {
				addToast(`Mitteilung erfolgreich ${announcement ? 'aktualisiert' : 'erstellt'}.`, 'success');
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
            title={announcement ? "Mitteilung bearbeiten" : "Neue Mitteilung erstellen"}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Speichern"
            submitButtonVariant="primary"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Titel</Text>
            <TextInput style={styles.input} value={title} onChangeText={setTitle} placeholderTextColor={colors.textMuted}/>
            <Text style={styles.label}>Inhalt (Markdown unterstützt)</Text>
            <TextInput style={[styles.input, styles.textArea]} value={content} onChangeText={setContent} multiline placeholderTextColor={colors.textMuted}/>
        </AdminModal>
	);
};

export default AnnouncementModal;