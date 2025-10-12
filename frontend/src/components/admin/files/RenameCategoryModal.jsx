import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import AdminModal from '../../../components/ui/AdminModal';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors } from '../../../styles/theme';

const RenameCategoryModal = ({ isOpen, onClose, onSuccess, category }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const [newName, setNewName] = useState(category?.name || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            const result = await apiClient.put(`/admin/files/categories/${category.id}`, { name: newName });
            if (result.success) {
                addToast('Kategorie umbenannt.', 'success');
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
        <AdminModal isOpen={isOpen} onClose={onClose} title="Kategorie umbenennen" onSubmit={handleSubmit} isSubmitting={isSubmitting}>
             <Text style={styles.label}>Neuer Kategoriename</Text>
            <TextInput style={styles.input} value={newName} onChangeText={setNewName} placeholderTextColor={colors.textMuted}/>
        </AdminModal>
    );
};

export default RenameCategoryModal;