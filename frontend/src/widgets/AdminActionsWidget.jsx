import React from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform, Linking } from 'react-native';
import { getToken } from '../lib/storage';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const WidgetButton = ({ label, icon, deepLink, styles, colors }) => {
    return (
        <TouchableOpacity
            style={styles.button}
            onPress={() => Linking.openURL(`technikteam://${deepLink}`)}
        >
            <Icon name={icon} size={20} color={colors.primary} />
            <Text style={styles.buttonText}>{label}</Text>
        </TouchableOpacity>
    );
};

const AdminActionsWidget = () => {
    const colors = getThemeColors('light'); // Widgets often have a fixed theme
    const styles = pageStyles({ colors });

    return (
        <View style={styles.container}>
            <Text style={styles.header}>Admin Actions</Text>
            <View style={styles.grid}>
                <WidgetButton label="New Event" icon="calendar-plus" deepLink="admin/events" styles={styles} colors={colors} />
                <WidgetButton label="New User" icon="user-plus" deepLink="admin/users" styles={styles} colors={colors} />
                <WidgetButton label="Storage" icon="warehouse" deepLink="admin/storage" styles={styles} colors={colors} />
                <WidgetButton label="System" icon="cogs" deepLink="admin/system" styles={styles} colors={colors} />
            </View>
        </View>
    );
};

const pageStyles = ({ colors }) => StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: colors.surface,
        padding: spacing.md,
        justifyContent: 'center',
    },
    header: {
        fontSize: typography.h4,
        fontWeight: 'bold',
        color: colors.heading,
        marginBottom: spacing.md,
    },
    grid: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    button: {
        backgroundColor: colors.background,
        borderRadius: 8,
        width: '48%',
        padding: spacing.md,
        marginBottom: spacing.sm,
        alignItems: 'center',
        justifyContent: 'center',
    },
    buttonText: {
        marginTop: spacing.sm,
        color: colors.text,
        fontWeight: '500',
    },
});

export default AdminActionsWidget;