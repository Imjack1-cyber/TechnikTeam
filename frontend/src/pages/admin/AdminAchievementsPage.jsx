import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, TextInput, ActivityIndicator, Alert, Linking } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import Icon from 'react-native-vector-icons/FontAwesome5';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import AdminModal from '../../components/ui/AdminModal';
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
                // Fallback for old custom keys, though they can't be created anymore
                setKeyPrefix(KEY_PREFIXES[0]);
                setKeyValue(achievement.achievementKey);
            }
        } else {
            setFormData({ name: '', description: '', iconClass: 'fa-award', achievementKey: '' });
            setKeyPrefix('EVENT_PARTICIPANT_');
            setKeyValue('');
        }
    }, [achievement, isOpen]);

    // Effect to combine prefix and value into the final achievementKey
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
            <TextInput style={styles.input} value={formData.name} onChangeText={val => setFormData({ ...formData, name: val })} />

            <Text style={styles.label}>System-Schlüssel</Text>
            <View style={{ flexDirection: 'row', gap: spacing.sm, alignItems: 'center' }}>
                <View style={[styles.input, { flex: 2, paddingHorizontal: 0 }]}>
                    <Picker selectedValue={keyPrefix} onValueChange={(itemValue) => setKeyPrefix(itemValue)}>
                        {KEY_PREFIXES.map(p => <Picker.Item key={p} label={p} value={p} />)}
                    </Picker>
                </View>
                 <TextInput 
                    style={[styles.input, { flex: 1 }]} 
                    value={keyValue} 
                    onChangeText={setKeyValue}
                    placeholder={'Wert'}
                    autoCapitalize="characters" 
                />
            </View>
            <Text style={styles.helperText}>{KEY_DESCRIPTIONS[keyPrefix]}</Text>
            <Text style={styles.subtitle}>Resultierender Schlüssel: <Text style={{fontWeight: 'bold'}}>{formData.achievementKey}</Text></Text>


            <Text style={styles.label}>Beschreibung</Text>
            <TextInput style={[styles.input, styles.textArea]} value={formData.description} onChangeText={val => setFormData({ ...formData, description: val })} multiline />

            <Text style={styles.label}>Font Awesome Icon-Klasse</Text>
            <TouchableOpacity style={{ alignSelf: 'flex-start', marginBottom: 4 }} onPress={() => Linking.openURL('https://fontawesome.com/search?m=free&s=solid')}>
                <Text style={{ color: colors.primary, fontSize: typography.small }}>
                    <Icon name="search" /> Icons suchen (solid)
                </Text>
            </TouchableOpacity>
            <TextInput style={styles.input} value={formData.iconClass} onChangeText={val => setFormData({ ...formData, iconClass: val })} placeholder="z.B. fa-star"/>
        </AdminModal>
    );
};

const AdminAchievementsPage = () => {
	const apiCall = useCallback(() => apiClient.get('/achievements'), []);
	const { data: achievements, loading, error, reload } = useApi(apiCall);
	const [isModalOpen, setIsModalOpen] = useState(false);
	const [editingAchievement, setEditingAchievement] = useState(null);
    const { addToast } = useToast();
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const openModal = (achievement = null) => {
		setEditingAchievement(achievement);
		setIsModalOpen(true);
	};
    
    const handleDelete = (achievement) => {
        Alert.alert(`Abzeichen "${achievement.name}" löschen?`, "Diese Aktion kann nicht rückgängig gemacht werden.", [
            { text: 'Abbrechen', style: 'cancel' },
            { text: 'Löschen', style: 'destructive', onPress: async () => {
                try {
                    const result = await apiClient.delete(`/achievements/${achievement.id}`);
                    if (result.success) {
                        addToast('Abzeichen gelöscht', 'success');
                        reload();
                    } else { throw new Error(result.message); }
                } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
            }},
        ]);
    };
    
    const renderItem = ({ item }) => (
        <View style={styles.card}>
            <View style={styles.cardHeader}>
                <Icon name={item.iconClass.replace('fa-', '')} solid size={24} style={styles.cardIcon} />
                <Text style={styles.cardTitle}>{item.name}</Text>
            </View>
            <View style={styles.detailRow}>
                <Text style={styles.label}>Schlüssel:</Text>
                <Text style={styles.code}>{item.achievementKey}</Text>
            </View>
            <Text style={styles.cardDescription}>{item.description}</Text>
            <View style={styles.cardActions}>
                <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal(item)}>
                    <Text style={styles.buttonText}>Bearbeiten</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.dangerOutlineButton]} onPress={() => handleDelete(item)}>
                    <Text style={styles.dangerOutlineButtonText}>Löschen</Text>
                </TouchableOpacity>
            </View>
        </View>
    );

	return (
		<View style={styles.container}>
			<View style={styles.header}>
                <Icon name="award" size={24} style={styles.headerIcon} />
				<Text style={styles.title}>Abzeichen verwalten</Text>
			</View>
            <TouchableOpacity style={[styles.button, styles.successButton, { marginHorizontal: 16, marginBottom: 16, alignSelf: 'flex-start'}]} onPress={() => openModal()}>
                <Icon name="plus" size={16} color="#fff" />
                <Text style={styles.buttonText}>Neues Abzeichen</Text>
            </TouchableOpacity>

			{loading && <ActivityIndicator size="large" />}
			{error && <Text style={styles.errorText}>{error}</Text>}
			
            <FlatList
                data={achievements}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 16 }}
                ListEmptyComponent={
                    <View style={styles.card}>
                        <Text>Keine Abzeichen konfiguriert.</Text>
                    </View>
                }
            />
			{isModalOpen && (
				<AchievementModal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} onSuccess={() => { setIsModalOpen(false); reload(); }} achievement={editingAchievement} />
			)}
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        container: { flex: 1, backgroundColor: colors.background },
        header: { flexDirection: 'row', alignItems: 'center', padding: 16 },
        headerIcon: { color: colors.heading, marginRight: 12 },
        card: { backgroundColor: colors.surface, borderRadius: 8, padding: 16, marginBottom: 12, borderWidth: 1, borderColor: colors.border },
        cardHeader: { flexDirection: 'row', alignItems: 'center', marginBottom: 8 },
        cardIcon: { marginRight: 8, color: colors.primary },
        cardTitle: { fontSize: 18, fontWeight: 'bold' },
        detailRow: { flexDirection: 'row', alignItems: 'center', marginVertical: 4 },
        cardDescription: { marginTop: 8, color: colors.text },
        cardActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 16 },
        buttonText: { color: '#fff' },
        secondaryButtonText: { color: '#fff' },
        dangerOutlineButtonText: { color: colors.danger },
        code: { fontFamily: 'monospace', backgroundColor: colors.background, padding: 4, borderRadius: 4 },
        helperText: {
            fontSize: typography.caption,
            color: colors.textMuted,
            marginTop: -spacing.sm, // pull up under the inputs
            marginBottom: spacing.sm,
        }
    });
};

export default AdminAchievementsPage;