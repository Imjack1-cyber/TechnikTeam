import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, ScrollView, Switch, StyleSheet } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useToast } from '../../../context/ToastContext';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../ui/AdminModal';
import { getThemeColors, spacing } from '../../../styles/theme';
import DateTimePicker from '../ui/DateTimePicker';
import Stepper from '../ui/Stepper';
import { RadioButton } from 'react-native-paper';
import { format, addDays, isAfter, isBefore, startOfDay, eachDayOfInterval, startOfMonth, endOfMonth, isSameDay, differenceInCalendarDays, addMonths, subMonths, parseISO } from 'date-fns';

const RangeCalendar = ({ minDate, maxDate, selectedDates = [], onToggle, colorPrimary, textColor }) => {
  const [currentMonth, setCurrentMonth] = useState(startOfMonth(minDate || new Date()));

  useEffect(() => {
    if (minDate) {
      setCurrentMonth(startOfMonth(minDate));
    }
  }, [minDate]);

  const goPrev = () => setCurrentMonth(prev => subMonths(prev, 1));
  const goNext = () => setCurrentMonth(prev => addMonths(prev, 1));

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
    const blanks = firstDay.getDay(); // 0 (Sun) - 6 (Sat)
    const cells = [];
    for (let i = 0; i < blanks; i++) cells.push(<View key={`blank-${i}`} style={localStyles.smallDayCell} />);
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
          <Text style={[localStyles.smallDayText, { color: textColor }, selected && { color: '#fff' }]}>{format(d, 'd')}</Text>
        </TouchableOpacity>
      );
    });
    return cells;
  };

  if (days.length === 0) {
    return <Text style={{color: textColor}}>Kein gültiger Zeitraum definiert.</Text>;
  }

  return (
    <ScrollView style={{ maxHeight: 300, borderWidth: 1, borderColor: '#ccc', borderRadius: 8, padding: 8 }}>
      {Object.keys(months).map(monthLabel => (
        <View key={monthLabel} style={{ marginBottom: spacing.md }}>
          <Text style={{ marginBottom: spacing.sm, fontWeight: '600', color: textColor }}>{monthLabel}</Text>
          <View style={localStyles.weekDaysRowSmall}>
            {['S','M','T','W','T','F','S'].map(w => <Text key={w} style={[localStyles.weekDayTextSmall, {color: textColor}]}>{w}</Text>)}
          </View>
          <View style={localStyles.monthGrid}>
            {renderMonthCells(months[monthLabel])}
          </View>
        </View>
      ))}
    </ScrollView>
  );
};


const PollCreateModal = ({ isOpen, onClose, onSuccess, poll }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const colors = getThemeColors(theme);
    const [step, setStep] = useState(0);
    const [type, setType] = useState('MULTIPLE_CHOICE');
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        startTime: new Date(),
        endTime: addDays(new Date(), 7),
        closesAt: null,
    });
    const [optionTexts, setOptionTexts] = useState(['', '']);
    const [options, setOptions] = useState({ allowGuests: false, requireVerificationCode: false, availableDays: [] });
    const [verificationCode, setVerificationCode] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();

    const resetState = () => {
        setStep(0);
        setType(poll ? poll.type : 'MULTIPLE_CHOICE');
        setFormData({ 
            title: poll ? poll.question : '', 
            description: poll ? poll.description : '', 
            startTime: poll?.startTime ? parseISO(poll.startTime) : new Date(), 
            endTime: poll?.endTime ? parseISO(poll.endTime) : addDays(new Date(), 7), 
            closesAt: poll?.closesAt ? parseISO(poll.closesAt) : null 
        });
        setOptionTexts(poll?.pollOptions?.map(opt => opt.optionText) || ['', '']);
        setOptions(poll?.optionsMap || { allowGuests: false, requireVerificationCode: false, availableDays: [] });
        setVerificationCode(poll?.verificationCode || '');
        setError('');
    };

    useEffect(() => {
        if (isOpen) resetState();
    }, [isOpen, poll]);
    
    useEffect(() => {
        setOptions(prev => ({...prev, type}));
    }, [type]);

    const handleOptionChange = (text, index) => {
        const newOptions = [...optionTexts];
        newOptions[index] = text;
        setOptionTexts(newOptions);
    };

    const handleAddOption = () => setOptionTexts([...optionTexts, '']);
    const handleRemoveOption = (index) => {
        if (optionTexts.length > 2) setOptionTexts(optionTexts.filter((_, i) => i !== index));
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
        
        if (!formData.title.trim()) {
            setError("Titel/Frage darf nicht leer sein.");
            setIsSubmitting(false);
            return;
        }

        if (type === 'MULTIPLE_CHOICE') {
            const validOptions = optionTexts.filter(t => t.trim());
            if (validOptions.length < 2) {
                setError("Mindestens zwei Antwortoptionen sind erforderlich.");
                setIsSubmitting(false);
                return;
            }
        }
        
        const isSchedulingType = type === 'SCHEDULING' || type === 'AVAILABILITY';

        if (isSchedulingType && !poll) { // Don't validate days on edit, as they might not be in view
            if (options.availableDays.length === 0) {
                setError("Bitte wählen Sie mindestens einen Tag aus.");
                setIsSubmitting(false);
                return;
            }
        }

        try {
            const payload = {
                question: formData.title, // Backend expects 'question'
                description: formData.description,
                type,
                options: { ...options, type },
                optionTexts: type === 'MULTIPLE_CHOICE' ? optionTexts.filter(t => t.trim()) : [],
                startTime: isSchedulingType ? format(startOfDay(formData.startTime), "yyyy-MM-dd'T'HH:mm:ss") : null,
                endTime: isSchedulingType ? format(startOfDay(formData.endTime), "yyyy-MM-dd'T'HH:mm:ss") : null,
                closesAt: formData.closesAt ? format(formData.closesAt, "yyyy-MM-dd'T'HH:mm:ss") : null,
                verificationCode: options.requireVerificationCode ? verificationCode : null
            };

            const result = poll
                ? await apiClient.put(`/admin/polls/${poll.id}`, { id: poll.id, ...payload })
                : await apiClient.post('/admin/polls', payload);

            if (result.success) {
                addToast(`Umfrage erfolgreich ${poll ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Ein Fehler ist aufgetreten.');
        } finally {
            setIsSubmitting(false);
        }
    };
    
    const isSchedulingType = type === 'SCHEDULING' || type === 'AVAILABILITY';
    const steps = ['Typ', 'Details'];
    if (isSchedulingType && !poll) steps.push('Tage'); // Don't show "Tage" step on edit
    if (type === 'MULTIPLE_CHOICE') steps.push('Optionen');
    steps.push('Einstellungen');

    
    const renderStepContent = () => {
        const currentStepName = steps[step];

        switch(currentStepName) {
            case 'Typ':
                 return (
                    <View>
                        <Text style={styles.label}>Welche Art von Umfrage möchten Sie erstellen?</Text>
                        <RadioButton.Group onValueChange={setType} value={type}>
                            <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="MULTIPLE_CHOICE" disabled={!!poll} /><Text style={{color: colors.text}}>Multiple Choice</Text></View>
                            <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="WORD_CLOUD" disabled={!!poll} /><Text style={{color: colors.text}}>Wortwolke</Text></View>
                            <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="AVAILABILITY" disabled={!!poll} /><Text style={{color: colors.text}}>Einfache Verfügbarkeit (Intern)</Text></View>
                            <View style={{flexDirection: 'row', alignItems: 'center'}}><RadioButton value="SCHEDULING" disabled={!!poll} /><Text style={{color: colors.text}}>Terminfindung (Öffentlich)</Text></View>
                        </RadioButton.Group>
                    </View>
                );
            case 'Details':
                return (
                     <View>
                        <Text style={styles.label}>{isSchedulingType ? 'Titel' : 'Frage'}</Text>
                        <TextInput style={styles.input} value={formData.title} onChangeText={val => setFormData({...formData, title: val})} placeholder={isSchedulingType ? "z.B. Sommertreffen" : "z.B. Welches Essen wollt ihr?"} placeholderTextColor={colors.textMuted}/>
                        <Text style={styles.label}>Beschreibung (optional)</Text>
                        <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({...formData, description: val})} multiline placeholderTextColor={colors.textMuted}/>
                        
                        {isSchedulingType && (
                            <View style={{flexDirection: 'row', gap: 8}}>
                                <View style={{flex: 1}}>
                                    <DateTimePicker label="Zeitraum Start" value={formData.startTime} onChange={date => setFormData({...formData, startTime: date})} mode="date" />
                                </View>
                                <View style={{flex: 1}}>
                                    <DateTimePicker label="Zeitraum Ende" value={formData.endTime} onChange={date => setFormData({...formData, endTime: date})} mode="date" />
                                </View>
                            </View>
                        )}
                        <DateTimePicker label="Autom. schließen am (optional)" value={formData.closesAt} onChange={date => setFormData({...formData, closesAt: date})} mode="datetime" />
                    </View>
                );
            case 'Tage':
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
                        <Text style={{marginTop: 8, color: colors.text}}>Ausgewählt: {options.availableDays.length} Tage</Text>
                    </View>
                );
            case 'Optionen':
                     return (
                        <>
                            <Text style={styles.label}>Antwortoptionen</Text>
                            {optionTexts.map((option, index) => (
                                <View key={index} style={{ flexDirection: 'row', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.sm }}>
                                    <TextInput style={[styles.input, { flex: 1, marginBottom: 0 }]} value={option} onChangeText={(text) => handleOptionChange(text, index)} placeholder={`Option ${index + 1}`} placeholderTextColor={colors.textMuted}/>
                                    {optionTexts.length > 2 && (
                                        <TouchableOpacity onPress={() => handleRemoveOption(index)}>
                                            <Icon name="times-circle" solid size={24} color={colors.danger} />
                                        </TouchableOpacity>
                                    )}
                                </View>
                            ))}
                            <TouchableOpacity style={[styles.button, styles.secondaryButton, { alignSelf: 'flex-start' }]} onPress={handleAddOption}>
                                <Icon name="plus" size={14} color={colors.text} />
                                <Text style={{color: colors.text}}> Option hinzufügen</Text>
                            </TouchableOpacity>
                        </>
                     );
            case 'Einstellungen':
                     return (
                        <>
                            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.md, backgroundColor: colors.surface, padding: 8, borderRadius: 8, borderWidth: 1, borderColor: colors.border }}>
                              <View style={{flex: 1}}>
                                <Text style={{fontWeight: 'bold', color: colors.text}}>Gästen die Teilnahme erlauben?</Text>
                                <Text style={{fontSize: 12, color: colors.textMuted}}>Ermöglicht die Teilnahme über einen öffentlichen Link ohne Login.</Text>
                              </View>
                              <Switch value={options.allowGuests} onValueChange={val => setOptions(prev => ({ ...prev, allowGuests: val }))} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
                            </View>

                            {options.allowGuests && (
                              <>
                                <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: spacing.md, backgroundColor: colors.surface, padding: 8, borderRadius: 8, borderWidth: 1, borderColor: colors.border }}>
                                  <View style={{flex: 1}}>
                                    <Text style={{fontWeight: 'bold', color: colors.text}}>Verifizierungscode erzwingen?</Text>
                                    <Text style={{fontSize: 12, color: colors.textMuted}}>Gäste müssen diesen Code eingeben, um abzustimmen.</Text>
                                  </View>
                                  <Switch value={options.requireVerificationCode} onValueChange={val => setOptions(prev => ({ ...prev, requireVerificationCode: val }))} trackColor={{ false: colors.border, true: colors.primary }} thumbColor={colors.white} />
                                </View>
                                {options.requireVerificationCode && (
                                  <View>
                                    <Text style={styles.label}>Code</Text>
                                    <TextInput style={styles.input} value={verificationCode} onChangeText={setVerificationCode} placeholder="z.B. Team123" placeholderTextColor={colors.textMuted} />
                                  </View>
                                )}
                              </>
                            )}
                        </>
                     );
            default:
                return <Text style={{color: colors.text}}>Fertig zum Speichern.</Text>;
        }
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={poll ? "Umfrage bearbeiten" : "Neue Umfrage erstellen"}
        >
            <ScrollView keyboardShouldPersistTaps="handled">
                {error && <Text style={styles.errorText}>{error}</Text>}
                <Stepper steps={steps} currentStep={step} />
                <View style={{ marginVertical: spacing.md, minHeight: 300 }}>
                    {renderStepContent()}
                </View>

                <View style={{ flexDirection: 'row', justifyContent: 'space-between', marginTop: 16 }}>
                    <TouchableOpacity
                    style={[styles.button, styles.secondaryButton, step === 0 ? styles.disabledButton : null]}
                    onPress={() => setStep(s => Math.max(0, s - 1))}
                    disabled={step === 0}
                    >
                    <Text style={{color: colors.text}}>Zurück</Text>
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
                        {isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Speichern</Text>}
                    </TouchableOpacity>
                    )}
                </View>
            </ScrollView>
        </AdminModal>
    );
};

const localStyles = StyleSheet.create({
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
    aspectRatio: 1,
    padding: 4,
    alignItems: 'center',
    justifyContent: 'center',
  },
  smallDayText: {
    textAlign: 'center',
    fontSize: 14,
  },
});

export default PollCreateModal;