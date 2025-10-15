import React from 'react';
import { View, Text, StyleSheet, Platform, TouchableOpacity, Linking } from 'react-native';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useWidgetStore } from '../store/widgetStore';

const AnnouncementsWidget = () => {
    const { latestAnnouncement, error } = useWidgetStore.getState();
    const colors = getThemeColors('light');
    const styles = pageStyles({ colors });

    const renderContent = () => {
        if (error) {
            return (
                <View style={styles.centered}>
                    <Icon name="exclamation-triangle" size={24} color={colors.danger} />
                    <Text style={styles.errorText}>Fehler beim Laden.</Text>
                </View>
            );
        }
        if (!latestAnnouncement) {
            return <Text style={styles.placeholderText}>Keine neuen Mitteilungen.</Text>;
        }

        const postDate = new Date(latestAnnouncement.createdAt);
        const formattedDate = postDate.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });

        return (
            <TouchableOpacity onPress={() => Linking.openURL(`technikteam://bulletin-board`)}>
                <Text style={styles.title} numberOfLines={2}>{latestAnnouncement.title}</Text>
                <Text style={styles.content} numberOfLines={Platform.OS === 'ios' ? 5 : 4}>{latestAnnouncement.content}</Text>
                <View style={styles.footer}>
                    <Icon name="user-circle" solid size={12} color={colors.textMuted} />
                    <Text style={styles.footerText}>{latestAnnouncement.authorUsername} - {formattedDate}</Text>
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
    centered: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    header: {
        fontSize: typography.h4,
        fontWeight: 'bold',
        color: colors.heading,
        marginBottom: spacing.sm,
    },
    title: {
        fontSize: typography.body,
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
        fontWeight: '500',
        textAlign: 'center',
    },
});

export default AnnouncementsWidget;