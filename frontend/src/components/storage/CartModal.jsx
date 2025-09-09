import React, { useState } from 'react';
import { View, Text, ScrollView, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import Modal from '../ui/Modal';
import apiClient from '../../services/apiClient';
import { useToast } from '../../context/ToastContext';
import { getCommonStyles } from '../../styles/commonStyles';
import { useAuthStore } from '../../store/authStore';
import { getThemeColors, spacing } from '../../styles/theme';
import Icon from '@expo/vector-icons/FontAwesome5';
import { Picker } from '@react-native-picker/picker';

const CartModal = ({ isOpen, onClose, cart, onUpdateQuantity, onRemove, onSwitchType, onSubmit, activeEvents, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();

    const [notes, setNotes] = useState('');
    const [eventId, setEventId] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [transactionError, setTransactionError] = useState('');

    const handleBulkSubmit = async () => {
        setIsSubmitting(true);
        setTransactionError('');

        const transactions = cart.map(item => ({
            itemId: item.id,
            quantity: item.cartQuantity,
            type: item.type,
            notes,
            eventId: eventId ? parseInt(eventId, 10) : null,
        }));

        try {
            const promises = transactions.map(tx => apiClient.post('/public/storage/transactions', tx));
            const results = await Promise.all(promises);
            const failed = results.filter(r => !r.success);

            if (failed.length > 0) {
                throw new Error(`${failed.length} von ${cart.length} Transaktionen fehlgeschlagen.`);
            }

            addToast('Alle Transaktionen erfolgreich verbucht!', 'success');
            onSuccess();
        } catch (err) {
            setTransactionError(err.message || 'Ein Fehler ist aufgetreten.');
        } finally {
            setIsSubmitting(false);
        }
    };

	const renderCartSection = (title, items, type) => {
		if (items.length === 0) return null;
		return (
			<View style={{ marginBottom: 24 }}>
				<Text style={styles.cardTitle}>{title}</Text>
				{items.map(item => {
					const maxQuantity = type === 'checkout'
						? item.availableQuantity
						: (item.maxQuantity > 0 ? item.maxQuantity - item.quantity : Infinity);

					return (
						<View style={styles.rowContainer} key={`${item.id}-${type}`}>
							<TouchableOpacity onPress={() => onSwitchType(item.id, type)}>
                                <Icon name={type === 'checkout' ? 'arrow-down' : 'arrow-up'} size={20} color={type === 'checkout' ? colors.danger : colors.success} />
                            </TouchableOpacity>
							<Text style={{ flex: 1 }}>{item.name}</Text>
							<TextInput
								value={String(item.cartQuantity)}
								onChangeText={val => onUpdateQuantity(item.id, type, parseInt(val, 10) || 1)}
                                keyboardType="number-pad"
								style={styles.quantityInput}
							/>
							<TouchableOpacity onPress={() => onRemove(item.id, type)}>
                                <Icon name="times-circle" size={24} color={colors.danger} />
                            </TouchableOpacity>
						</View>
					);
				})}
			</View>
		);
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose} title={`Warenkorb (${cart.length} Artikel)`}>
			<ScrollView>
				{transactionError && <Text style={styles.errorText}>{transactionError}</Text>}

				{renderCartSection('Zu Entnehmen', cart.filter(i => i.type === 'checkout'), 'checkout')}
				{renderCartSection('Einzuräumen', cart.filter(i => i.type === 'checkin'), 'checkin')}

				<View style={styles.formGroup}>
					<Text style={styles.label}>Notiz (optional, gilt für alle Artikel)</Text>
					<TextInput style={styles.input} value={notes} onChangeText={setNotes} placeholder="z.B. für Event XYZ" />
				</View>
				<View style={styles.formGroup}>
					<Text style={styles.label}>Zuweisen zu Event (optional, gilt für alle Artikel)</Text>
					<Picker selectedValue={eventId} onValueChange={setEventId}>
						<Picker.Item label="Kein Event" value="" />
						{activeEvents.map(event => (
							<Picker.Item key={event.id} label={event.name} value={event.id} />
						))}
					</Picker>
				</View>

				<TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleBulkSubmit} disabled={isSubmitting || cart.length === 0}>
					{isSubmitting ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>{`Alle ${cart.length} Transaktionen ausführen`}</Text>}
				</TouchableOpacity>
			</ScrollView>
		</Modal>
	);
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return {
        rowContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            marginBottom: spacing.sm,
            paddingVertical: spacing.sm,
            borderBottomWidth: 1,
            borderColor: colors.border,
        },
        quantityInput: {
            width: 70,
            height: 48,
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 8,
            paddingHorizontal: spacing.sm,
            textAlign: 'center',
        },
    };
};

export default CartModal;