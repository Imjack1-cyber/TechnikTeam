import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';

const CalendarMobileView = ({ entries }) => {
    const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

	if (!entries || entries.length === 0) {
		return <View style={styles.emptyContainer}><Text>Derzeit sind keine Termine geplant.</Text></View>;
	}

	const sortedEntries = [...entries].sort((a, b) => parseISO(a.start) - parseISO(b.start));

	const renderItem = ({ item }) => {
        const isEvent = item.type === 'Event';
        const navigateTo = isEvent ? 'EventDetails' : 'MeetingDetails';
        const params = isEvent ? { eventId: item.id } : { meetingId: item.id };
        
		return (
            <TouchableOpacity style={styles.itemLink} onPress={() => navigation.navigate(navigateTo, params)}>
                <View style={styles.dateContainer}>
                    <Text style={styles.dateDay}>{format(parseISO(item.start), 'dd')}</Text>
                    <Text style={styles.dateMonth}>{format(parseISO(item.start), 'MMM', { locale: de })}</Text>
                </View>
                <View style={styles.detailsContainer}>
                    <Text style={styles.title}>{item.title}</Text>
                    <View style={[styles.badge, isEvent ? styles.eventBadge : styles.meetingBadge]}>
                        <Text style={styles.badgeText}>{item.type}</Text>
                    </View>
                </View>
                <Icon name="chevron-right" size={16} color={colors.textMuted} />
            </TouchableOpacity>
        );
	};

	return (
		<FlatList
			data={sortedEntries}
			renderItem={renderItem}
			keyExtractor={item => `${item.type}-${item.id}`}
		/>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        emptyContainer: { padding: spacing.lg, alignItems: 'center' },
        itemLink: {
            flexDirection: 'row',
            alignItems: 'center',
            padding: spacing.md,
            backgroundColor: colors.surface,
            borderBottomWidth: borders.width,
            borderBottomColor: colors.border,
        },
        dateContainer: {
            alignItems: 'center',
            marginRight: spacing.md,
            width: 60,
        },
        dateDay: {
            fontSize: 28,
            fontWeight: '600',
            color: colors.primary,
        },
        dateMonth: {
            fontSize: 14,
            textTransform: 'uppercase',
            color: colors.textMuted,
        },
        detailsContainer: {
            flex: 1,
        },
        title: {
            fontWeight: '600',
            fontSize: typography.body,
            marginBottom: spacing.xs,
        },
        badge: {
            paddingVertical: 4,
            paddingHorizontal: 8,
            borderRadius: 12,
            alignSelf: 'flex-start',
        },
        badgeText: {
            color: colors.white,
            fontSize: typography.caption,
            fontWeight: 'bold',
        },
        eventBadge: {
            backgroundColor: colors.danger,
        },
        meetingBadge: {
            backgroundColor: colors.primary,
        },
    });
};

export default CalendarMobileView;