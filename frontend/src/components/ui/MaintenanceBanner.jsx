import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, spacing, typography } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

const MaintenanceBanner = () => {
    const { mode, message } = useAuthStore(state => state.maintenanceStatus);
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);
    const insets = useSafeAreaInsets();

    if (mode !== 'SOFT') {
        return null;
    }

    return (
        <View style={[styles.bannerContainer, { paddingTop: insets.top }]}>
            <Icon name="exclamation-triangle" size={18} color={colors.black} style={styles.icon} />
            <Text style={styles.bannerText}>
                <Text style={{ fontWeight: 'bold' }}>Wartungsarbeiten: </Text>
                {message || 'Die Anwendung wird in Kürze gewartet.'}
            </Text>
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        bannerContainer: {
            backgroundColor: colors.warning,
            paddingHorizontal: spacing.md,
            paddingBottom: spacing.sm,
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
        },
        icon: {
            marginRight: spacing.sm,
        },
        bannerText: {
            color: colors.black,
            fontSize: typography.small,
            textAlign: 'center',
        },
    });
};

export default MaintenanceBanner;