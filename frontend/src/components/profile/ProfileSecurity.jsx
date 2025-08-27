import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useToast } from '../../context/ToastContext';
import { useAuthStore } from '../../store/authStore';
import apiClient from '../../services/apiClient';
import Modal from '../ui/Modal';
import TwoFactorAuthSetup from './TwoFactorAuthSetup';
import ProfileActiveSessions from './ProfileActiveSessions';

const ProfileSecurity = ({ onUpdate }) => {
	const navigation = useNavigation();
	const user = useAuthStore(state => state.user);
	const { addToast } = useToast();
	const [is2faModalOpen, setIs2faModalOpen] = useState(false);

	const handleDisable2FA = async () => {
		// In a real app, you would use a dedicated prompt component, not the browser's `prompt`.
		// This is a placeholder for the logic.
		addToast('Diese Funktion erfordert eine native UI für die Code-Eingabe.', 'info');
	};

	const handleSetupComplete = () => {
		setIs2faModalOpen(false);
		onUpdate(); // Reload profile data to get the new 2FA status
	};

	return (
		<>
			<View style={styles.card}>
				<Text style={styles.title}>Sicherheit</Text>

				<TouchableOpacity style={styles.listItem} onPress={() => navigation.navigate('PasswordChange')}>
					<Text style={styles.listItemText}>Passwort ändern</Text>
				</TouchableOpacity>

				<View style={styles.listItem}>
					<Text style={styles.listItemText}>Zwei-Faktor-Authentifizierung (2FA)</Text>
					{user.totpEnabled ? (
						<TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={handleDisable2FA}>
							<Text style={styles.dangerOutlineButtonText}>Deaktivieren</Text>
						</TouchableOpacity>
					) : (
						<TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => setIs2faModalOpen(true)}>
							<Text style={styles.buttonText}>Aktivieren</Text>
						</TouchableOpacity>
					)}
				</View>

				<Text style={styles.passkeyTitle}>Passkeys (Passwortloser Login)</Text>
				<Text style={styles.passkeyDescription}>
					Dieses Feature wird zurzeit überarbeitet und ist in Kürze wieder verfügbar.
				</Text>
				<TouchableOpacity style={[styles.button, styles.successButton, styles.disabledButton]} disabled={true}>
					<Text style={styles.buttonText}>Neues Gerät registrieren</Text>
				</TouchableOpacity>
			</View>
			
			<ProfileActiveSessions />

			<Modal
				isOpen={is2faModalOpen}
				onClose={() => setIs2faModalOpen(false)}
				title="Zwei-Faktor-Authentifizierung einrichten"
			>
				<TwoFactorAuthSetup onSetupComplete={handleSetupComplete} />
			</Modal>
		</>
	);
};

const styles = StyleSheet.create({
	card: {
		backgroundColor: '#ffffff',
		borderRadius: 8,
		padding: 16,
		marginHorizontal: 16,
		marginBottom: 16,
		borderWidth: 1,
		borderColor: '#dee2e6',
	},
	title: {
		fontSize: 20,
		fontWeight: '600',
		color: '#002B5B',
		marginBottom: 12,
	},
	listItem: {
		flexDirection: 'row',
		justifyContent: 'space-between',
		alignItems: 'center',
		paddingVertical: 12,
		borderBottomWidth: 1,
		borderBottomColor: '#f0f0f0',
	},
	listItemText: {
		fontSize: 16,
		color: '#212529',
	},
	button: {
		paddingVertical: 8,
		paddingHorizontal: 12,
		borderRadius: 6,
	},
	buttonText: {
		color: '#fff',
		fontWeight: '500',
	},
	successButton: {
		backgroundColor: '#28a745',
	},
	dangerOutlineButton: {
		backgroundColor: 'transparent',
		borderWidth: 1,
		borderColor: '#dc3545',
	},
	dangerOutlineButtonText: {
		color: '#dc3545',
		fontWeight: '500',
	},
	passkeyTitle: {
		fontSize: 18,
		fontWeight: '600',
		marginTop: 24,
		marginBottom: 8,
	},
	passkeyDescription: {
		color: '#6c757d',
		marginBottom: 16,
	},
	disabledButton: {
		opacity: 0.5,
	},
});


export default ProfileSecurity;