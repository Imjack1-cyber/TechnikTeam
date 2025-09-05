import React from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import Icon from '@expo/vector-icons/FontAwesome5';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing } from '../../../styles/theme';

const DynamicItemRows = ({ rows, setRows, storageItems, availabilityPreview }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);

	const handleRowChange = (index, field, value) => {
		const newRows = [...rows];
		newRows[index][field] = value;
		setRows(newRows);
	};

	const handleAddRow = () => {
		setRows([...rows, { itemId: '', quantity: '1' }]);
	};

	const handleRemoveRow = (index) => {
		setRows(rows.filter((_, i) => i !== index));
	};

	return (
		<View>
			{rows.map((row, index) => {
                const preview = availabilityPreview ? availabilityPreview[row.itemId] : null;
                const isOverbooked = preview && preview.availableQuantity < row.quantity;
                return (
                    <View key={index}>
                        <View style={styles.rowContainer}>
                            <View style={styles.pickerContainer}>
                                <Picker
                                    selectedValue={row.itemId}
                                    onValueChange={(val) => handleRowChange(index, 'itemId', val)}
                                >
                                    <Picker.Item label="-- Artikel auswählen --" value="" />
                                    {storageItems.map(item => <Picker.Item key={item.id} label={`${item.name} (${item.availableQuantity} verf.)`} value={item.id} />)}
                                </Picker>
                            </View>
                            <TextInput
                                style={styles.quantityInput}
                                value={String(row.quantity)}
                                onChangeText={(val) => handleRowChange(index, 'quantity', val)}
                                keyboardType="number-pad"
                            />
                            <TouchableOpacity onPress={() => handleRemoveRow(index)}>
                                <Icon name="times-circle" solid size={24} color={colors.danger} />
                            </TouchableOpacity>
                        </View>
                        {isOverbooked && (
                            <View style={styles.warningContainer}>
                                <Icon name="exclamation-triangle" size={14} color={colors.warning} />
                                <Text style={styles.warningText}>Warnung: Nur {preview.availableQuantity} an diesem Datum verfügbar!</Text>
                            </View>
                        )}
                    </View>
                );
            })}
			<TouchableOpacity style={[styles.button, styles.secondaryButton, {alignSelf: 'flex-start'}]} onPress={handleAddRow}>
                <Icon name="plus" size={14} color={colors.text} />
				<Text style={{color: colors.text}}>Artikel hinzufügen</Text>
			</TouchableOpacity>
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
        warningContainer: {
            flexDirection: 'row',
            alignItems: 'center',
            gap: spacing.sm,
            marginBottom: spacing.md,
            padding: spacing.sm,
            backgroundColor: 'rgba(255, 193, 7, 0.2)', // warning color with opacity
            borderRadius: 4,
        },
        warningText: {
            color: colors.warning,
            fontWeight: 'bold',
        }
    });
};

export default DynamicItemRows;