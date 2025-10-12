import React, { useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Calendar, LocaleConfig } from 'react-native-calendars';
import { useNavigation } from '@react-navigation/native';
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography } from '../../styles/theme';

LocaleConfig.locales['de'] = {
  monthNames: ['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember'],
  monthNamesShort: ['Jan.','Feb.','März','Apr.','Mai','Juni','Juli','Aug.','Sep.','Okt.','Nov.','Dez.'],
  dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
  dayNamesShort: ['So.','Mo.','Di.','Mi.','Do.','Fr.','Sa.'],
  today: "Heute"
};
LocaleConfig.defaultLocale = 'de';

const ReservationCalendar = ({ reservations }) => {
    const navigation = useNavigation();
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

	const markedDates = useMemo(() => {
		if (!reservations) return {};

		const markings = {};
		reservations.forEach(res => {
			const startDate = parseISO(res.event_datetime);
			const endDate = res.end_datetime ? parseISO(res.end_datetime) : startDate;

			// This library requires iterating through the date range
			for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
				const dateString = format(d, 'yyyy-MM-dd');
				markings[dateString] = {
					selected: true,
					selectedColor: colors.danger,
					dotColor: colors.white,
					marked: true,
                    // Store extra data for onPress
                    eventName: res.event_name,
                    eventId: res.event_id
				};
			}
		});
		return markings;
	}, [reservations, colors]);

    const onDayPress = (day) => {
        const dateString = day.dateString;
        if (markedDates[dateString] && markedDates[dateString].eventId) {
            navigation.navigate('EventDetails', { eventId: markedDates[dateString].eventId });
        }
    }

	return (
        <View>
            <Text style={styles.title}>Zukünftige Reservierungen</Text>
            <Calendar
                markedDates={markedDates}
                onDayPress={onDayPress}
                theme={{
                    calendarBackground: colors.surface,
                    textSectionTitleColor: colors.textMuted,
                    todayTextColor: colors.primary,
                    dayTextColor: colors.text,
                    arrowColor: colors.primary,
                    monthTextColor: colors.heading,
                    textDayFontWeight: '300',
                    textMonthFontWeight: 'bold',
                    textDayHeaderFontWeight: '300',
                    textDayFontSize: 16,
                    textMonthFontSize: 18,
                    textDayHeaderFontSize: 14,
                }}
            />
        </View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        title: {
            fontSize: typography.h4,
            fontWeight: '600',
            color: colors.heading,
            marginBottom: 12,
        }
    });
};

export default ReservationCalendar;