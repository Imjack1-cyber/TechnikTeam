import React, { useCallback, useState } from 'react';
import { View, Text, StyleSheet, TextInput, TouchableOpacity, ActivityIndicator, ScrollView } from 'react-native';
import { useRoute, useNavigation } from '@react-navigation/native';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useToast } from '../context/ToastContext';
import { useAuthStore } from '../store/authStore';
import { getCommonStyles } from '../styles/commonStyles';
import { Picker } from '@react-native-picker/picker';

const QrActionPage = () => {
    const navigation = useNavigation();
	const route = useRoute();
	const { itemId } = route.params;
	const { addToast } = useToast();
	const [isLoading, setIsLoading] = useState(false);
	const [error, setError] = useState('');
	const [quantity, setQuantity] = useState('1');
	const [activeAction, setActiveAction] = useState(null);
    const [notes, setNotes] = useState('');
    const [eventId, setEventId] = useState('');

    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);

	const itemApiCall = useCallback(() => apiClient.get(`/public/storage/${itemId}`), [itemId]);
	const eventsApiCall = useCallback(() => apiClient.get('/public/events'), []);

	const { data: item, loading: itemLoading, error: itemError, reload: reloadItem } = useApi(itemApiCall);
	const { data: activeEvents, loading: eventsLoading, error: eventsError } = useApi(eventsApiCall);

	const handleSubmit = async () => {
		if (!activeAction) return;
		setIsLoading(true);
		setError('');
		const payload = {
			itemId: parseInt(itemId, 10),
			quantity: parseInt(quantity, 10),
			type: activeAction,
			eventId: eventId ? parseInt(eventId, 10) : null,
			notes: notes,
		};
		try {
			const result = await apiClient.post('/public/storage/transactions', payload);
			if (result.success) {
				addToast(result.message, 'success');
				setQuantity('1');
				setActiveAction(null);
				reloadItem();
			} else { throw new Error(result.message); }
		} catch (err) {
			setError(err.message || `Aktion '${activeAction}' fehlgeschlagen.`);
		} finally {
			setIsLoading(false);
		}
	};

	if (itemLoading || eventsLoading) return <View style={styles.centered}><ActivityIndicator size="large" /></View>;
	if (itemError) return <View style={styles.centered}><Text style={styles.errorText}>{itemError}</Text></View>;
	if (!item) return <View style={styles.centered}><Text style={styles.errorText}>Artikel nicht gefunden.</Text></View>;

	return (
		<ScrollView contentContainerStyle={styles.centered}>
			<View style={[styles.card, {width: '90%'}]}>
				<Text style={styles.title}>Aktion für:</Text>
				<Text style={[styles.cardTitle, {textAlign: 'center'}]}>{item.name}</Text>
				<Text style={{textAlign: 'center', marginBottom: 16}}>Verfügbar: {item.availableQuantity} / {item.quantity}</Text>

				{error && <Text style={styles.errorText}>{error}</Text>}

                <Text style={styles.label}>Anzahl</Text>
                <TextInput style={styles.input} value={quantity} onChangeText={setQuantity} keyboardType="number-pad" />
                
                <Text style={styles.label}>Event (optional)</Text>
                <Picker selectedValue={eventId} onValueChange={setEventId}>
                    <Picker.Item label="Kein Event" value="" />
                    {activeEvents?.map(event => <Picker.Item key={event.id} label={event.name} value={event.id} />)}
                </Picker>
                
                <Text style={styles.label}>Notiz (optional)</Text>
                <TextInput style={styles.input} value={notes} onChangeText={setNotes} />

                <View style={{flexDirection: 'row', gap: 8, marginTop: 16}}>
                    <TouchableOpacity style={[styles.button, styles.dangerButton, {flex: 1}]} onPress={() => { setActiveAction('checkout'); handleSubmit(); }} disabled={isLoading}>
                        <Text style={styles.buttonText}>Entnehmen</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={[styles.button, styles.successButton, {flex: 1}]} onPress={() => { setActiveAction('checkin'); handleSubmit(); }} disabled={isLoading}>
                        <Text style={styles.buttonText}>Einräumen</Text>
                    </TouchableOpacity>
                </View>

				<TouchableOpacity onPress={() => navigation.navigate('Storage')}>
                    <Text style={{color: getThemeColors(theme).primary, textAlign: 'center', marginTop: 24}}>Zurück zur Lagerübersicht</Text>
                </TouchableOpacity>
			</View>
		</ScrollView>
	);
};

export default QrActionPage;