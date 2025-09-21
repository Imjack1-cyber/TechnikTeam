import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { Calendar, LocaleConfig } from 'react-native-calendars';
import { useNavigation } from '@react-navigation/native';
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, typography, spacing, borders } from '../../styles/theme';

LocaleConfig.locales['de'] = {
  monthNames: ['Januar','Februar','März','April','Mai','Juni','Juli','August','September','Oktober','November','Dezember'],
  monthNamesShort: ['Jan.','Feb.','März','Apr.','Mai','Juni','Juli','Aug.','Sep.','Okt.','Nov.','Dez.'],
  dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
  dayNamesShort: ['So.','Mo.','Di.','Mi.','Do.','Fr.','Sa.'],
  today: "Heute"
};
LocaleConfig.defaultLocale = 'de';

const CalendarView = ({ entries }) => {
    const navigation = useNavigation();
    const [currentDate, setCurrentDate] = React.useState(format(new Date(), 'yyyy-MM-dd'));
    const theme = useAuthStore(state => state.theme);
    const colors = getThemeColors(theme);
    const styles = pageStyles(theme);

    const markedDates = React.useMemo(() => {
        const markings = {};
        entries.forEach(entry => {
            const startDate = parseISO(entry.start);
            const endDate = entry.end ? parseISO(entry.end) : startDate;

            for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
                const dateString = format(d, 'yyyy-MM-dd');
                const existing = markings[dateString];
                
                const newDot = {
                    key: `${entry.type}-${entry.id}`,
                    color: entry.type === 'Event' ? colors.danger : colors.primary,
                };

                if (existing?.dots) {
                    existing.dots.push(newDot);
                } else {
                     markings[dateString] = {
                        dots: [newDot],
                        marked: true,
                        // Store entry data for onPress
                        entryData: entry
                    };
                }
            }
        });
        return markings;
    }, [entries, colors]);

    const onDayPress = (day) => {
        const dateString = day.dateString;
        if (markedDates[dateString] && markedDates[dateString].entryData) {
            const entry = markedDates[dateString].entryData;
            if (entry.type === 'Event') {
                navigation.navigate('Veranstaltungen', { screen: 'EventDetails', params: { eventId: entry.id } });
            } else {
                navigation.navigate('MeetingDetails', { meetingId: entry.id });
            }
        }
    };

    return (
        <View style={styles.calendarContainer}>
            <Calendar
                current={currentDate}
                onDayPress={onDayPress}
                markedDates={markedDates}
                markingType={'multi-dot'}
                enableSwipeMonths={true}
                theme={{
                    calendarBackground: colors.surface,
                    textSectionTitleColor: colors.textMuted,
                    todayTextColor: colors.primary,
                    dayTextColor: colors.text,
                    monthTextColor: colors.heading,
                    textDayFontWeight: '300',
                    textMonthFontWeight: 'bold',
                    textDayHeaderFontWeight: '500',
                    arrowColor: colors.primary,
                }}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        calendarContainer: {
            marginHorizontal: spacing.md,
            marginBottom: spacing.md,
            borderWidth: borders.width,
            borderColor: colors.border,
            borderRadius: borders.radius,
            overflow: 'hidden',
        },
    });
};

export default CalendarView;