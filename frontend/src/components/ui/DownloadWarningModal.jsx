import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import Modal from './Modal';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing } from '../../styles/theme';

const DownloadWarningModal = ({ isOpen, onClose, onConfirm, file }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

	if (!isOpen || !file) return null;

	return (
		<Modal isOpen={isOpen} onClose={onClose} title="Download-Warnung">
			<View style={styles.container}>
				<Icon name="exclamation-triangle" size={48} color={colors.warning} style={styles.icon} />
				<Text style={styles.title}>Potenziell unsichere Datei</Text>
				<Text style={styles.description}>
					Sie sind im Begriff, die Datei <Text style={{ fontWeight: 'bold' }}>"{file.filename}"</Text> herunterzuladen.
				</Text>
				<Text style={styles.description}>
					Dateien dieses Typs könnten potenziell schädlichen Code enthalten. Öffnen Sie diese Datei nur, wenn Sie der Quelle vertrauen.
				</Text>
				<Text style={styles.question}>
					Möchten Sie den Download fortsetzen?
				</Text>
				<View style={styles.buttonContainer}>
					<TouchableOpacity onPress={onClose} style={[styles.button, { backgroundColor: colors.textMuted }]}>
						<Text style={styles.buttonText}>Abbrechen</Text>
					</TouchableOpacity>
					<TouchableOpacity onPress={onConfirm} style={[styles.button, { backgroundColor: colors.danger }]}>
						<Text style={styles.buttonText}>Ja, herunterladen</Text>
					</TouchableOpacity>
				</View>
			</View>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            alignItems: 'center',
        },
        icon: {
            marginBottom: spacing.md,
        },
        title: {
            fontSize: typography.h3,
            fontWeight: 'bold',
            marginBottom: spacing.sm,
            color: colors.heading,
        },
        description: {
            textAlign: 'center',
            fontSize: typography.body,
            marginBottom: spacing.sm,
            color: colors.text,
        },
        question: {
            fontWeight: 'bold',
            textAlign: 'center',
            fontSize: typography.body,
            marginTop: spacing.md,
            color: colors.text,
        },
        buttonContainer: {
            flexDirection: 'row',
            justifyContent: 'center',
            gap: spacing.md,
            marginTop: spacing.lg,
        },
        button: {
            paddingVertical: 10,
            paddingHorizontal: 20,
            borderRadius: 6,
        },
        buttonText: {
            color: colors.white,
            fontWeight: '500',
        },
    });
};

export default DownloadWarningModal;