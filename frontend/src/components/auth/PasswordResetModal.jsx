import React, { useState } from 'react';
import { View, Text, TextInput, ActivityIndicator } from 'react-native';
import AdminModal from '../ui/AdminModal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { useToast } from '../../context/ToastContext';
import apiClient from '../../services/apiClient';

const PasswordResetModal = ({ isOpen, onClose }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const { addToast } = useToast();

    const [usernameOrEmail, setUsernameOrEmail] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [isSubmitted, setIsSubmitted] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = await apiClient.post('/identity-verification/initiate-password-reset', { usernameOrEmail });
            if (result.success) {
                setIsSubmitted(true); // Show the confirmation message
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            // We show a generic message on error to prevent user enumeration
            setIsSubmitted(true);
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const handleClose = () => {
        setUsernameOrEmail('');
        setError('');
        setIsSubmitted(false);
        onClose();
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={handleClose}
            title="Passwort zurücksetzen"
            onSubmit={!isSubmitted ? handleSubmit : handleClose}
            isSubmitting={isSubmitting}
            submitText={isSubmitted ? 'Schließen' : 'Anfrage senden'}
        >
            {isSubmitted ? (
                <Text style={styles.bodyText}>
                    Anfrage zum Zurücksetzen des Passworts wurde gesendet. Wenn ein Konto mit dieser E-Mail/Benutzernamen und einem registrierten Gerät existiert, wurde eine Push-Benachrichtigung zur Bestätigung gesendet.
                </Text>
            ) : (
                <>
                    <Text style={styles.bodyText}>
                        Geben Sie Ihren Benutzernamen oder Ihre E-Mail-Adresse ein. Wenn Sie ein Gerät mit der App registriert haben, erhalten Sie eine Push-Benachrichtigung, um das Zurücksetzen zu bestätigen.
                    </Text>
                    {error && <Text style={styles.errorText}>{error}</Text>}
                    <Text style={styles.label}>Benutzername oder E-Mail</Text>
                    <TextInput
                        style={styles.input}
                        value={usernameOrEmail}
                        onChangeText={setUsernameOrEmail}
                        autoCapitalize="none"
                        keyboardType="email-address"
                        autoComplete="email"
                    />
                </>
            )}
        </AdminModal>
    );
};

export default PasswordResetModal;