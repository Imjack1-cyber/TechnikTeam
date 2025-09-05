import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing, typography } from '../../styles/theme';
import { useAuthStore } from '../../store/authStore';

const MaintenancePage = ({ navigation }) => {
    const theme = useAuthStore.getState().theme;
    const commonStyles = getCommonStyles(theme);
    const styles = { ...commonStyles, ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const message = useAuthStore.getState().maintenanceStatus.message || "Wir führen gerade einige Wartungsarbeiten durch. Die Anwendung ist in Kürze wieder für Sie verfügbar.";


	return (
		<SafeAreaView style={styles.container}>
			<View style={styles.contentContainer}>
				<Icon name="tools" size={80} color={colors.primary} style={styles.icon} />
				<Text style={styles.title}>Anwendung im Wartungsmodus</Text>
				<Text style={styles.message}>{message}</Text>
				<Text style={styles.patienceText}>Vielen Dank für Ihre Geduld!</Text>
				<View style={styles.adminSection}>
					<Text style={styles.adminText}>
						Administratoren können sich weiterhin anmelden, um den Wartungsmodus zu deaktivieren.
					</Text>
					<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => navigation.navigate('Login')}>
						<Text style={styles.buttonText}>Zur Login-Seite</Text>
					</TouchableOpacity>
				</View>
			</View>
		</SafeAreaView>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        contentContainer: {
            flex: 1,
            justifyContent: 'center',
            alignItems: 'center',
            padding: spacing.lg,
        },
        icon: {
            marginBottom: spacing.xl,
        },
        title: {
            fontSize: typography.h1,
            fontWeight: 'bold',
            textAlign: 'center',
            marginBottom: spacing.md,
            color: colors.heading,
        },
        message: {
            fontSize: typography.h4,
            textAlign: 'center',
            color: colors.textMuted,
            marginBottom: spacing.sm,
        },
        patienceText: {
            fontSize: typography.body,
            textAlign: 'center',
            color: colors.text,
        },
        adminSection: {
            marginTop: spacing.xl,
            alignItems: 'center',
        },
        adminText: {
            fontSize: typography.small,
            color: colors.textMuted,
            textAlign: 'center',
            marginBottom: spacing.md,
        },
    });
};

export default MaintenancePage;