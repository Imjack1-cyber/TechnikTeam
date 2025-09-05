import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, SafeAreaView } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import Icon from '@expo/vector-icons/FontAwesome5';
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

const NotFoundPage = () => {
    const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const pageStyles = styles(theme);

    return (
        <SafeAreaView style={pageStyles.fullScreenContainer}>
            <SimpleHeader title="Seite nicht gefunden" />
            <View style={pageStyles.content}>
                <Icon name="map-signs" size={80} color={colors.primary} style={pageStyles.icon} />
                <Text style={pageStyles.title}>Fehler 404 - Seite nicht gefunden</Text>
                <Text style={pageStyles.message}>Die von Ihnen angeforderte Seite existiert nicht. Bitte überprüfen Sie die URL oder gehen Sie zurück zum Dashboard.</Text>
                <TouchableOpacity style={pageStyles.button} onPress={() => navigation.navigate('Dashboard')}>
                    <Icon name="home" size={16} color="#fff" />
                    <Text style={pageStyles.buttonText}>Zum Dashboard</Text>
                </TouchableOpacity>
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
        button: { backgroundColor: colors.primary, paddingVertical: 12, paddingHorizontal: 24, borderRadius: borders.radius, flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
        buttonText: { color: colors.white, fontWeight: '500' },
    });
};

export default NotFoundPage;