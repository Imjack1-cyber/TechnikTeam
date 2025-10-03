import React from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import AdminModal from './AdminModal';

const ConfirmationModal = ({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    confirmText = 'Confirm',
    confirmButtonVariant = 'danger',
    isSubmitting = false
}) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={title}
            onSubmit={onConfirm}
            isSubmitting={isSubmitting}
            submitText={confirmText}
            submitButtonVariant={confirmButtonVariant}
        >
            <Text style={styles.bodyText}>{message}</Text>
        </AdminModal>
    );
};

export default ConfirmationModal;