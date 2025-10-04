import React from 'react';
import { View, Text, StyleSheet, Platform, TouchableOpacity, Linking } from 'react-native';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const OpenTasksWidget = ({ tasks, error }) => {
    const colors = getThemeColors('light');
    const styles = pageStyles({ colors });

    const renderContent = () => {
        if (error) {
            return <Text style={styles.errorText}>{error}</Text>;
        }
        if (!tasks || tasks.length === 0) {
            return <Text style={styles.placeholderText}>Keine offenen Aufgaben.</Text>;
        }
        return tasks.slice(0, 3).map(task => (
             <TouchableOpacity
                key={task.id}
                style={styles.taskItem}
                onPress={() => Linking.openURL(`technikteam://veranstaltungen/details/${task.eventId}`)}
            >
                <Icon name="tasks" size={16} color={colors.primary} />
                <View style={{ flex: 1, marginLeft: spacing.sm }}>
                    <Text style={styles.taskName} numberOfLines={1}>{task.name}</Text>
                    <Text style={styles.eventName} numberOfLines={1}>{task.eventName}</Text>
                </View>
            </TouchableOpacity>
        ));
    };

    return (
        <View style={styles.container}>
            <Text style={styles.header}>Offene Aufgaben</Text>
            {renderContent()}
        </View>
    );
};

const pageStyles = ({ colors }) => StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: colors.surface,
        padding: spacing.md,
    },
    header: {
        fontSize: typography.h4,
        fontWeight: 'bold',
        color: colors.heading,
        marginBottom: spacing.sm,
    },
    taskItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: spacing.sm,
        borderBottomWidth: 1,
        borderBottomColor: colors.border,
    },
    taskName: {
        fontWeight: '500',
        color: colors.text,
    },
    eventName: {
        fontSize: typography.small,
        color: colors.textMuted,
    },
    placeholderText: {
        color: colors.textMuted,
        fontStyle: 'italic',
        marginTop: spacing.md,
    },
    errorText: {
        color: colors.danger,
        marginTop: spacing.md,
    },
});


export default OpenTasksWidget;