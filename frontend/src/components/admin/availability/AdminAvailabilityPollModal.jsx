import React, { useState, useMemo, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Modal,
  ScrollView,
  FlatList,
  Platform,
  Switch,
  ActivityIndicator,
  StyleSheet,
} from 'react-native';
import { useToast } from '../../../context/ToastContext';
import apiClient from '../../../services/apiClient';
import AdminModal from '../../ui/AdminModal';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing, borders } from '../../../styles/theme';
import {
  addDays,
  format,
  startOfDay,
  eachDayOfInterval,
  startOfMonth,
  endOfMonth,
  isSameDay,
  isAfter,
  isBefore,
  differenceInCalendarDays,
  addMonths,
  subMonths,
} from 'date-fns';
import Stepper from '../../ui/Stepper';
import { RadioButton } from 'react-native-paper';

// -------------------------------
// Inline Date Picker Modal
// -------------------------------
const InlineDatePickerModal = ({
  isVisible,
  date = new Date(),
  onConfirm,
  onCancel,
  minimumDate,
  maximumDate,
}) => {
  const [currentMonth, setCurrentMonth] = useState(startOfMonth(date));
  const [selected, setSelected] = useState(date ? startOfDay(date) : startOfDay(new Date()));

  useEffect(() => {
    if (date) {
      setSelected(startOfDay(date));
      setCurrentMonth(startOfMonth(date));
    }
  }, [date, isVisible]);

  if (!isVisible) return null;

  const goPrev = () => setCurrentMonth(prev => subMonths(prev, 1));
  const goNext = () => setCurrentMonth(prev => addMonths(prev, 1));

  // Build month days grid
  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(currentMonth);
  const leadingBlanks = monthStart.getDay(); // 0 (Sun) - 6 (Sat)
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
    <Modal visible={true} transparent animationType="fade" onRequestClose={onCancel}>
      <View style={localStyles.modalOverlay}>
        <View style={localStyles.modalContent}>
          <View style={localStyles.headerRow}>
            <TouchableOpacity onPress={goPrev} style={localStyles.navButton}><Text>{'<'}</Text></TouchableOpacity>
            <Text style={localStyles.headerTitle}>{format(currentMonth, 'MMMM yyyy')}</Text>
            <TouchableOpacity onPress={goNext} style={localStyles.navButton}><Text>{'>'}</Text></TouchableOpacity>
          </View>

          <View style={localStyles.weekDaysRow}>
            {['S','M','T','W','T','F','S'].map(d => (
              <Text key={d} style={localStyles.weekDayText}>{d}</Text>
            ))}
          </View>

          <View style={localStyles.grid}>
            {days.map((d, idx) => {
              const key = d ? format(d, 'yyyy-MM-dd') : `blank-${idx}`;
              const disabled = isDisabled(d);
              const selectedDay = d && isSameDay(d, selected);
              return (
                <TouchableOpacity
                  key={key}
                  onPress={() => !disabled && setSelected(startOfDay(d))}
                  activeOpacity={disabled ? 1 : 0.7}
                  style={[
                    localStyles.dayCell,
                    disabled && localStyles.dayCellDisabled,
                    selectedDay && localStyles.dayCellSelected,
                  ]}
                >
                  <Text style={[localStyles.dayText, disabled && localStyles.dayTextDisabled, selectedDay && localStyles.dayTextSelected]}>
                    {d ? format(d, 'd') : ''}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          <View style={localStyles.actionsRow}>
            <TouchableOpacity onPress={onCancel} style={[localStyles.actionButton, localStyles.cancelButton]}>
              <Text>Abbrechen</Text>
            </TouchableOpacity>
            <TouchableOpacity
              onPress={() => onConfirm(selected)}
              style={[localStyles.actionButton, localStyles.confirmButton]}
            >
              <Text style={{ color: '#fff' }}>Auswählen</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
};

// -------------------------------
// Inline Range Calendar (Select Days)
// -------------------------------
const RangeCalendar = ({ minDate, maxDate, selectedDates = [], onToggle, colorPrimary, textColor }) => {
  const days = useMemo(() => {
    if (!minDate || !maxDate || isAfter(minDate, maxDate)) return [];
    try {
      return eachDayOfInterval({ start: startOfDay(minDate), end: startOfDay(maxDate) });
    } catch (e) {
      return [];
    }
  }, [minDate, maxDate]);

  const months = useMemo(() => {
    const map = {};
    days.forEach(d => {
      const key = format(d, 'MMMM yyyy');
      if (!map[key]) map[key] = [];
      map[key].push(d);
    });
    return map;
  }, [days]);

  const selectedSet = useMemo(() => new Set(selectedDates || []), [selectedDates]);

  const renderMonthCells = (monthDays) => {
    const firstDay = monthDays[0];
    const blanks = firstDay.getDay();
    const cells = [];

    // Add leading blank cells with stable keys
    for (let i = 0; i < blanks; i++) {
      cells.push(<View key={`blank-start-${format(firstDay, 'MM-yyyy')}-${i}`} style={localStyles.smallDayCell} />);
    }

    // Add day cells
    monthDays.forEach(d => {
      const key = format(d, 'yyyy-MM-dd');
      const selected = selectedSet.has(key);
      cells.push(
        <TouchableOpacity
          key={key}
          onPress={() => onToggle(key)}
          style={[localStyles.smallDayCell, selected && { backgroundColor: colorPrimary, borderRadius: 6 }]}
          activeOpacity={0.8}
        >
          <Text style={[localStyles.smallDayText, selected && { color: '#fff' }]}>{format(d, 'd')}</Text>
        </TouchableOpacity>
      );
    });

    // Add trailing blank cells to fill the grid, with stable keys
    while (cells.length % 7 !== 0) {
      cells.push(<View key={`blank-end-${format(firstDay, 'MM-yyyy')}-${cells.length}`} style={localStyles.smallDayCell} />);
    }
    return cells;
  };

  if (days.length === 0) {
    return <Text>Kein Zeitraum definiert.</Text>;
  }

  return (
    <ScrollView style={{ maxHeight: 420 }}>
      {Object.keys(months).map(monthLabel => (
        <View key={monthLabel} style={{ marginBottom: spacing.md }}>
          <Text style={{ marginBottom: spacing.sm, fontWeight: '600', color: textColor }}>{monthLabel}</Text>
          <View style={localStyles.weekDaysRowSmall}>
            {['S','M','T','W','T','F','S'].map(w => <Text key={w} style={localStyles.weekDayTextSmall}>{w}</Text>)}
          </View>
          <View style={localStyles.monthGrid}>
            {renderMonthCells(months[monthLabel])}
          </View>
        </View>
      ))}
    </ScrollView>
  );
};

// -------------------------------
// Main Component
// -------------------------------
const AdminAvailabilityPollModal = ({ isOpen, onClose, onSuccess }) => {
  const theme = useAuthStore(state => state.theme);
  const styles = getCommonStyles(theme);
  const colors = getThemeColors(theme);
  const { addToast } = useToast();

  const [step, setStep] = useState(0);
  const [type, setType] = useState('AVAILABILITY');
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    startTime: new Date(),
    endTime: addDays(new Date(), 7),
  });
  const [options, setOptions] = useState({ allowGuests: false, requireVerificationCode: false, availableDays: [] });
  const [verificationCode, setVerificationCode] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [isPickerVisible, setPickerVisible] = useState(false);
  const [pickerTargetField, setPickerTargetField] = useState(null);

  // Reset on close/open
  useEffect(() => {
    if (isOpen) {
      resetState();
    }
  }, [isOpen]);

  const resetState = () => {
    setStep(0);
    setType('AVAILABILITY');
    setFormData({ title: '', description: '', startTime: new Date(), endTime: addDays(new Date(), 7) });
    setOptions({ allowGuests: false, requireVerificationCode: false, availableDays: [] });
    setVerificationCode('');
    setError('');
    setIsSubmitting(false);
    setPickerVisible(false);
    setPickerTargetField(null);
  };

  const handleClose = () => {
    resetState();
    onClose && onClose();
  };
  const handleSuccess = () => {
    resetState();
    onSuccess && onSuccess();
  };

  const showPicker = (field) => {
    setPickerTargetField(field);
    setPickerVisible(true);
  };

  const handleConfirmDate = (date) => {
    if (!pickerTargetField) {
      setPickerVisible(false);
      return;
    }
    setFormData(prev => ({ ...prev, [pickerTargetField]: date }));
    setPickerVisible(false);

    if (pickerTargetField === 'startTime' && isAfter(date, formData.endTime)) {
      setFormData(prev => ({ ...prev, endTime: addDays(date, 1) }));
    }
    if (pickerTargetField === 'endTime' && isBefore(date, formData.startTime)) {
      setFormData(prev => ({ ...prev, startTime: addDays(date, -1) }));
    }
  };

  const handleDayPress = (dateString) => {
    setOptions(prev => {
      const setDays = new Set(prev.availableDays || []);
      if (setDays.has(dateString)) {
        setDays.delete(dateString);
      } else {
        setDays.add(dateString);
      }
      return { ...prev, availableDays: Array.from(setDays).sort() };
    });
  };

  const handleSubmit = async () => {
    setIsSubmitting(true);
    setError('');
    try {
      const payload = {
        title: formData.title,
        description: formData.description,
        startTime: format(startOfDay(formData.startTime), "yyyy-MM-dd'T'HH:mm:ss"),
        endTime: format(startOfDay(formData.endTime), "yyyy-MM-dd'T'HH:mm:ss"),
        type,
        options: JSON.stringify(options),
        verificationCode: options.requireVerificationCode ? verificationCode : null,
      };

      const result = await apiClient.post('/admin/availability', payload);
      if (result && result.success) {
        addToast('Umfrage erfolgreich erstellt.', 'success');
        handleSuccess();
      } else {
        throw new Error((result && result.message) || 'Unbekannter Fehler vom Server.');
      }
    } catch (err) {
      setError(err.message || 'Erstellen der Umfrage fehlgeschlagen.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const renderStepContent = () => {
    switch (step) {
      case 0:
         return (
            <View>
                <Text style={styles.label}>Welche Art von Umfrage möchten Sie erstellen?</Text>
                <RadioButton.Group onValueChange={setType} value={type}>
                    <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="AVAILABILITY" /><Text>Einfache Verfügbarkeit</Text></View>
                    <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="SCHEDULING" /><Text>Terminfindung (öffentlich teilbar)</Text></View>
                </RadioButton.Group>
                <Text style={[styles.input, styles.textArea, {backgroundColor: colors.background, marginTop: 16, minHeight: 100}]}>
                    {type === 'AVAILABILITY'
                        ? 'Eine einfache Umfrage für Teammitglieder, um ihre generelle Verfügbarkeit für ausgewählte Tage anzugeben.'
                        : 'Eine detaillierte Terminfindung, die über einen öffentlichen Link auch mit Gästen geteilt werden kann.'
                    }
                </Text>
            </View>
        );
      case 1:
        return (
          <View>
            <Text style={styles.label}>Titel</Text>
            <TextInput
              style={styles.input}
              value={formData.title}
              onChangeText={val => setFormData(prev => ({ ...prev, title: val }))}
            />
            <Text style={styles.label}>Beschreibung (optional)</Text>
            <TextInput
              style={[styles.input, styles.textArea]}
              value={formData.description}
              onChangeText={val => setFormData(prev => ({ ...prev, description: val }))}
              multiline
            />

            <Text style={styles.label}>Startdatum des Zeitraums</Text>
            <TouchableOpacity onPress={() => showPicker('startTime')} style={[styles.input, { justifyContent: 'center' }]}>
              <Text>{format(formData.startTime, 'dd.MM.yyyy')}</Text>
            </TouchableOpacity>

            <Text style={styles.label}>Enddatum des Zeitraums</Text>
            <TouchableOpacity onPress={() => showPicker('endTime')} style={[styles.input, { justifyContent: 'center' }]}>
              <Text>{format(formData.endTime, 'dd.MM.yyyy')}</Text>
            </TouchableOpacity>
          </View>
        );
      case 2:
        return (
          <View>
            <Text style={styles.label}>Wähle die Tage aus, die zur Abstimmung stehen:</Text>
            <RangeCalendar
              minDate={formData.startTime}
              maxDate={formData.endTime}
              selectedDates={options.availableDays}
              onToggle={handleDayPress}
              colorPrimary={colors.primary}
              textColor={colors.text}
            />
          </View>
        );
      case 3:
        return (
          <View>
            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.md }}>
              <Text style={styles.label}>Gästen die Teilnahme erlauben?</Text>
              <Switch
                value={options.allowGuests}
                onValueChange={val => setOptions(prev => ({ ...prev, allowGuests: val }))}
              />
            </View>

            {options.allowGuests && (
              <>
                <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.md }}>
                  <Text style={styles.label}>Verifizierungscode für Gäste?</Text>
                  <Switch
                    value={options.requireVerificationCode}
                    onValueChange={val => setOptions(prev => ({ ...prev, requireVerificationCode: val }))}
                  />
                </View>
                {options.requireVerificationCode && (
                  <TextInput
                    style={styles.input}
                    value={verificationCode}
                    onChangeText={setVerificationCode}
                    placeholder="Geheimer Code"
                  />
                )}
              </>
            )}
          </View>
        );
      default:
        return <View />;
    }
  };

  const steps = ['Typ', 'Details', 'Tage auswählen', 'Optionen'];

  return (
    <AdminModal isOpen={isOpen} onClose={handleClose} title="Neue Umfrage erstellen">
      {error ? <Text style={styles.errorText}>{error}</Text> : null}
      <Stepper steps={steps} currentStep={step} />
      <View style={{ marginVertical: spacing.md, minHeight: 400, justifyContent: 'center' }}>
        {renderStepContent()}
      </View>

      <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
        <TouchableOpacity
          style={[styles.button, styles.secondaryButton, step === 0 ? styles.disabledButton : null]}
          onPress={() => setStep(s => Math.max(0, s - 1))}
          disabled={step === 0}
        >
          <Text style={styles.buttonText}>Zurück</Text>
        </TouchableOpacity>

        {step < steps.length - 1 ? (
          <TouchableOpacity
            style={[styles.button, styles.primaryButton]}
            onPress={() => setStep(s => Math.min(steps.length - 1, s + 1))}
          >
            <Text style={styles.buttonText}>Weiter</Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity
            style={[styles.button, styles.successButton]}
            onPress={handleSubmit}
            disabled={isSubmitting}
          >
            {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Erstellen</Text>}
          </TouchableOpacity>
        )}
      </View>

      <InlineDatePickerModal
        isVisible={isPickerVisible}
        date={pickerTargetField ? (formData[pickerTargetField] || new Date()) : new Date()}
        minimumDate={pickerTargetField === 'endTime' ? formData.startTime : undefined}
        maximumDate={undefined}
        onConfirm={handleConfirmDate}
        onCancel={() => setPickerVisible(false)}
      />
    </AdminModal>
  );
};

const localStyles = StyleSheet.create({
  modalOverlay: {
    flex: 1,
    backgroundColor: '#00000088',
    justifyContent: 'center',
    padding: 20,
  },
  modalContent: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 12,
    maxHeight: '90%',
  },
  headerRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  navButton: {
    padding: 8,
  },
  headerTitle: {
    fontWeight: '600',
    fontSize: 16,
  },
  weekDaysRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  weekDayText: {
    width: 32,
    textAlign: 'center',
    fontWeight: '600',
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 12,
  },
  dayCell: {
    width: `${100 / 7}%`,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  dayCellDisabled: {
    opacity: 0.3,
  },
  dayCellSelected: {
    backgroundColor: '#1976D2',
    borderRadius: 18,
  },
  dayText: {
    textAlign: 'center',
  },
  dayTextDisabled: {
    color: '#999',
  },
  dayTextSelected: {
    color: '#fff',
    fontWeight: '700',
  },
  actionsRow: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    marginTop: 6,
  },
  actionButton: {
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 6,
    marginLeft: 8,
  },
  cancelButton: {
    backgroundColor: '#eee',
  },
  confirmButton: {
    backgroundColor: '#1976D2',
  },

  weekDaysRowSmall: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  weekDayTextSmall: {
    width: `${100 / 7}%`,
    textAlign: 'center',
    fontWeight: '600',
    fontSize: 12,
  },
  monthGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  smallDayCell: {
    width: `${100 / 7}%`,
    paddingVertical: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  smallDayText: {
    textAlign: 'center',
    fontSize: 14,
  },
});

export default AdminAvailabilityPollModal;