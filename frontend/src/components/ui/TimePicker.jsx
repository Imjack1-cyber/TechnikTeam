import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform, Modal as RNModal } from 'react-native';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing } from '../../styles/theme';
import DateTimePickerModal from 'react-native-modal-datetime-picker';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { format, setHours, setMinutes } from 'date-fns';

const WebTimePickerModal = ({ isVisible, date, onConfirm, onCancel }) => {
    const [hour, setHour] = useState(date.getHours());
    const [minute, setMinute] = useState(date.getMinutes());

    const handleConfirm = () => {
        let newDate = setHours(date, hour);
        newDate = setMinutes(newDate, minute);
        onConfirm(newDate);
    };

    return (
        <RNModal visible={isVisible} transparent animationType="fade" onRequestClose={onCancel}>
            <View style={localStyles.modalOverlay}>
                <View style={localStyles.modalContent}>
                    <Text style={localStyles.headerTitle}>Uhrzeit auswählen</Text>
                    <View style={localStyles.pickerContainer}>
                        <TouchableOpacity onPress={() => setHour(h => (h + 1) % 24)}><Icon name="chevron-up" size={24} /></TouchableOpacity>
                        <Text style={localStyles.timeText}>{String(hour).padStart(2, '0')}</Text>
                        <TouchableOpacity onPress={() => setHour(h => (h - 1 + 24) % 24)}><Icon name="chevron-down" size={24} /></TouchableOpacity>
                    </View>
                    <Text style={localStyles.timeText}>:</Text>
                    <View style={localStyles.pickerContainer}>
                         <TouchableOpacity onPress={() => setMinute(m => (m + 1) % 60)}><Icon name="chevron-up" size={24} /></TouchableOpacity>
                        <Text style={localStyles.timeText}>{String(minute).padStart(2, '0')}</Text>
                        <TouchableOpacity onPress={() => setMinute(m => (m - 1 + 60) % 60)}><Icon name="chevron-down" size={24} /></TouchableOpacity>
                    </View>
                    <View style={localStyles.actionsRow}>
                         <TouchableOpacity onPress={onCancel} style={[localStyles.actionButton, localStyles.cancelButton]}><Text>Abbrechen</Text></TouchableOpacity>
                         <TouchableOpacity onPress={handleConfirm} style={[localStyles.actionButton, localStyles.confirmButton]}><Text style={{ color: '#fff' }}>OK</Text></TouchableOpacity>
                    </View>
                </View>
            </View>
        </RNModal>
    );
};


const TimePicker = ({ label, value, onChange, isWebModal = false }) => {
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

    if (isWebModal) { // Special compact layout for embedding in another modal
        return (
             <View style={localStyles.webModalContainer}>
                <Text style={styles.label}>Uhrzeit</Text>
                <TouchableOpacity onPress={showPicker} style={[styles.input, { justifyContent: 'center' }]}>
                    <Text style={{color: colors.text}}>{format(value, 'HH:mm')}</Text>
                </TouchableOpacity>
                 <WebTimePickerModal isVisible={isPickerVisible} date={value || new Date()} onConfirm={handleConfirm} onCancel={hidePicker} />
            </View>
        );
    }

    return (
        <View style={styles.formGroup}>
            <Text style={styles.label}>{label}</Text>
            <TouchableOpacity onPress={showPicker} style={[styles.input, { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }]}>
                <Text style={value ? { color: colors.text } : { color: colors.textMuted }}>
                    {value ? format(value, 'HH:mm') : 'Zeit auswählen'}
                </Text>
                <Icon name="clock" size={20} color={colors.primary} />
            </TouchableOpacity>
             {Platform.OS === 'web' ? (
                <WebTimePickerModal isVisible={isPickerVisible} date={value || new Date()} onConfirm={handleConfirm} onCancel={hidePicker} />
            ) : (
                <DateTimePickerModal
                    isVisible={isPickerVisible}
                    mode="time"
                    date={value || new Date()}
                    onConfirm={handleConfirm}
                    onCancel={hidePicker}
                    is24Hour={true}
                    locale="de_DE"
                />
            )}
        </View>
    );
};

const localStyles = StyleSheet.create({
    modalOverlay: { flex: 1, backgroundColor: '#00000088', justifyContent: 'center', padding: 20 },
    modalContent: { backgroundColor: '#fff', borderRadius: 12, padding: 12, alignItems: 'center' },
    pickerContainer: { flexDirection: 'column', alignItems: 'center', marginHorizontal: 16 },
    timeText: { fontSize: 48, fontWeight: 'bold', marginVertical: 8 },
    actionsRow: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 16, width: '100%' },
    actionButton: { paddingVertical: 10, paddingHorizontal: 14, borderRadius: 6, marginLeft: 8 },
    cancelButton: { backgroundColor: '#eee' },
    confirmButton: { backgroundColor: '#1976D2' },
    headerTitle: { fontWeight: '600', fontSize: 16, marginBottom: 16 },
    webModalContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: spacing.md,
        marginTop: spacing.md,
        paddingTop: spacing.md,
        borderTopWidth: 1,
        borderTopColor: '#eee'
    },
});


export default TimePicker;