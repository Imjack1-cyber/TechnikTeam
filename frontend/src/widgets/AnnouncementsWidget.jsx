import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Platform, TouchableOpacity, Linking } from 'react-native';
import { getToken } from '../lib/storage';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const AnnouncementsWidget = () => {
    const [announcement, setAnnouncement] = useState(null);
    const [error, setError] = useState(null);
    const colors = getThemeColors('light');
    const styles = pageStyles({ colors });

    useEffect(() => {
        const fetchWidgetData = async () => {
            try {
                const token = await getToken();
                if (!token) {
                    setError('Not logged in.');
                    return;
                }
                const response = await fetch('https://technikteam.qs0.de/TechnikTeam/api/v1/public/dashboard/widget-data', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const result = await response.json();
                if (result.success) {
                    setAnnouncement(result.data.latestAnnouncement);
                } else {
                    setError(result.message);
                }
            } catch (e) {
                setError('Network error.');
            }
        };
        fetchWidgetData();
    }, []);

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
        fontSize: typography.h3,
        fontWeight: '600',
        color: colors.primary,
        marginBottom: spacing.sm,
    },
    content: {
        fontSize: typography.body,
        color: colors.text,
        lineHeight: 22,
    },
    footer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: spacing.md,
    },
    footerText: {
        marginLeft: spacing.sm,
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

export default AnnouncementsWidget;