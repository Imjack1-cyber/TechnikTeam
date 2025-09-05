import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView } from 'react-native';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';
import { navigationRef } from '../../router/navigation';

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

const ForbiddenPage = () => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const pageStyles = styles(theme);

    return (
        <SafeAreaView style={pageStyles.fullScreenContainer}>
            <SimpleHeader title="Zugriff verweigert" />
            <View style={pageStyles.content}>
                <Icon name="hand-paper" size={80} color={colors.warning} style={pageStyles.icon} />
                <Text style={pageStyles.title}>Fehler 403 - Zugriff verweigert</Text>
                <Text style={pageStyles.message}>Sie haben keine Berechtigung, auf diese Seite zuzugreifen. Ihr Versuch wurde protokolliert.</Text>
                <View style={pageStyles.buttonContainer}>
                    <TouchableOpacity style={pageStyles.button} onPress={() => navigationRef.navigate('Dashboard')}>
                        <Icon name="arrow-left" size={16} color="#fff" />
                        <Text style={pageStyles.buttonText}>Zurück zum Dashboard</Text>
                    </TouchableOpacity>
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
        buttonContainer: { flexDirection: 'row', gap: spacing.md },
        button: { backgroundColor: colors.primary, paddingVertical: 12, paddingHorizontal: 24, borderRadius: borders.radius, flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
        buttonText: { color: colors.white, fontWeight: '500' },
    });
};

export default ForbiddenPage;