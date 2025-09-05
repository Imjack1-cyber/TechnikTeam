import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import useWebSocket from '../../hooks/useWebSocket';
import { Picker } from '@react-native-picker/picker';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, typography, spacing } from '../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';

const ChecklistTab = ({ event }) => {
    const navigation = useNavigation();
	const { addToast } = useToast();
	const [checklistItems, setChecklistItems] = useState([]);
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const checklistApiCall = useCallback(() => apiClient.get(`/events/${event.id}/checklist`), [event.id]);
	const { data: initialItems, loading, error, reload } = useApi(checklistApiCall);

	useEffect(() => {
		if (initialItems) setChecklistItems(initialItems);
	}, [initialItems]);

	const handleWebSocketMessage = useCallback((message) => {
		if (message.type === 'checklist_update') {
			setChecklistItems(currentItems =>
				currentItems.map(item => item.id === message.payload.id ? message.payload : item)
			);
		}
	}, []);

	useWebSocket(`/ws/checklist/${event.id}`, handleWebSocketMessage);

	const handleStatusChange = async (itemId, newStatus) => {
		try {
			const result = await apiClient.put(`/events/${event.id}/checklist/${itemId}/status`, { status: newStatus });
			if (!result.success) throw new Error(result.message);
		} catch (err) { addToast(`Fehler beim Aktualisieren: ${err.message}`, 'error'); }
	};

	const handleGenerateChecklist = async () => {
		try {
			const result = await apiClient.post(`/events/${event.id}/checklist/generate`);
			if (result.success) {
				addToast('Checkliste erfolgreich aus Reservierungen generiert.', 'success');
				reload();
			} else { throw new Error(result.message); }
		} catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
	};

	if (loading) return <ActivityIndicator />;
	if (error) return <Text style={styles.errorText}>{error}</Text>;

    const renderItem = ({item}) => (
        <View style={styles.itemRow}>
            <View style={styles.itemInfo}>
                <TouchableOpacity onPress={() => navigation.navigate('StorageItemDetails', { itemId: item.itemId })}>
                    <Text style={styles.itemName}>{item.itemName}</Text>
                </TouchableOpacity>
                <Text>Menge: {item.quantity}</Text>
            </View>
            <View style={styles.pickerContainer}>
                <Picker selectedValue={item.status} onValueChange={(val) => handleStatusChange(item.id, val)}>
                    <Picker.Item label="Ausstehend" value="PENDING" />
                    <Picker.Item label="Eingepackt" value="PACKED_OUT" />
                    <Picker.Item label="Zurück & OK" value="RETURNED_CHECKED" />
                    <Picker.Item label="Zurück & Defekt" value="RETURNED_DEFECT" />
                </Picker>
            </View>
        </View>
    );

	return (
		<View>
			<View style={styles.controlsContainer}>
				<Text style={styles.description}>Haken Sie Artikel beim Ein- und Ausladen ab.</Text>
				<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleGenerateChecklist}>
                    <Icon name="sync" size={14} color={colors.text} />
					<Text style={{color: colors.text}}>Generieren</Text>
				</TouchableOpacity>
			</View>
			<FlatList
                data={checklistItems}
                renderItem={renderItem}
                keyExtractor={item => item.id.toString()}
                ListEmptyComponent={<Text>Keine Artikel auf der Checkliste.</Text>}
            />
		</View>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        controlsContainer: { padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        description: { color: colors.textMuted, marginBottom: spacing.md },
        itemRow: { flexDirection: 'row', alignItems: 'center', padding: spacing.md, borderBottomWidth: 1, borderColor: colors.border },
        itemInfo: { flex: 2 },
        itemName: { fontSize: typography.body, fontWeight: 'bold', color: colors.primary },
        pickerContainer: { flex: 3, borderWidth: 1, borderColor: colors.border, borderRadius: 8 },
    });
};

export default ChecklistTab;