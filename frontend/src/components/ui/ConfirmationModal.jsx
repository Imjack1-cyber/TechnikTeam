import React from 'react';
import { View, Text, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import Modal from './Modal';

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

    const getConfirmButtonVariant = () => {
        switch (confirmButtonVariant) {
            case 'success':
                return styles.successButton;
            case 'primary':
                return styles.primaryButton;
            case 'danger':
            default:
                return styles.dangerButton;
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title={title}>
            <View>
                <Text style={styles.bodyText}>{message}</Text>
                <View style={{ flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 24 }}>
                    <TouchableOpacity
                        style={[styles.button, styles.secondaryButton]}
                        onPress={onClose}
                        disabled={isSubmitting}
                    >
                        <Text style={styles.buttonText}>Cancel</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                        style={[styles.button, getConfirmButtonVariant(), isSubmitting && styles.disabledButton]}
                        onPress={onConfirm}
                        disabled={isSubmitting}
                    >
                        {isSubmitting
                            ? <ActivityIndicator color="#fff" />
                            : <Text style={styles.buttonText}>{confirmText}</Text>
                        }
                    </TouchableOpacity>
                </View>
            </View>
        </Modal>
    );
};

export default ConfirmationModal;