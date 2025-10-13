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
        const trimmed = (deviceName || '').trim();
        if (!trimmed) {
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
                deviceName: trimmed 
            });

            if (finishResult.success) {
                addToast('Passkey erfolgreich registriert!', 'success');
                onSuccess(); // Trigger reload of passkeys on profile page
            } else {
                throw new Error(finishResult.message);
            }
        } catch (err) {
            console.error("Passkey Registration Error:", err);
            setError(passkeyService.getFriendlyPasskeyErrorMessage(err));
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Neuen Passkey registrieren">
            <View>
                <Text style={styles.bodyText}>
                    Registrieren Sie dieses Gerät (z.B. Ihr Smartphone oder Ihren Computer), um sich zukünftig schnell und sicher ohne Passwort anzumelden.
                </Text>
                <Text style={[styles.bodyText, { fontSize: 12, color: colors.textMuted, marginTop: spacing.sm}]}>
                    Ein Passkey nutzt die biometrischen Daten (Face ID, Fingerabdruck) oder die PIN Ihres Geräts für den Login.
                </Text>
                
                {error && <Text style={styles.errorText}>{error}</Text>}

                <Text style={styles.label}>Name des Geräts</Text>
                <TextInput
                    style={styles.input}
                    value={deviceName}
                    // Use an explicit wrapper so we always set a string value (prevents unexpected event objects)
                    onChangeText={(text) => {
                        try {
                            setDeviceName(text == null ? '' : String(text));
                        } catch (e) {
                            // Fallback to empty string on any unexpected input
                            setDeviceName('');
                        }
                    }}
                    placeholder="z.B. Mein iPhone, Arbeitslaptop"
                    editable={!isLoading}
                    placeholderTextColor={colors.textMuted || '#999'}
                />

                <TouchableOpacity 
                    style={[styles.button, styles.primaryButton, { marginTop: spacing.md }]} 
                    onPress={handleRegister} 
                    disabled={isLoading || !(deviceName && deviceName.trim())}
                >
                    {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Registrierung starten</Text>}
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

export default PasskeyRegistrationModal;