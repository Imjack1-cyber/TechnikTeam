import React from 'react';
import { View, Text, StyleSheet, Platform, TouchableOpacity, Linking } from 'react-native';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const AnnouncementsWidget = ({ announcement, error }) => {
    const colors = getThemeColors('light');
    const styles = pageStyles({ colors });

    const renderContent = () => {
        if (error) {
            return <Text style={styles.errorText}>{error}</Text>;
        }
        if (!announcement) {
            return <Text style={styles.placeholderText}>Keine neuen Mitteilungen.</Text>;
        }

        const postDate = new Date(announcement.createdAt);
        const formattedDate = postDate.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });

        return (
            <TouchableOpacity onPress={() => Linking.openURL(`technikteam://bulletin-board`)}>
                <Text style={styles.title} numberOfLines={2}>{announcement.title}</Text>
                <Text style={styles.content} numberOfLines={4}>{announcement.content}</Text>
                <View style={styles.footer}>
                    <Icon name="user-circle" solid size={12} color={colors.textMuted} />
                    <Text style={styles.footerText}>{announcement.authorUsername} - {formattedDate}</Text>
                </View>
            </TouchableOpacity>
        );
    };

    return (
        <View style={styles.container}>
            <Text style={styles.header}>Anschlagbrett</Text>
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
    title: {
        fontSize: typography.body, // Adjusted for widget size
        fontWeight: '600',
        color: colors.primary,
        marginBottom: spacing.sm,
    },
    content: {
        fontSize: typography.small,
        color: colors.text,
        lineHeight: 18,
    },
    footer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: spacing.md,
    },
    footerText: {
        marginLeft: spacing.sm,
        fontSize: typography.caption,
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

export default AnnouncementsWidget;