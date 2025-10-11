import React, { useState } from 'react';
import { View, Text, TextInput, StyleSheet, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { getThemeColors } from '../styles/theme';

const PasswordPage = () => {
    const navigation = useNavigation();
	const [currentPassword, setCurrentPassword] = useState('');
	const [newPassword, setNewPassword] = useState('');
	const [confirmPassword, setConfirmPassword] = useState('');
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
    const [isCurrentPasswordVisible, setIsCurrentPasswordVisible] = useState(false);
    const [isNewPasswordVisible, setIsNewPasswordVisible] = useState(false);
    const [isConfirmPasswordVisible, setIsConfirmPasswordVisible] = useState(false);
	const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

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
                <View style={styles.passwordContainer}>
                    <TextInput 
                        style={[styles.input, { paddingRight: 40 }]} 
                        value={currentPassword} 
                        onChangeText={setCurrentPassword} 
                        secureTextEntry={!isCurrentPasswordVisible} 
                    />
                    <TouchableOpacity style={styles.eyeIcon} onPress={() => setIsCurrentPasswordVisible(!isCurrentPasswordVisible)}>
                        <Icon name={isCurrentPasswordVisible ? 'eye-slash' : 'eye'} size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                </View>
                
                <Text style={styles.label}>Neues Passwort</Text>
                <View style={styles.passwordContainer}>
                    <TextInput 
                        style={[styles.input, { paddingRight: 40 }]} 
                        value={newPassword} 
                        onChangeText={setNewPassword} 
                        secureTextEntry={!isNewPasswordVisible} 
                    />
                    <TouchableOpacity style={styles.eyeIcon} onPress={() => setIsNewPasswordVisible(!isNewPasswordVisible)}>
                        <Icon name={isNewPasswordVisible ? 'eye-slash' : 'eye'} size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                </View>
                
                <Text style={styles.label}>Neues Passwort bestätigen</Text>
                <View style={styles.passwordContainer}>
                    <TextInput 
                        style={[styles.input, { paddingRight: 40 }]} 
                        value={confirmPassword} 
                        onChangeText={setConfirmPassword} 
                        secureTextEntry={!isConfirmPasswordVisible} 
                    />
                    <TouchableOpacity style={styles.eyeIcon} onPress={() => setIsConfirmPasswordVisible(!isConfirmPasswordVisible)}>
                        <Icon name={isConfirmPasswordVisible ? 'eye-slash' : 'eye'} size={18} color={colors.textMuted} />
                    </TouchableOpacity>
                </View>
                
                <TouchableOpacity style={[styles.button, styles.primaryButton]} onPress={handleSubmit} disabled={isLoading}>
                    {isLoading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Passwort speichern</Text>}
                </TouchableOpacity>
            </View>
		</ScrollView>
	);
};

const pageStyles = (theme) => {
    return StyleSheet.create({
        passwordContainer: {
            position: 'relative',
            justifyContent: 'center',
        },
        eyeIcon: {
            position: 'absolute',
            right: 0,
            height: '100%',
            width: 40,
            justifyContent: 'center',
            alignItems: 'center',
            paddingBottom: 16, // Adjust for marginBottom of input
        },
    });
};

export default PasswordPage;