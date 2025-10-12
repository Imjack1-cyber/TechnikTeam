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
  const theme = useAuthStore(state => state.theme);
  const colors = getThemeColors(theme);
  const styles = localStyles(theme);

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
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <View style={styles.headerRow}>
            <TouchableOpacity onPress={goPrev} style={styles.navButton}><Text style={{color: colors.text}}>{'<'}</Text></TouchableOpacity>
            <Text style={styles.headerTitle}>{format(currentMonth, 'MMMM yyyy')}</Text>
            <TouchableOpacity onPress={goNext} style={styles.navButton}><Text style={{color: colors.text}}>{'>'}</Text></TouchableOpacity>
          </View>
          <View style={styles.weekDaysRow}>
            {['S','M','T','W','T','F','S'].map(d => <Text key={d} style={styles.weekDayText}>{d}</Text>)}
          </View>
          <View style={styles.grid}>
            {days.map((d, idx) => {
              const key = d ? format(d, 'yyyy-MM-dd') : `blank-${idx}`;
              const disabled = isDisabled(d);
              const selectedDay = d && isSameDay(d, selectedDate);
              return (
                <TouchableOpacity
                  key={key}
                  onPress={() => !disabled && setSelectedDate(startOfDay(d))}
                  activeOpacity={disabled ? 1 : 0.7}
                  style={[styles.dayCell, disabled && styles.dayCellDisabled, selectedDay && styles.dayCellSelected]}
                >
                  <Text style={[styles.dayText, disabled && styles.dayTextDisabled, selectedDay && styles.dayTextSelected]}>
                    {d ? format(d, 'd') : ''}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <TimePicker value={selectedTime} onChange={handleConfirmTime} isWebModal={true}/>
          <View style={styles.actionsRow}>
            <TouchableOpacity onPress={onCancel} style={[styles.actionButton, styles.cancelButton]}><Text style={{color: colors.text}}>Abbrechen</Text></TouchableOpacity>
            <TouchableOpacity onPress={handleFinalConfirm} style={[styles.actionButton, styles.confirmButton]}><Text style={{ color: colors.white }}>Auswählen</Text></TouchableOpacity>
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

const localStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        modalOverlay: { flex: 1, backgroundColor: 'rgba(0, 0, 0, 0.6)', justifyContent: 'center', padding: 20 },
        modalContent: { backgroundColor: colors.surface, borderRadius: 12, padding: 12, maxHeight: '90%' },
        headerRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 },
        navButton: { padding: 8 },
        headerTitle: { fontWeight: '600', fontSize: 16, color: colors.heading },
        weekDaysRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 6 },
        weekDayText: { width: 32, textAlign: 'center', fontWeight: '600', color: colors.textMuted },
        grid: { flexDirection: 'row', flexWrap: 'wrap', marginBottom: 12 },
        dayCell: { width: `${100 / 7}%`, paddingVertical: 8, alignItems: 'center', justifyContent: 'center' },
        dayCellDisabled: { opacity: 0.3 },
        dayCellSelected: { backgroundColor: colors.primary, borderRadius: 18 },
        dayText: { textAlign: 'center', color: colors.text },
        dayTextDisabled: { color: colors.disabledText },
        dayTextSelected: { color: colors.white, fontWeight: '700' },
        actionsRow: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 6 },
        actionButton: { paddingVertical: 10, paddingHorizontal: 14, borderRadius: 6, marginLeft: 8 },
        cancelButton: { backgroundColor: colors.background },
        confirmButton: { backgroundColor: colors.primary },
    });
};

export default DateTimePicker;