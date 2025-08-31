
import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';

const PasswordPage = () => {
    const navigation = useNavigation();
	const [currentPassword, setCurrentPassword] = useState('');
	const [newPassword, setNewPassword] = useState('');
	const [confirmPassword, setConfirmPassword] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const handleSubmit = async () => {
		setError('');
		if (newPassword !== confirmPassword) {
			setError('Die neuen Passwörter stimmen nicht überein.');
			return;
		}
		setIsLoading(true);
		try {
			const result = await apiClient.put('/public/profile/password', { currentPassword, newPassword, confirmPassword });
			if (result.success) {
				addToast('Ihr Passwort wurde erfolgreich geändert.', 'success');
				setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
                navigation.goBack();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || 'Ein Fehler ist aufgetreten.');
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
            <View style={styles.card}>
                <Text style={styles.title}>Passwort ändern</Text>
                <Text style={styles.subtitle}>Das neue Passwort muss den Sicherheitsrichtlinien entsprechen.</Text>

                {error && <Text style={styles.errorText}>{error}</Text>}

                <Text style={styles.label}>Aktuelles Passwort</Text>
                <TextInput style={styles.input} value={currentPassword} onChangeText={setCurrentPassword} secureTextEntry />
                
                <Text style={styles.label}>Neues Passwort</Text>
                <TextInput style={styles.input} value={newPassword} onChangeText={setNewPassword} secureTextEntry />
                
                <Text style={styles.label}>Neues Passwort bestätigen</Text>
                <TextInput style={styles.input} value={confirmPassword} onChangeText={setConfirmPassword} secureTextEntry />
                
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isLoading}>
                    {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Passwort speichern</Text>}
                </TouchableOpacity>
            </View>
		</ScrollView>
	);
};

export default PasswordPage;