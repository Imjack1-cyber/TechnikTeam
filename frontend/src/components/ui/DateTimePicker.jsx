import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform, Modal as RNModal } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';
import DateTimePickerModal from 'react-native-modal-datetime-picker';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { format, addDays, startOfDay, eachDayOfInterval, startOfMonth, endOfMonth, isSameDay, isAfter, isBefore, differenceInCalendarDays, addMonths, subMonths, setHours, setMinutes } from 'date-fns';
import TimePicker from './TimePicker';

const WebDatePickerModal = ({ isVisible, date, onConfirm, onCancel, minimumDate, maximumDate }) => {
  const [currentMonth, setCurrentMonth] = useState(startOfMonth(date || new Date()));
  const [selectedDate, setSelectedDate] = useState(date ? startOfDay(date) : startOfDay(new Date()));
  const [selectedTime, setSelectedTime] = useState(date || new Date());

  useEffect(() => {
    if (date) {
        setSelectedDate(startOfDay(date));
        setSelectedTime(date);
        setCurrentMonth(startOfMonth(date));
    }
  }, [date, isVisible]);

  if (!isVisible) return null;

  const handleConfirmTime = (time) => {
    setSelectedTime(time);
  };

  const handleFinalConfirm = () => {
    let finalDate = setHours(selectedDate, selectedTime.getHours());
    finalDate = setMinutes(finalDate, selectedTime.getMinutes());
    onConfirm(finalDate);
  };
  
  const goPrev = () => setCurrentMonth(prev => subMonths(prev, 1));
  const goNext = () => setCurrentMonth(prev => addMonths(prev, 1));

  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(currentMonth);
  const leadingBlanks = monthStart.getDay();
  const daysInMonth = differenceInCalendarDays(monthEnd, monthStart) + 1;
  const days = [];
  for (let i = 0; i < leadingBlanks; i++) days.push(null);
  for (let i = 0; i < daysInMonth; i++) {
    const d = addDays(monthStart, i);
    days.push(d);
  }

  const isDisabled = (d) => {
    if (!d) return true;
    if (minimumDate && isBefore(d, startOfDay(minimumDate))) return true;
    if (maximumDate && isAfter(d, startOfDay(maximumDate))) return true;
    return false;
  };

  return (
    <RNModal visible={true} transparent animationType="fade" onRequestClose={onCancel}>
      <View style={localStyles.modalOverlay}>
        <View style={localStyles.modalContent}>
          <View style={localStyles.headerRow}>
            <TouchableOpacity onPress={goPrev} style={localStyles.navButton}><Text>{'<'}</Text></TouchableOpacity>
            <Text style={localStyles.headerTitle}>{format(currentMonth, 'MMMM yyyy')}</Text>
            <TouchableOpacity onPress={goNext} style={localStyles.navButton}><Text>{'>'}</Text></TouchableOpacity>
          </View>
          <View style={localStyles.weekDaysRow}>
            {['S','M','T','W','T','F','S'].map(d => <Text key={d} style={localStyles.weekDayText}>{d}</Text>)}
          </View>
          <View style={localStyles.grid}>
            {days.map((d, idx) => {
              const key = d ? format(d, 'yyyy-MM-dd') : `blank-${idx}`;
              const disabled = isDisabled(d);
              const selectedDay = d && isSameDay(d, selectedDate);
              return (
                <TouchableOpacity
                  key={key}
                  onPress={() => !disabled && setSelectedDate(startOfDay(d))}
                  activeOpacity={disabled ? 1 : 0.7}
                  style={[localStyles.dayCell, disabled && localStyles.dayCellDisabled, selectedDay && localStyles.dayCellSelected]}
                >
                  <Text style={[localStyles.dayText, disabled && localStyles.dayTextDisabled, selectedDay && localStyles.dayTextSelected]}>
                    {d ? format(d, 'd') : ''}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <TimePicker value={selectedTime} onChange={handleConfirmTime} isWebModal={true}/>
          <View style={localStyles.actionsRow}>
            <TouchableOpacity onPress={onCancel} style={[localStyles.actionButton, localStyles.cancelButton]}><Text>Abbrechen</Text></TouchableOpacity>
            <TouchableOpacity onPress={handleFinalConfirm} style={[localStyles.actionButton, localStyles.confirmButton]}><Text style={{ color: '#fff' }}>Auswählen</Text></TouchableOpacity>
          </View>
        </View>
      </View>
    </RNModal>
  );
};

const DateTimePicker = ({ label, value, onChange, mode = 'datetime' }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const [isPickerVisible, setPickerVisible] = useState(false);

    const showPicker = () => setPickerVisible(true);
    const hidePicker = () => setPickerVisible(false);

    const handleConfirm = (date) => {
        onChange(date);
        hidePicker();
    };

    const displayFormat = mode === 'date' ? 'dd.MM.yyyy' : 'dd.MM.yyyy HH:mm';

    return (
        <View style={styles.formGroup}>
            <Text style={styles.label}>{label}</Text>
            <TouchableOpacity onPress={showPicker} style={[styles.input, { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }]}>
                <Text style={value ? { color: colors.text } : { color: colors.textMuted }}>
                    {value ? format(value, displayFormat) : 'Datum auswählen'}
                </Text>
                <Icon name="calendar-alt" size={20} color={colors.primary} />
            </TouchableOpacity>

            {isPickerVisible && (
                Platform.OS === 'web' ? (
                    <WebDatePickerModal
                        isVisible={isPickerVisible}
                        date={value || new Date()}
                        onConfirm={handleConfirm}
                        onCancel={hidePicker}
                    />
                ) : (
                    <DateTimePickerModal
                        isVisible={isPickerVisible}
                        mode={mode}
                        date={value || new Date()}
                        onConfirm={handleConfirm}
                        onCancel={hidePicker}
                        is24Hour={true}
                        locale="de_DE"
                    />
                )
            )}
        </View>
    );
};

const localStyles = StyleSheet.create({
    modalOverlay: { flex: 1, backgroundColor: '#00000088', justifyContent: 'center', padding: 20 },
    modalContent: { backgroundColor: '#fff', borderRadius: 12, padding: 12, maxHeight: '90%' },
    headerRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 },
    navButton: { padding: 8 },
    headerTitle: { fontWeight: '600', fontSize: 16 },
    weekDaysRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 },
    weekDayText: { width: 32, textAlign: 'center', fontWeight: '600' },
    grid: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: 12 },
    dayCell: { width: `${100 / 7}%`, paddingVertical: 8, alignItems: 'center', justifyContent: 'center' },
    dayCellDisabled: { opacity: 0.3 },
    dayCellSelected: { backgroundColor: '#1976D2', borderRadius: 18 },
    dayText: { textAlign: 'center' },
    dayTextDisabled: { color: '#999' },
    dayTextSelected: { color: '#fff', fontWeight: '700' },
    actionsRow: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 6 },
    actionButton: { paddingVertical: 10, paddingHorizontal: 14, borderRadius: 6, marginLeft: 8 },
    cancelButton: { backgroundColor: '#eee' },
    confirmButton: { backgroundColor: '#1976D2' },
});

export default DateTimePicker;