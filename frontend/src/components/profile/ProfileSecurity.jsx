import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, ActivityIndicator, Alert } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import TwoFactorAuthSetup from './TwoFactorAuthSetup';
import { getCommonStyles } from '../../styles/commonStyles';

const Disable2FAModal = ({ isOpen, onClose, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [verificationCode, setVerificationCode] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();

    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = await apiClient.post('/public/profile/2fa/disable', { token: verificationCode });
            if (result.success) {
                addToast('Zwei-Faktor-Authentifizierung erfolgreich deaktiviert.', 'success');
                onSuccess();
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Deaktivierung fehlgeschlagen. Ungültiger Code?');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="2FA Deaktivieren Bestätigen">
            <View>
                <Text style={styles.bodyText}>Um 2FA zu deaktivieren, geben Sie bitte einen aktuellen Code aus Ihrer Authenticator-App ein.</Text>
                {error && <Text style={styles.errorText}>{error}</Text>}
                <Text style={styles.label}>Verifizierungscode</Text>
                <TextInput
                    style={[styles.input, { textAlign: 'center', fontSize: 20, letterSpacing: 5 }]}
                    value={verificationCode}
                    onChangeText={setVerificationCode}
                    keyboardType="number-pad"
                    maxLength={6}
                    placeholder="------"
                />
                <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={handleSubmit} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>2FA Deaktivieren</Text>}
                </TouchableOpacity>
            </View>
        </Modal>
    );
};

const ProfileSecurity = ({ user, onUpdate }) => {
	const navigation = useNavigation();
	const [is2faModalOpen, setIs2faModalOpen] = useState(false);
    const [isDisableModalOpen, setIsDisableModalOpen] = useState(false);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const handleSetupComplete = () => {
		setIs2faModalOpen(false);
		onUpdate(); // Reload profile data to get the new 2FA status
	};

	return (
		<>
			<View style={styles.card}>
				<Text style={styles.cardTitle}>Sicherheit</Text>

				<TouchableOpacity style={styles.detailsListRow} onPress={() => navigation.navigate('PasswordChange')}>
					<Text style={styles.detailsListLabel}>Passwort ändern</Text>
				</TouchableOpacity>

				<View style={styles.detailsListRow}>
					<Text style={styles.detailsListLabel}>Zwei-Faktor-Authentifizierung (2FA)</Text>
					{user.isTotpEnabled ? (
						<TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => setIsDisableModalOpen(true)}>
							<Text style={styles.dangerOutlineButtonText}>Deaktivieren</Text>
						</TouchableOpacity>
					) : (
						<TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => setIs2faModalOpen(true)}>
							<Text style={styles.buttonText}>Aktivieren</Text>
						</TouchableOpacity>
					)}
				</View>

				<View style={[styles.detailsListRow, {borderBottomWidth: 0}]}>
                    <View>
                        <Text style={styles.detailsListLabel}>Passkeys (Passwortloser Login)</Text>
                        <Text style={styles.subtitle}>Dieses Feature wird zurzeit überarbeitet.</Text>
                    </View>
                    <TouchableOpacity style={[styles.button, styles.successButton, styles.disabledButton]} disabled={true}>
                        <Text style={styles.buttonText}>Registrieren</Text>
                    </TouchableOpacity>
                </View>
			</View>
			
			<Modal
				isOpen={is2faModalOpen}
				onClose={() => setIs2faModalOpen(false)}
				title="Zwei-Faktor-Authentifizierung einrichten"
			>
				<TwoFactorAuthSetup onSetupComplete={handleSetupComplete} />
			</Modal>

            <Disable2FAModal
                isOpen={isDisableModalOpen}
                onClose={() => setIsDisableModalOpen(false)}
                onSuccess={() => {
                    setIsDisableModalOpen(false);
                    onUpdate();
                }}
            />
		</>
	);
};


export default ProfileSecurity;