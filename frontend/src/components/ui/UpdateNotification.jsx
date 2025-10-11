import React from 'react';
import { TouchableOpacity, Text, StyleSheet } from 'react-native';
import { useToast } from '../../context/ToastContext';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, shadows } from '../../styles/theme';

const UpdateNotification = ({ onUpdate }) => {
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = pageStyles(theme);

    const handleUpdate = () => {
        addToast("Anwendung wird aktualisiert...", "info");
        setTimeout(onUpdate, 1000);
    };

    return (
        <TouchableOpacity style={styles.container} onPress={handleUpdate}>
            <Text style={styles.text}>Eine neue Version ist verfügbar. Klicken, um zu aktualisieren.</Text>
            <Icon name="sync-alt" size={16} color={styles.text.color} />
        </TouchableOpacity>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: {
            position: 'absolute',
            bottom: spacing.md,
            left: spacing.md,
            right: spacing.md,
            zIndex: 10000,
            backgroundColor: colors.info,
            padding: spacing.md,
            borderRadius: 8,
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center',
            ...shadows.lg,
        },
        text: {
            color: colors.black,
            fontWeight: '500',
            flex: 1,
        },
    });
};

export default UpdateNotification;