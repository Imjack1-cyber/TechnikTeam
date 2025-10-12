import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, ActivityIndicator, Linking } from 'react-native';
import { useToast } from '../../../context/ToastContext';
import apiClient from '../../../services/apiClient';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import AdminModal from '../../ui/AdminModal';
import { getThemeColors, typography, spacing } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { Picker } from '@react-native-picker/picker';

const AchievementModal = ({ isOpen, onClose, onSuccess, achievement }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState('');
    const { addToast } = useToast();
    const [formData, setFormData] = useState({ name: '', description: '', iconClass: 'fa-award', achievementKey: '' });
    const [keyPrefix, setKeyPrefix] = useState('EVENT_PARTICIPANT_');
    const [keyValue, setKeyValue] = useState('');

    const KEY_PREFIXES = ['EVENT_PARTICIPANT_', 'EVENT_LEADER_', 'QUALIFICATION_GAINED_'];

    const KEY_DESCRIPTIONS = {
        'EVENT_PARTICIPANT_': 'Wert ist die Anzahl der abgeschlossenen Events (z.B. 1, 5, 10).',
        'EVENT_LEADER_': 'Wert ist die Anzahl der geleiteten Events.',
        'QUALIFICATION_GAINED_': 'Wert ist die Abkürzung des Kurses (z.B. TON-GL).',
    };

    useEffect(() => {
        if (achievement) {
            setFormData({
                name: achievement.name || '',
                description: achievement.description || '',
                iconClass: achievement.iconClass || 'fa-award',
                achievementKey: achievement.achievementKey || ''
            });

            const foundPrefix = KEY_PREFIXES.find(p => achievement.achievementKey.startsWith(p));
            if (foundPrefix) {
                setKeyPrefix(foundPrefix);
                setKeyValue(achievement.achievementKey.substring(foundPrefix.length));
            } else {
                setKeyPrefix(KEY_PREFIXES[0]);
                setKeyValue(achievement.achievementKey);
            }
        } else {
            setFormData({ name: '', description: '', iconClass: 'fa-award', achievementKey: '' });
            setKeyPrefix('EVENT_PARTICIPANT_');
            setKeyValue('');
        }
    }, [achievement, isOpen]);

    useEffect(() => {
        const finalKey = `${keyPrefix}${keyValue}`;
        setFormData(prev => ({ ...prev, achievementKey: finalKey.toUpperCase() }));
    }, [keyPrefix, keyValue]);


    const handleSubmit = async () => {
        setIsSubmitting(true);
        setError('');
        try {
            const result = achievement
                ? await apiClient.put(`/achievements/${achievement.id}`, formData)
                : await apiClient.post('/achievements', formData);
            if (result.success) {
                addToast(`Abzeichen erfolgreich ${achievement ? 'aktualisiert' : 'erstellt'}.`, 'success');
                onSuccess();
            } else { throw new Error(result.message); }
        } catch (err) {
            setError(err.message || 'Fehler beim Speichern');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal
            isOpen={isOpen}
            onClose={onClose}
            title={achievement ? 'Abzeichen bearbeiten' : 'Neues Abzeichen'}
            onSubmit={handleSubmit}
            isSubmitting={isSubmitting}
        >
            {error && <Text style={styles.errorText}>{error}</Text>}
            <Text style={styles.label}>Name</Text>
            <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({ ...formData, name: val })} placeholderTextColor={colors.textMuted} />

            <Text style={styles.label}>System-Schlüssel</Text>
            <View style={{ flexDirection: 'row', gap: spacing.sm, alignItems: 'center' }}>
                <View style={[styles.input, { flex: 2, paddingHorizontal: 0 }]}>
                    <Picker selectedValue={keyPrefix} onValueChange={(itemValue) => setKeyPrefix(itemValue)} itemStyle={{ color: colors.text }}>
                        {KEY_PREFIXES.map(p => <Picker.Item key={p} label={p} value={p} />)}
                    </Picker>
                </View>
                 <TextInput 
                    style={[styles.input, { flex: 1 }]} 
                    value={keyValue} 
                    onChangeText={setKeyValue}
                    placeholder={'Wert'}
                    autoCapitalize="characters" 
                    placeholderTextColor={colors.textMuted}
                />
            </View>
            <Text style={styles.helperText}>{KEY_DESCRIPTIONS[keyPrefix]}</Text>
            <Text style={styles.subtitle}>Resultierender Schlüssel: <Text style={{fontWeight: 'bold'}}>{formData.achievementKey}</Text></Text>


            <Text style={styles.label}>Beschreibung</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({ ...formData, description: val })} multiline placeholderTextColor={colors.textMuted} />

            <Text style={styles.label}>Font Awesome Icon-Klasse</Text>
            <TouchableOpacity style={{ alignSelf: 'flex-start', marginBottom: 4 }} onPress={() => Linking.openURL('https://fontawesome.com/search?m=free&s=solid')}>
                <Text style={{ color: colors.primary, fontSize: typography.small }}>
                    <Icon name="search" /> Icons suchen (solid)
                </Text>
            </TouchableOpacity>
            <TextInput style={styles.input} value={formData.iconClass} onChangeText={val => setFormData({ ...formData, iconClass: val })} placeholder="z.B. fa-star" placeholderTextColor={colors.textMuted}/>
        </AdminModal>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return {
        helperText: {
            fontSize: typography.caption,
            color: colors.textMuted,
            marginTop: -spacing.sm,
            marginBottom: spacing.sm,
        }
    };
};

export default AchievementModal;