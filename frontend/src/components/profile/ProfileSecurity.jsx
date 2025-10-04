import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, ActivityIndicator, Alert, Platform, FlatList } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import TwoFactorAuthSetup from './TwoFactorAuthSetup';
import { getCommonStyles } from '../../styles/commonStyles';
import ProfileActiveSessions from './ProfileActiveSessions';
import useApi from '../../hooks/useApi';
import ConfirmationModal from '../ui/ConfirmationModal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import PasskeyRegistrationModal from './PasskeyRegistrationModal';
import { getThemeColors } from '../../styles/theme';

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
    const { addToast } = useToast();
    const [is2faModalOpen, setIs2faModalOpen] = useState(false);
    const [isDisableModalOpen, setIsDisableModalOpen] = useState(false);
    const [isPasskeyRegistrationModalOpen, setIsPasskeyRegistrationModalOpen] = useState(false); // New state for passkey registration modal
    const [deletingPasskey, setDeletingPasskey] = useState(null);
    const [isSubmittingDelete, setIsSubmittingDelete] = useState(false);
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);

    // Fetch passkeys using useApi hook
    const passkeysApiCall = useCallback(() => apiClient.get('/passkeys'), []);
    const { data: passkeys, loading: passkeysLoading, reload: reloadPasskeys } = useApi(passkeysApiCall);

    const handleSetupComplete = () => {
        setIs2faModalOpen(false);
        onUpdate(); // Reload profile data to get the new 2FA status
    };

    const handlePasskeyRegistrationComplete = () => {
        setIsPasskeyRegistrationModalOpen(false);
        reloadPasskeys(); // Reload passkeys list after new registration
    };
    
    const confirmDeletePasskey = async () => {
        if (!deletingPasskey) return;
        setIsSubmittingDelete(true);
        try {
            const result = await apiClient.delete(`/passkeys/${deletingPasskey.id}`);
            if (result.success) {
                addToast('Passkey gelöscht.', 'success');
                reloadPasskeys();
            } else { throw new Error(result.message); }
        } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        finally {
            setIsSubmittingDelete(false);
            setDeletingPasskey(null);
        }
    };

    return (
        <>
            <ProfileActiveSessions onUpdate={onUpdate} />
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

                <View style={[styles.detailsListRow, { borderBottomWidth: 0, flexDirection: 'column', alignItems: 'flex-start' }]}>
                    <View style={{ flexDirection: 'row', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
                        <Text style={styles.detailsListLabel}>Passkeys (Passwortloser Login)</Text>
                        <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => setIsPasskeyRegistrationModalOpen(true)}>
                            <Text style={styles.buttonText}>Gerät registrieren</Text>
                        </TouchableOpacity>
                    </View>
                    <Text style={[styles.subtitle, {marginTop: 8, marginBottom: 8}]}>Registrierte Geräte für den passwortlosen Login.</Text>
                    {passkeysLoading && <ActivityIndicator />}
                    {passkeys && passkeys.length > 0 ? (
                        <FlatList
                            data={passkeys}
                            keyExtractor={item => item.id.toString()}
                            renderItem={({ item }) => (
                                <View style={{flexDirection: 'row', alignItems: 'center', paddingVertical: 4, width: '100%'}}>
                                    <Icon name="key" size={14} style={{marginRight: 8}} />
                                    <Text style={{flex: 1}}>{item.deviceName}</Text>
                                    <Text style={{color: colors.textMuted, marginRight: 16}}>{new Date(item.createdAt).toLocaleDateString()}</Text>
                                    <TouchableOpacity onPress={() => setDeletingPasskey(item)}>
                                        <Icon name="trash" size={16} color={colors.danger} />
                                    </TouchableOpacity>
                                </View>
                            )}
                        />
                    ) : (
                        !passkeysLoading && <Text>Keine Passkeys registriert.</Text>
                    )}
                </View>
            </View>
			
            {/* 2FA Setup Modal */}
			<Modal
				isOpen={is2faModalOpen}
				onClose={() => setIs2faModalOpen(false)}
				title="Zwei-Faktor-Authentifizierung einrichten"
			>
				<TwoFactorAuthSetup onSetupComplete={handleSetupComplete} />
			</Modal>

            {/* Disable 2FA Modal */}
            <Disable2FAModal
                isOpen={isDisableModalOpen}
                onClose={() => setIsDisableModalOpen(false)}
                onSuccess={() => {
                    setIsDisableModalOpen(false);
                    onUpdate();
                }}
            />
            
            {/* Passkey Registration Modal */}
            <PasskeyRegistrationModal
                isOpen={isPasskeyRegistrationModalOpen}
                onClose={() => setIsPasskeyRegistrationModalOpen(false)}
                onSuccess={handlePasskeyRegistrationComplete}
            />

            {/* Passkey Deletion Confirmation Modal */}
            {deletingPasskey && (
                <ConfirmationModal
                    isOpen={!!deletingPasskey}
                    onClose={() => setDeletingPasskey(null)}
                    onConfirm={confirmDeletePasskey}
                    title={`Passkey "${deletingPasskey.deviceName}" löschen?`}
                    message="Dieser Passkey kann danach nicht mehr für den Login verwendet werden."
                    confirmText="Löschen"
                    confirmButtonVariant="danger"
                    isSubmitting={isSubmittingDelete}
                />
            )}
        </>
    );
};


export default ProfileSecurity;