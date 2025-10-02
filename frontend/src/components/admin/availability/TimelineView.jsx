import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useAuthStore } from '../../../store/authStore';
import { getThemeColors, spacing, typography, borders } from '../../../styles/theme';

const TimelineView = ({ poll, analysis }) => {
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles({ colors });

    if (!analysis || Object.keys(analysis).length === 0) {
        return <Text style={{ color: colors.textMuted, padding: spacing.md }}>Noch keine Antworten eingegangen.</Text>;
    }

    const sortedSlots = Object.entries(analysis).sort(([timeA], [timeB]) => new Date(timeA) - new Date(timeB));

    return (
        <ScrollView>
            {sortedSlots.map(([time, participants]) => {
                const count = participants.length;
                let barColor = colors.success;
                if (count <= 2) barColor = colors.warning;
                if (count <= 1) barColor = colors.danger;

                return (
                    <View key={time} style={styles.slotRow}>
                        <View style={styles.timeLabel}>
                            <Text style={styles.timeText}>{new Date(time).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })}</Text>
                        </View>
                        <View style={styles.barContainer}>
                            <View style={[styles.bar, { width: `${(count / 10) * 100}%`, backgroundColor: barColor }]} />
                            <Text style={styles.countText}>{count}</Text>
                        </View>
                        <View style={styles.participantsContainer}>
                            <Text style={styles.participantsText}>{participants.join(', ')}</Text>
                        </View>
                    </View>
                );
            })}
        </ScrollView>
    );
};

const pageStyles = ({ colors }) => StyleSheet.create({
    slotRow: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: spacing.md,
        borderBottomWidth: 1,
        borderBottomColor: colors.border,
    },
    timeLabel: {
        width: 80,
        paddingRight: spacing.md,
    },
    timeText: {
        fontWeight: 'bold',
        color: colors.text,
    },
    barContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: colors.background,
        borderRadius: borders.radius,
        height: 30,
    },
    bar: {
        height: '100%',
        borderRadius: borders.radius,
    },
    countText: {
        position: 'absolute',
        left: spacing.sm,
        fontWeight: 'bold',
        color: colors.white,
        textShadowColor: 'rgba(0, 0, 0, 0.75)',
        textShadowOffset: { width: 1, height: 1 },
        textShadowRadius: 2,
    },
    participantsContainer: {
        flex: 2,
        paddingLeft: spacing.md,
    },
    participantsText: {
        color: colors.textMuted,
        fontStyle: 'italic',
    },
});

export default TimelineView;