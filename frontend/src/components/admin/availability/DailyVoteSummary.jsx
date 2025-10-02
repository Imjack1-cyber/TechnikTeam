import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, spacing, typography, borders } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { format, parseISO } from 'date-fns';

const DailyVoteSummary = ({ analysis, adminAvailableDays }) => {
    const theme = 'light'; // This view is admin-only, but let's assume light for consistency
    const colors = getThemeColors(theme);
    const styles = pageStyles({ colors });

    if (!analysis || !adminAvailableDays || adminAvailableDays.length === 0) {
        return <Text style={{ color: colors.textMuted, padding: spacing.md }}>Noch keine Antworten oder keine Tage zur Auswahl gestellt.</Text>;
    }

    return (
        <ScrollView>
            {adminAvailableDays.sort().map(dateString => {
                const dayAnalysis = analysis[dateString] || {};
                const available = dayAnalysis['AVAILABLE'] || [];
                const maybe = dayAnalysis['MAYBE'] || [];
                const unavailable = dayAnalysis['UNAVAILABLE'] || [];

                return (
                    <View key={dateString} style={styles.dayContainer}>
                        <Text style={styles.dayTitle}>{format(parseISO(dateString), 'eeee, dd.MM.yyyy')}</Text>
                        
                        <View style={styles.section}>
                            <Text style={styles.sectionTitle}><Icon name="check-circle" color={colors.success} /> Verfügbar ({available.length})</Text>
                            <Text style={styles.participantList}>{available.join(', ') || 'Niemand'}</Text>
                        </View>
                        
                        <View style={styles.section}>
                            <Text style={styles.sectionTitle}><Icon name="question-circle" color={colors.warning} /> Vielleicht ({maybe.length})</Text>
                            {maybe.length > 0 ? maybe.map((item, index) => (
                                <Text key={index} style={styles.participantList}>{item.user}: <Text style={styles.maybeNotes}>{item.notes}</Text></Text>
                            )) : <Text style={styles.participantList}>Niemand</Text>}
                        </View>

                        <View style={styles.section}>
                            <Text style={styles.sectionTitle}><Icon name="times-circle" color={colors.danger} /> Nicht verfügbar ({unavailable.length})</Text>
                            <Text style={styles.participantList}>{unavailable.join(', ') || 'Niemand'}</Text>
                        </View>
                    </View>
                );
            })}
        </ScrollView>
    );
};

const pageStyles = ({ colors }) => StyleSheet.create({
    dayContainer: {
        backgroundColor: colors.surface,
        borderRadius: borders.radius,
        borderWidth: 1,
        borderColor: colors.border,
        padding: spacing.md,
        marginBottom: spacing.md,
    },
    dayTitle: {
        fontSize: typography.h4,
        fontWeight: 'bold',
        color: colors.heading,
        marginBottom: spacing.md,
        borderBottomWidth: 1,
        borderColor: colors.border,
        paddingBottom: spacing.sm,
    },
    section: {
        marginBottom: spacing.sm,
    },
    sectionTitle: {
        fontSize: typography.body,
        fontWeight: 'bold',
        color: colors.text,
        marginBottom: spacing.xs,
    },
    participantList: {
        color: colors.textMuted,
        paddingLeft: spacing.lg,
    },
    maybeNotes: {
        fontStyle: 'italic',
    },
});

export default DailyVoteSummary;