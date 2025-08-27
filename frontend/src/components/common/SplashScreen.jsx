import React from 'react';
import { View, Text, ActivityIndicator, StyleSheet } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const SplashScreen = () => {
    const theme = useAuthStore.getState().theme;
    const colors = getThemeColors(theme);

    return (
        <View style={[styles.container, { backgroundColor: colors.background }]}>
            <ActivityIndicator size="large" color={colors.primary} />
            <Text style={[styles.text, { color: colors.textMuted }]}>
                Lade Anwendung...
            </Text>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    text: {
        marginTop: 16,
        fontSize: 16,
    },
});

export default SplashScreen;