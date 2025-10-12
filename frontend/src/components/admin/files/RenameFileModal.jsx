import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import AdminModal from '../../../components/ui/AdminModal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';

const RenameFileModal = ({ isOpen, onClose, onSuccess, file }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [newName, setNewName] = useState(file?.filename || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.put(`/admin/files/${file.id}/rename`, { newName });
            if (result.success) {
                addToast('Datei umbenannt.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title="Datei umbenennen" onSubmit={handleSubmit} isSubmitting={isSubmitting}>
            <Text style={styles.label}>Neuer Dateiname</Text>
            <TextInput style={styles.input} value={newName} onChangeText={setNewName} placeholderTextColor={colors.textMuted} />
        </AdminModal>
    );
};

export default RenameFileModal;