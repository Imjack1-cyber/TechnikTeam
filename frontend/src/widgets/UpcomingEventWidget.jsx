import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Platform, TouchableOpacity, Linking } from 'react-native';
import { getToken } from '../lib/storage';
import { getThemeColors, typography, spacing } from '../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const UpcomingEventWidget = () => {
    const [nextEvent, setNextEvent] = useState(null);
    const [error, setError] = useState(null);
    const colors = getThemeColors('light'); // Widgets often have a fixed theme
    const styles = pageStyles({ colors });

    useEffect(() => {
        const fetchWidgetData = async () => {
            try {
                const token = await getToken();
                if (!token) {
                    setError('Not logged in.');
                    return;
                }
                // This is a simplified direct fetch, bypassing apiClient for widget context
                const response = await fetch('https://technikteam.qs0.de/TechnikTeam/api/v1/public/dashboard/widget-data', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const result = await response.json();
                if (result.success) {
                    setNextEvent(result.data.nextEvent);
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
        if (!nextEvent) {
            return <Text style={styles.placeholderText}>Keine anstehenden Einsätze.</Text>;
        }

        const eventDate = new Date(nextEvent.eventDateTime);
        const formattedDate = eventDate.toLocaleDateString('de-DE', { weekday: 'long', day: '2-digit', month: '2-digit' });
        const formattedTime = eventDate.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' });

        return (
            <TouchableOpacity onPress={() => Linking.openURL(`technikteam://veranstaltungen/details/${nextEvent.id}`)}>
                <Text style={styles.eventName}>{nextEvent.name}</Text>
                <View style={styles.detailRow}>
                    <Icon name="calendar-alt" size={16} color={colors.textMuted} />
                    <Text style={styles.detailText}>{formattedDate}</Text>
                </View>
                <View style={styles.detailRow}>
                    <Icon name="clock" size={16} color={colors.textMuted} />
                    <Text style={styles.detailText}>{formattedTime} Uhr</Text>
                </View>
                <View style={styles.detailRow}>
                    <Icon name="map-marker-alt" size={16} color={colors.textMuted} />
                    <Text style={styles.detailText}>{nextEvent.location}</Text>
                </View>
            </TouchableOpacity>
        );
    };

    return (
        <View style={styles.container}>
            <Text style={styles.header}>Nächster Einsatz</Text>
            {renderContent()}
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
    eventName: {
        fontSize: typography.h3,
        fontWeight: '600',
        color: colors.primary,
        marginBottom: spacing.md,
    },
    detailRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: spacing.sm,
    },
    detailText: {
        marginLeft: spacing.md,
        fontSize: typography.body,
        color: colors.text,
    },
    placeholderText: {
        color: colors.textMuted,
        fontStyle: 'italic',
    },
    errorText: {
        color: colors.danger,
    },
});

export default UpcomingEventWidget;