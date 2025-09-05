import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView, ScrollView, Platform } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import * as Updates from 'expo-updates';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';

const SimpleHeader = ({ title }) => {
    const theme = useAuthStore.getState().theme;
    const colors = getThemeColors(theme);
    return (
        <View style={{
            height: 60,
            backgroundColor: colors.surface,
            borderBottomWidth: 1,
            borderBottomColor: colors.border,
            justifyContent: 'center',
            alignItems: 'center',
            width: '100%',
            paddingTop: spacing.sm
        }}>
            <Text style={{ fontSize: typography.h3, color: colors.heading, fontWeight: 'bold' }}>{title}</Text>
        </View>
    );
};


const ErrorPage = ({ error: propError }) => {
    const defaultError = { message: "Ein unbekannter Fehler ist aufgetreten." };
	const error = propError || defaultError;
	console.error(error);
    const theme = useAuthStore(state => state.theme);
    const pageStyles = styles(theme);
    const colors = getThemeColors(theme);

	const errorMessage = error?.message || "Ein unbekannter Fehler ist aufgetreten.";
    const errorStack = error?.stack || "Kein Stacktrace verfügbar.";

    return (
        <SafeAreaView style={pageStyles.fullScreenContainer}>
            <SimpleHeader title="Systemfehler" />
            <View style={pageStyles.content}>
                <Icon name="bug" size={80} color={colors.danger} style={pageStyles.icon} />
                <Text style={pageStyles.title}>Fehler 500 - Interner Fehler</Text>
                <Text style={pageStyles.message}>Ein unerwarteter Fehler ist aufgetreten. Das Technik-Team wurde informiert.</Text>
                <ScrollView style={pageStyles.errorBox}>
                    <Text style={pageStyles.errorMessage}>{errorMessage}</Text>
                    <Text style={[pageStyles.errorMessage, { marginTop: 10, color: colors.textMuted }]}>{errorStack}</Text>
                </ScrollView>
                <View style={pageStyles.buttonContainer}>
                    {Platform.OS === 'web' ? (
                        <a href="/" style={{ textDecoration: 'none' }}>
                            <View style={pageStyles.button}>
                                <Icon name="home" size={16} color="#fff" />
                                <Text style={pageStyles.buttonText}>Zurück zum Start</Text>
                            </View>
                        </a>
                    ) : (
                        <TouchableOpacity style={pageStyles.button} onPress={() => Updates.reloadAsync()}>
                            <Icon name="refresh" size={16} color="#fff" />
                            <Text style={pageStyles.buttonText}>App neu laden</Text>
                        </TouchableOpacity>
                    )}
                </View>
            </View>
        </SafeAreaView>
    );
};

const styles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        fullScreenContainer: { flex: 1, backgroundColor: colors.background, alignItems: 'center', width: '100%' },
        content: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: spacing.lg, width: '100%', maxWidth: 600 },
        icon: { marginBottom: spacing.xl },
        title: { fontSize: typography.h1, fontWeight: 'bold', textAlign: 'center', marginBottom: spacing.md, color: colors.heading },
        message: { fontSize: typography.h4, textAlign: 'center', color: colors.textMuted, marginBottom: spacing.lg },
        errorBox: { maxHeight: 150, width: '100%', backgroundColor: colors.surface, borderWidth: 1, borderColor: colors.border, borderRadius: borders.radius, padding: spacing.md, marginBottom: spacing.lg },
        errorMessage: { fontFamily: Platform.OS === 'ios' ? 'Courier New' : 'monospace', color: colors.danger },
        buttonContainer: { flexDirection: 'row', gap: spacing.md },
        button: { backgroundColor: colors.primary, paddingVertical: 12, paddingHorizontal: 24, borderRadius: borders.radius, flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
        buttonText: { color: colors.white, fontWeight: '500' },
    });
};

export default ErrorPage;