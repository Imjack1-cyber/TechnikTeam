import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import apiClient from '../../../services/apiClient';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing } from '../../../styles/theme';
import { Picker } from '@react-native-picker/picker';
import Icon from '@expo/vector-icons/FontAwesome5';

const KitItemsForm = ({ kit, allStorageItems, onUpdateSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
	const [items, setItems] = useState([]);
	const [isSubmitting, setIsSubmitting] = useState(false);
	const [error, setError] = useState('');

	useEffect(() => {
		const initialItems = (kit.items && kit.items.length > 0)
			? kit.items.map(it => ({ itemId: String(it.itemId), quantity: it.quantity }))
			: [{ itemId: '', quantity: 1 }];
		setItems(initialItems);
	}, [kit.items]);

	const handleItemChange = (index, field, value) => {
		const newItems = [...items];
		const currentItem = { ...newItems[index], [field]: value };

		if (field === 'itemId') {
			const selectedStorageItem = allStorageItems.find(si => si.id === parseInt(value));
			if (selectedStorageItem && currentItem.quantity > selectedStorageItem.maxQuantity) {
				currentItem.quantity = selectedStorageItem.maxQuantity;
			}
		}
		newItems[index] = currentItem;
		setItems(newItems);
	};

	const handleAddItem = () => {
		setItems([...items, { itemId: '', quantity: 1 }]);
	};

	const handleRemoveItem = (index) => {
		const newItems = items.filter((_, i) => i !== index);
		setItems(newItems);
	};

	const handleSubmit = async () => {
		setIsSubmitting(true);
		setError('');

		const validItems = items.filter(item => item.itemId && item.quantity > 0)
			.map(item => ({ itemId: parseInt(item.itemId), quantity: parseInt(item.quantity) }));

		const itemIds = validItems.map(item => item.itemId);
		if (new Set(itemIds).size !== itemIds.length) {
			setError('Jeder Artikel darf nur einmal pro Kit hinzugefügt werden.');
			setIsSubmitting(false);
			return;
		}

		try {
			const result = await apiClient.put(`/kits/${kit.id}/items`, validItems);
			if (result.success) {
				onUpdateSuccess();
			} else {
				throw new Error(result.message);
			}
		} catch (err) {
			setError(err.message || 'Inhalt konnte nicht gespeichert werden.');
		} finally {
			setIsSubmitting(false);
		}
	};

	return (
		<View>
			<Text style={styles.cardTitle}>Inhalt bearbeiten</Text>
			{error && <Text style={styles.errorText}>{error}</Text>}
			<View>
				{items.length === 0 && <Text>Dieses Kit ist leer. Fügen Sie einen Artikel hinzu.</Text>}
				{items.map((item, index) => {
					const selectedStorageItem = allStorageItems.find(si => si.id === parseInt(item.itemId));
					return (
						<View style={styles.rowContainer} key={index}>
                            <View style={styles.pickerContainer}>
                                <Picker
                                    selectedValue={item.itemId}
                                    onValueChange={(val) => handleItemChange(index, 'itemId', val)}
                                >
                                    <Picker.Item label="-- Artikel auswählen --" value="" />
                                    {allStorageItems.map(i => <Picker.Item key={i.id} label={i.name} value={i.id} />)}
                                </Picker>
                            </View>
							<TextInput
								value={String(item.quantity)}
								onChangeText={(val) => handleItemChange(index, 'quantity', val)}
								keyboardType="number-pad"
								style={styles.quantityInput}
							/>
							<TouchableOpacity onPress={() => handleRemoveItem(index)}>
                                <Icon name="times-circle" size={24} color={colors.danger} />
                            </TouchableOpacity>
						</View>
					);
				})}
			</View>
			<View style={styles.actionsContainer}>
				<TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={handleAddItem}>
					<Icon name="plus" size={14} />
                    <Text> Zeile hinzufügen</Text>
				</TouchableOpacity>
				<TouchableOpacity style={[styles.button, styles.successButton]} onPress={handleSubmit} disabled={isSubmitting}>
					<Icon name="save" size={14} color="#fff" />
                    <Text style={styles.buttonText}>{isSubmitting ? 'Speichern...' : ' Kit-Inhalt speichern'}</Text>
				</TouchableOpacity>
			</View>
		</View>
	);
};


const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        rowContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            marginBottom: spacing.sm,
        },
        pickerContainer: {
            flex: 1,
            borderWidth: 1,
            borderColor: colors.border,
            borderRadius: 8,
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
        actionsContainer: {
            marginTop: spacing.md,
            flexDirection: 'row',
            justifyContent: 'space-between',
            alignItems: 'center'
        }
    });
};


export default KitItemsForm;