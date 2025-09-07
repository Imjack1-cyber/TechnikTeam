import React from 'react';
import { View, StyleSheet } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors } from '../../styles/theme';

const ProgressBar = ({ progress }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const progressPercent = (progress || 0) * 100;

    return (
        <View style={[styles.container, { backgroundColor: colors.border }]}>
            <View style={[styles.bar, { width: `${progressPercent}%`, backgroundColor: colors.primary }]} />
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        height: 8,
        borderRadius: 4,
        overflow: 'hidden',
        width: '100%',
    },
    bar: {
        height: '100%',
    },
});

export default ProgressBar;