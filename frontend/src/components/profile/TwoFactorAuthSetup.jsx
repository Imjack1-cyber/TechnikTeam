import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Image, ScrollView } from 'react-native';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Clipboard from '@react-native-clipboard/clipboard';

const TwoFactorAuthSetup = ({ onSetupComplete }) => {
    const [setupData, setSetupData] = useState(null);
    const [verificationCode, setVerificationCode] = useState('');
    const [backupCodes, setBackupCodes] = useState([]);
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { addToast } = useToast();

    useEffect(() => {
        const startSetup = async () => {
            try {
                const result = await apiClient.post('/public/profile/2fa/setup');
                if (result.success) {
                    setSetupData(result.data);
                } else {
                    throw new Error(result.message);
                }
            } catch (err) {
                setError(err.message || 'Setup could not be started.');
            }
        };
        startSetup();
    }, []);

    const handleVerifyAndEnable = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const payload = { secret: setupData.secret, token: verificationCode };
            const result = await apiClient.post('/public/profile/2fa/enable', payload);
            if (result.success) {
                setBackupCodes(result.data.backupCodes);
                setSetupData(null);
            } else {
                throw new Error(result.message);
            }
        } catch (err) {
            setError(err.message || 'Verification failed. Check the code and your device time.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCopyCodes = () => {
        Clipboard.setString(backupCodes.join('\n'));
        addToast('Backup-Codes in die Zwischenablage kopiert!', 'success');
    };

    if (error) {
        return <Text style={styles.errorText}>{error}</Text>;
    }

    if (backupCodes.length > 0) {
        return (
            <View>
                <Text style={styles.successTitle}>2FA erfolgreich aktiviert!</Text>
                <Text style={styles.infoText}>Bitte speichern Sie die folgenden Backup-Codes an einem sicheren Ort.</Text>
                <ScrollView style={styles.codeContainer}>
                    {backupCodes.map(code => <Text key={code} style={styles.codeText}>{code}</Text>)}
                </ScrollView>
                <TouchableOpacity onPress={handleCopyCodes} style={[styles.button, styles.secondaryButton]}>
                    <Text style={styles.secondaryButtonText}>Codes kopieren</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={onSetupComplete} style={[styles.button, styles.primaryButton, { marginTop: 8 }]}>
                    <Text style={styles.buttonText}>Fertig</Text>
                </TouchableOpacity>
            </View>
        );
    }

    if (setupData) {
        return (
            <View>
                <Text style={styles.stepText}>1. Scannen Sie diesen QR-Code mit Ihrer Authenticator-App.</Text>
                <View style={styles.qrContainer}>
                    <Image source={{ uri: setupData.qrCodeDataUri }} style={styles.qrCode} />
                </View>
                <Text style={styles.stepText}>2. Geben Sie den 6-stelligen Code aus Ihrer App ein.</Text>
                <Text style={styles.label}>Verifizierungscode</Text>
                <TextInput
                    style={styles.input}
                    value={verificationCode}
                    onChangeText={setVerificationCode}
                    keyboardType="number-pad"
                    maxLength={6}
                />
                <TouchableOpacity onPress={handleVerifyAndEnable} style={[styles.button, styles.successButton]} disabled={isSubmitting}>
                    {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Aktivieren</Text>}
                </TouchableOpacity>
            </View>
        );
    }

    return <ActivityIndicator size="large" />;
};

const styles = StyleSheet.create({
    errorText: { color: '#dc3545', textAlign: 'center', marginBottom: 16 },
    successTitle: { fontSize: 18, fontWeight: 'bold', color: '#28a745', marginBottom: 8 },
    infoText: { backgroundColor: '#e2f3fe', padding: 12, borderRadius: 6, marginBottom: 12, color: '#0c5460' },
    codeContainer: { backgroundColor: '#f8f9fa', padding: 16, borderRadius: 8, marginVertical: 16, maxHeight: 150 },
    codeText: { fontFamily: 'monospace', fontSize: 16, lineHeight: 24 },
    stepText: { fontSize: 16, marginBottom: 8 },
    qrContainer: { alignItems: 'center', marginVertical: 16 },
    qrCode: { width: 250, height: 250 },
    label: { fontWeight: '500', color: '#6c757d', marginBottom: 8 },
    input: { borderWidth: 1, borderColor: '#dee2e6', borderRadius: 6, padding: 12, fontSize: 18, textAlign: 'center', letterSpacing: 5 },
    button: { paddingVertical: 12, paddingHorizontal: 20, borderRadius: 6, alignItems: 'center', marginTop: 16 },
    primaryButton: { backgroundColor: '#007bff' },
    secondaryButton: { backgroundColor: '#6c757d' },
    successButton: { backgroundColor: '#28a745' },
    buttonText: { color: '#fff', fontWeight: '500' },
    secondaryButtonText: { color: '#fff', fontWeight: '500' },
});

export default TwoFactorAuthSetup;