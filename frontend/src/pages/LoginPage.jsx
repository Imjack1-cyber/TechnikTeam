import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, SafeAreaView, StatusBar, Alert, Platform } from 'react-native';
import { useAuthStore } from '../store/authStore';
import { useToast } from '../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import apiClient from '../services/apiClient';
import { getThemeColors, spacing } from '../styles/theme';
import { passkeyService } from '../services/passkeyService'; // Import the frontend passkey service
import AdminModal from '../components/ui/AdminModal';

const TwoFactorAuthForm = ({ username, preAuthToken, onAuthSuccess }) => {
	const [token, setToken] = useState('');
	const [backupCode, setBackupCode] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const [showRecovery, setShowRecovery] = useState(false);

	const handleSubmit = async () => {
		setIsLoading(true);
		setError('');

		try {
            // Ensure only one code is sent
            const payload = { 
                preAuthToken, 
                token: token.trim() || null, 
                backupCode: backupCode.trim() || null 
            };
			const result = await apiClient.post('/auth/verify-2fa', payload);
			if (result.success && result.data.token && result.data.session) {
				onAuthSuccess(result.data);
			} else {
				throw new Error(result.message || 'Verifizierung fehlgeschlagen.');
			}
		} catch (err) {
			setError(err.message);
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<View>
			<Text style={styles.title}>Zwei-Faktor-Authentifizierung</Text>
			<Text style={styles.subtitle}>Login für <Text style={{fontWeight: 'bold'}}>{username}</Text> von einem neuen Standort. Bitte geben Sie Ihren Code ein.</Text>
			{error && <Text style={styles.errorText}>{error}</Text>}
			
			<Text style={styles.label}>Authenticator-Code</Text>
			<TextInput
				style={styles.input}
				value={token}
				onChangeText={(val) => { setToken(val); setBackupCode(''); }}
				placeholder="6-stelliger Code"
				keyboardType="number-pad"
				maxLength={6}
				autoFocus
			/>
			
            {showRecovery && (
                <>
                    <Text style={[styles.label, {marginTop: 16}]}>oder Backup-Code</Text>
                    <TextInput
                        style={styles.input}
                        value={backupCode}
                        onChangeText={(val) => { setBackupCode(val); setToken(''); }}
                        placeholder="8-stelliger Code"
                    />
                </>
            )}

			<TouchableOpacity style={styles.button} onPress={handleSubmit} disabled={isLoading || (!token && !backupCode)}>
				{isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Bestätigen</Text>}
			</TouchableOpacity>
            
            <TouchableOpacity onPress={() => setShowRecovery(!showRecovery)} style={{marginTop: 16}}>
                <Text style={{color: '#007bff', textAlign: 'center'}}>{showRecovery ? 'Verberge Wiederherstellungsoptionen' : 'Hilfe benötigt?'}</Text>
            </TouchableOpacity>

            {showRecovery && (
                <View style={styles.recoveryInfo}>
                    <Text style={styles.subtitle}>Wenn Sie den Zugriff auf Ihre Authenticator-App verloren haben, verwenden Sie einen Ihrer Backup-Codes oder kontaktieren Sie einen Administrator zur manuellen Zurücksetzung.</Text>
                </View>
            )}
		</View>
	);
};


const LoginPage = ({ navigation }) => {
	const [username, setUsername] = useState('');
	const [password, setPassword] = useState('');
	const [isPasswordVisible, setIsPasswordVisible] = useState(false);
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
    const [isPasskeyInfoModalOpen, setIsPasskeyInfoModalOpen] = useState(false);
    const [isBackendSwitcherOpen, setIsBackendSwitcherOpen] = useState(false);

	const [preAuthToken, setPreAuthToken] = useState(null);

	const { login, backendMode, setBackendMode, completePasskeyLogin } = useAuthStore();
	const { addToast } = useToast();

	const handleAuthSuccess = async (loginData) => {
		const { completeLogin } = useAuthStore.getState();
		await completeLogin(loginData);
		addToast('Erfolgreich angemeldet!', 'success');
	};

	const handleSubmit = async () => {
		setIsLoading(true);
		setError('');
		try {
			const result = await login(username, password);
			if (result.status === '2FA_REQUIRED') {
				setPreAuthToken(result.token);
			} else if (result.status === 'SUCCESS') {
                addToast('Erfolgreich angemeldet!', 'success');
                // Navigator will handle the switch
            }
		} catch (err) {
			setError(err.message || 'Login fehlgeschlagen. Bitte überprüfen Sie Ihre Eingaben.');
		} finally {
			setIsLoading(false);
		}
	};
    
    const handlePasskeyLogin = async () => {
        if (Platform.OS !== 'web' || !navigator.credentials) {
            setIsPasskeyInfoModalOpen(true);
            return;
        }
        if (!username) {
            setError('Bitte geben Sie zuerst Ihren Benutzernamen ein.');
            return;
        }
        setIsLoading(true);
        setError('');
        try {
            const startResult = await apiClient.post('/passkeys/authentication/start', { username });
            if (!startResult.success) throw new Error(startResult.message);
            
            // `startAuthentication` from `passkeyService` expects the raw options object
            const credential = await passkeyService.startAuthentication(startResult.data);

            const finishResult = await apiClient.post('/passkeys/authentication/finish', credential);
            if (finishResult.success && finishResult.data.token && finishResult.data.session) {
                await completePasskeyLogin(finishResult.data);
                addToast('Erfolgreich mit Passkey angemeldet!', 'success');
            } else {
                throw new Error(finishResult.message || 'Passkey-Anmeldung fehlgeschlagen.');
            }
        } catch (err) {
            console.error("Passkey Login Error:", err);
            setError(err.message || 'Passkey-Anmeldung fehlgeschlagen. Stellen Sie sicher, dass Sie den richtigen Benutzernamen eingegeben und einen Passkey für dieses Konto registriert haben.');
        } finally {
            setIsLoading(false);
        }
    };
    
    const handleSwitchBackend = () => {
        setIsBackendSwitcherOpen(true);
    };

	if (preAuthToken) {
		return (
			<SafeAreaView style={styles.container}>
				<View style={styles.loginBox}>
					<TwoFactorAuthForm
						username={username}
						preAuthToken={preAuthToken}
						onAuthSuccess={handleAuthSuccess}
					/>
				</View>
			</SafeAreaView>
		);
	}

	return (
		<SafeAreaView style={styles.container}>
            <StatusBar barStyle="dark-content" />
			<View style={styles.loginBox}>
				<Icon name="bolt" size={40} color="#007bff" style={{ alignSelf: 'center', marginBottom: 8 }} />
				<Text style={styles.title}>TechnikTeam</Text>
				{error && <Text style={styles.errorText}>{error}</Text>}
				<View>
					<Text style={styles.label}>Benutzername</Text>
					<TextInput
						style={styles.input}
						value={username}
						onChangeText={setUsername}
						autoCapitalize="none"
						autoComplete="username"
						editable={!isLoading}
					/>
					<Text style={styles.label}>Passwort</Text>
					<View style={styles.passwordContainer}>
						<TextInput
							style={styles.input}
							value={password}
							onChangeText={setPassword}
							secureTextEntry={!isPasswordVisible}
							autoComplete="password"
							editable={!isLoading}
						/>
						<TouchableOpacity onPress={() => setIsPasswordVisible(!isPasswordVisible)} style={styles.eyeIcon}>
							<Icon name={isPasswordVisible ? 'eye-slash' : 'eye'} size={18} color="#6c757d" />
						</TouchableOpacity>
					</View>
					<TouchableOpacity style={styles.button} onPress={handleSubmit} disabled={isLoading}>
						{isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Anmelden</Text>}
					</TouchableOpacity>
                    {/* New Passkey Login Button */}
                    <TouchableOpacity style={[styles.button, { marginTop: 8, backgroundColor: '#6c757d'}]} onPress={handlePasskeyLogin} disabled={isLoading}>
                        {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Login mit Passkey</Text>}
                    </TouchableOpacity>
				</View>
			</View>
            <View style={styles.backendSwitcher}>
                <Text style={styles.backendText}>
                    Verbunden mit: <Text style={{ fontWeight: 'bold' }}>{backendMode === 'dev' ? 'Development' : 'Production'}</Text>
                </Text>
                <TouchableOpacity onPress={handleSwitchBackend}>
                    <Text style={styles.backendSwitchLink}>Wechseln</Text>
                </TouchableOpacity>
            </View>
            <AdminModal
                isOpen={isPasskeyInfoModalOpen}
                onClose={() => setIsPasskeyInfoModalOpen(false)}
                title="Nicht unterstützt"
                onSubmit={() => setIsPasskeyInfoModalOpen(false)}
                submitText="OK"
            >
                <Text style={styles.bodyText}>
                    Passkey-Login ist derzeit nur im Web-Browser verfügbar oder Ihr Gerät unterstützt es nicht.
                </Text>
            </AdminModal>
            <AdminModal
                isOpen={isBackendSwitcherOpen}
                onClose={() => setIsBackendSwitcherOpen(false)}
                title="Backend wechseln"
            >
                <Text style={styles.bodyText}>Wählen Sie die Zielumgebung aus. Sie werden abgemeldet.</Text>
                <View style={{flexDirection: 'row', justifyContent: 'space-around', marginTop: 24, gap: spacing.sm}}>
                    <TouchableOpacity style={[styles.button, {flex: 1, backgroundColor: '#6c757d'}]} onPress={() => { setBackendMode('dev'); setIsBackendSwitcherOpen(false); }}>
                        <Text style={styles.buttonText}>Development</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, {flex: 1, backgroundColor: '#28a745'}]} onPress={() => { setBackendMode('prod'); setIsBackendSwitcherOpen(false); }}>
                        <Text style={styles.buttonText}>Production</Text>
                    </TouchableOpacity>
                </View>
            </AdminModal>
		</SafeAreaView>
	);
};

const styles = StyleSheet.create({
    container: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 16, backgroundColor: '#f8f9fa' },
    loginBox: { width: '100%', maxWidth: 400, padding: 24, backgroundColor: '#ffffff', borderRadius: 8, shadowColor: "#000", shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4, elevation: 5 },
    title: { fontSize: 24, fontWeight: '700', textAlign: 'center', marginBottom: 24, color: '#002B5B' },
    subtitle: { fontSize: 16, textAlign: 'center', marginBottom: 16, color: '#6c757d' },
    bodyText: { fontSize: 16, color: '#212529', lineHeight: 24 },
    errorText: { color: '#dc3545', marginBottom: 16, textAlign: 'center' },
    label: { marginBottom: 8, fontWeight: '500', color: '#6c757d' },
    input: { width: '100%', height: 48, borderWidth: 1, borderColor: '#dee2e6', borderRadius: 6, paddingHorizontal: 12, marginBottom: 16, backgroundColor: '#fff' },
    passwordContainer: { position: 'relative', justifyContent: 'center' },
    eyeIcon: { position: 'absolute', right: 12, padding: 4 },
    button: { backgroundColor: '#007bff', paddingVertical: 12, borderRadius: 6, alignItems: 'center', justifyContent: 'center', height: 48 },
    buttonText: { color: '#fff', fontWeight: '500', fontSize: 16 },
    recoveryInfo: {
        marginTop: 16,
        padding: 12,
        backgroundColor: '#f8f9fa',
        borderRadius: 6,
    },
    backendSwitcher: {
        position: 'absolute',
        bottom: 20,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        padding: 8,
        backgroundColor: 'rgba(255, 255, 255, 0.8)',
        borderRadius: 6,
    },
    backendText: {
        color: '#6c757d',
    },
    backendSwitchLink: {
        color: '#007bff',
        fontWeight: 'bold',
    },
});

export default LoginPage;