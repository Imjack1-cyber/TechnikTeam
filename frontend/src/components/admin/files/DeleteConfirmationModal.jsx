import React from 'react';
import { Text } from 'react-native';
import AdminModal from '../../ui/AdminModal';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';

const DeleteConfirmationModal = ({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    confirmText = 'Löschen',
    itemType,
    itemName
}) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

    const finalTitle = title || `${itemType} "${itemName}" löschen?`;
    const finalMessage = message || 'Diese Aktion kann nicht rückgängig gemacht werden.';
    
    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={finalTitle}
            onSubmit={onConfirm}
            submitText={confirmText}
            submitButtonVariant="danger"
        >
            <Text style={styles.bodyText}>{finalMessage}</Text>
        </AdminModal>
    );
};

export default DeleteConfirmationModal;