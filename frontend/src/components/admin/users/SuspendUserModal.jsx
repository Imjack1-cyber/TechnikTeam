import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import { getThemeColors } from '../../../styles/theme';

const SuspendUserModal = ({ isOpen, onClose, user, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
	const [duration, setDuration] = useState('7d');
	const [reason, setReason] = useState('');
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();

    useEffect(() => {
        if(isOpen) {
            console.log(`SuspendUserModal opened for user: ${user?.username}`);
        }
    }, [isOpen, user]);

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');
		try {
			const result = await apiClient.post(`/admin/users/${user.id}/suspend`, { duration, reason });
			if (result.success) {
				addToast(`Benutzer ${user.username} wurde gesperrt.`, 'success');
				onSuccess();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Sperren fehlgeschlagen.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={`Benutzer sperren: ${user.username}`}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
            submitText="Benutzer sperren"
            submitButtonVariant="danger"
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Dauer (z.B. 1h, 7d, indefinite)</Text>
            <TextInput style={styles.input} value={duration} onChangeText={setDuration} placeholderTextColor={colors.textMuted} />
            <Text style={styles.label}>Grund</Text>
            <TextInput style={[styles.input, styles.textArea]} value={reason} onChangeText={setReason} multiline placeholderTextColor={colors.textMuted} />
        </AdminModal>
	);
};

export default SuspendUserModal;