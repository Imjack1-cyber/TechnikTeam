import React, { useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Calendar } from 'react-native-calendars';
import { useNavigation } from '@react-navigation/native';
import { format, parseISO } from 'date-fns';

const ReservationCalendar = ({ reservations }) => {
    const navigation = useNavigation();

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
					selectedColor: '#dc3545', // danger-color
					dotColor: 'white',
					marked: true,
                    // Store extra data for onPress
                    eventName: res.event_name,
                    eventId: res.event_id
				};
			}
		});
		return markings;
	}, [reservations]);

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
                    calendarBackground: '#ffffff',
                    textSectionTitleColor: '#6c757d',
                    todayTextColor: '#007bff',
                    dayTextColor: '#212529',
                    arrowColor: '#007bff',
                    monthTextColor: '#002B5B',
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

const styles = StyleSheet.create({
    title: {
        fontSize: 18,
        fontWeight: '600',
        color: '#002B5B',
        marginBottom: 12,
    }
});

export default ReservationCalendar;