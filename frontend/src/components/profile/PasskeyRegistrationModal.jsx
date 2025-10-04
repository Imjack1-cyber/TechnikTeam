import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Platform, Alert } from 'react-native';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';
import { passkeyService } from '../../services/passkeyService';

const PasskeyRegistrationModal = ({ isOpen, onClose, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const [deviceName, setDeviceName] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleRegister = async () => {
        if (!deviceName.trim()) {
            setError('Bitte geben Sie einen Namen für das Gerät ein.');
            return;
        }

        setIsLoading(true);
        setError('');

        try {
            const startResult = await apiClient.post('/passkeys/registration/start');
            if (!startResult.success) throw new Error(startResult.message);

            // Use passkeyService to initiate browser/platform WebAuthn registration
            const credential = await passkeyService.startRegistration(startResult.data);
            
            // Send the resulting credential to the backend to finish registration
            const finishResult = await apiClient.post('/passkeys/registration/finish', { 
                credential: credential, 
                deviceName: deviceName.trim() 
            });

            if (finishResult.success) {
                addToast('Passkey erfolgreich registriert!', 'success');
                onSuccess(); // Trigger reload of passkeys on profile page
            } else {
                throw new Error(finishResult.message);
            }
        } catch (err) {
            console.error("Passkey Registration Error:", err);
            setError(err.message || 'Registrierung fehlgeschlagen. Bitte versuchen Sie es erneut.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neuen Passkey registrieren">
            <View>
                <Text style={styles.bodyText}>
                    Registrieren Sie ein neues Gerät (z.B. Ihr Smartphone oder Ihren Computer), um sich passwortlos anmelden zu können.
                </Text>
                {Platform.OS !== 'web' && (
                    <Text style={[styles.bodyText, {color: colors.warning, marginTop: spacing.sm}]}>
                        Hinweis: Diese Funktion ist auf mobilen Apps noch experimentell und erfordert möglicherweise zusätzliche Browser-Apps oder Systemeinstellungen.
                    </Text>
                )}

                {error && <Text style={styles.errorText}>{error}</Text>}

                <Text style={styles.label}>Name des Geräts</Text>
                <TextInput
                    style={styles.input}
                    value={deviceName}
                    onChangeText={setDeviceName}
                    placeholder="z.B. Mein iPhone, Arbeitslaptop"
                    editable={!isLoading}
                />

                <TouchableOpacity 
                    style={[styles.button, styles.primaryButton, { marginTop: spacing.md }]} 
                    onPress={handleRegister} 
                    disabled={isLoading || !deviceName.trim()}
                >
                    {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Registrieren</Text>}
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

const localStyles = StyleSheet.create({
    // Add any specific styles if needed
});

export default PasskeyRegistrationModal;